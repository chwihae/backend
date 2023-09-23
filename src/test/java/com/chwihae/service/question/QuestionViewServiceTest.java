package com.chwihae.service.question;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.test.AbstractMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestionViewServiceTest extends AbstractMockTest {

    @InjectMocks
    private QuestionViewService questionViewService;

    @Mock
    private QuestionViewRepository questionViewRepository;

    @Mock
    private QuestionViewCacheRepository questionViewCacheRepository;

    @Test
    @DisplayName("질문 조회 엔티티를 저장한다")
    void createQuestionView() {
        // given
        QuestionEntity givenQuestionEntity = mock(QuestionEntity.class);
        QuestionViewEntity givenQuestionViewEntity = QuestionViewEntity.builder().questionEntity(givenQuestionEntity).build();

        when(questionViewRepository.save(any(QuestionViewEntity.class))).thenReturn(givenQuestionViewEntity);

        // when
        questionViewService.createQuestionView(givenQuestionEntity);

        // then
        verify(questionViewRepository).save(any(QuestionViewEntity.class));
    }

    @Test
    @DisplayName("질문 조회수를 조회한다")
    void getViewCount() {
        // given
        Long givenQuestionId = 1L;
        int givenViewCount = 100;

        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.empty());
        when(questionViewRepository.findViewCountByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenViewCount));

        // when
        Integer viewCount = questionViewService.getViewCount(givenQuestionId);

        // then
        Assertions.assertThat(givenViewCount).isEqualTo(viewCount);
        verify(questionViewCacheRepository).setQuestionView(givenQuestionId, givenViewCount);
    }

    @Test
    @DisplayName("질문 조회 엔티티가 DB에 없을 경우 예외 발생가 발생한다")
    void getViewCount_whenNotFound_throwsException() {
        // given
        Long givenQuestionId = 1L;

        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.empty());
        when(questionViewRepository.findViewCountByQuestionEntityId(givenQuestionId)).thenReturn(Optional.empty());

        // then
        Assertions.assertThatThrownBy(() -> questionViewService.getViewCount(givenQuestionId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("캐싱되어 있는 질문 조회 수를 1 증가시킨다")
    void incrementViewCount() {
        // given
        Long givenQuestionId = 1L;

        when(questionViewCacheRepository.existsByKey(givenQuestionId)).thenReturn(true);

        // when
        questionViewService.incrementViewCount(givenQuestionId);

        // then
        verify(questionViewCacheRepository).incrementViewCount(givenQuestionId);
    }

    @Test
    @DisplayName("질문 조회 수가 캐싱되어 있지 않으면 DB에서 가져와서 1 증가시킨다")
    void incrementViewCount_whenQuestionIdNotCached() {
        // given
        Long givenQuestionId = 1L;
        int givenViewCount = 100;

        when(questionViewCacheRepository.existsByKey(givenQuestionId)).thenReturn(false);
        when(questionViewRepository.findViewCountByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenViewCount));

        // when
        questionViewService.incrementViewCount(givenQuestionId);

        // then
        verify(questionViewCacheRepository).setQuestionView(givenQuestionId, givenViewCount);
        verify(questionViewCacheRepository).incrementViewCount(givenQuestionId);
    }

    @Test
    @DisplayName("캐시된 모든 질문의 조회수를 동기화하고, 해당 키를 삭제한다")
    void syncQuestionViewCount() {
        // given
        String givenKey = "question:1:views";
        Long givenQuestionId = 1L;
        int cachedViewCount = 100;

        Set<String> keys = new HashSet<>();
        keys.add(givenKey);

        QuestionViewEntity givenEntity = mock(QuestionViewEntity.class);

        when(questionViewCacheRepository.findAllQuestionViewKeys()).thenReturn(keys);
        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.of(cachedViewCount));
        when(questionViewRepository.findByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenEntity));

        // when
        questionViewService.syncQuestionViewCount();

        // then
        verify(questionViewCacheRepository).deleteKey(givenKey);
        verify(givenEntity).setViewCount(cachedViewCount);
        verify(questionViewRepository).save(givenEntity);
    }

    @Test
    @DisplayName("캐시에서 키 목록을 가져오지 못하면 DB와 동기화가 되지 않는다")
    void syncQuestionViewCount_noKeysFromCache() {
        // given
        when(questionViewCacheRepository.findAllQuestionViewKeys()).thenReturn(null);

        // when
        questionViewService.syncQuestionViewCount();

        // then
        verify(questionViewCacheRepository, never()).getQuestionView(anyLong());
        verify(questionViewRepository, never()).findByQuestionEntityId(anyLong());
    }

    @Test
    @DisplayName("캐시에 저장된 조회수 정보가 없는 경우 캐시에서 키를 삭제한다")
    void syncQuestionViewCount_noViewInfoInCache() {
        // given
        String givenKey = "question:1:views";
        Long givenQuestionId = 1L;

        Set<String> keys = new HashSet<>();
        keys.add(givenKey);

        when(questionViewCacheRepository.findAllQuestionViewKeys()).thenReturn(keys);
        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.empty());

        // when
        questionViewService.syncQuestionViewCount();

        // then
        verify(questionViewCacheRepository).deleteKey(givenKey);
        verify(questionViewRepository, never()).findByQuestionEntityId(anyLong());
    }

    @Test
    @DisplayName("DB 업데이트 중 오류가 발생하면 캐시 키는 삭제되지 않는다")
    void syncQuestionViewCount_errorDuringDbUpdate() {
        // given
        String givenKey = "question:1:views";
        Long givenQuestionId = 1L;
        int cachedViewCount = 100;

        Set<String> keys = new HashSet<>();
        keys.add(givenKey);
        QuestionViewEntity givenEntity = mock(QuestionViewEntity.class);

        when(questionViewCacheRepository.findAllQuestionViewKeys()).thenReturn(keys);
        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.of(cachedViewCount));
        when(questionViewRepository.findByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenEntity));
        doThrow(new RuntimeException()).when(questionViewRepository).save(givenEntity);

        // when //then
        Assertions.assertThatThrownBy(() -> questionViewService.syncQuestionViewCount())
                .isInstanceOf(RuntimeException.class);

        verify(questionViewCacheRepository, never()).deleteKey(givenKey);
    }

    @Test
    @DisplayName("캐시와 DB 모두에서 조회수 정보를 찾지 못하면 캐시에서 키를 삭제한다")
    void syncQuestionViewCount_noViewInfoInCacheOrDb() {
        // given
        String givenKey = "question:1:views";
        Long givenQuestionId = 1L;

        Set<String> keys = new HashSet<>();
        keys.add(givenKey);

        when(questionViewCacheRepository.findAllQuestionViewKeys()).thenReturn(keys);
        when(questionViewCacheRepository.getQuestionView(givenQuestionId)).thenReturn(Optional.empty());

        // when
        questionViewService.syncQuestionViewCount();

        // then
        verify(questionViewCacheRepository).deleteKey(givenKey);
    }
}