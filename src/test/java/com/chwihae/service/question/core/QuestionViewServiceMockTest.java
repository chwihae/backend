package com.chwihae.service.question.core;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.test.AbstractMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestionViewServiceMockTest extends AbstractMockTest {

    @InjectMocks
    private QuestionViewService questionViewService;

    @Mock
    private QuestionViewRepository questionViewRepository;

    @Mock
    private QuestionViewCacheRepository questionViewCacheRepository;

    @Mock
    private RedisTemplate<String, String> questionViewLockRedisTemplate;

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
        Long givenViewCount = 100L;

        when(questionViewCacheRepository.getViewCount(givenQuestionId)).thenReturn(Optional.empty());
        when(questionViewRepository.findViewCountByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenViewCount));

        // when
        Long viewCount = questionViewService.getViewCount(givenQuestionId);

        // then
        Assertions.assertThat(givenViewCount).isEqualTo(viewCount);
        verify(questionViewCacheRepository).setViewCount(givenQuestionId, givenViewCount);
    }

    @Test
    @DisplayName("질문 조회 엔티티가 DB에 없을 경우 예외 발생가 발생한다")
    void getViewCount_whenNotFound_throwsException() {
        // given
        Long givenQuestionId = 1L;

        when(questionViewCacheRepository.getViewCount(givenQuestionId)).thenReturn(Optional.empty());
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

        ValueOperations<String, String> opsMock = mock(ValueOperations.class);
        when(questionViewLockRedisTemplate.opsForValue()).thenReturn(opsMock);
        when(opsMock.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(questionViewCacheRepository.existsByQuestionId(givenQuestionId)).thenReturn(true);

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
        Long givenViewCount = 100L;

        // given
        ValueOperations<String, String> opsMock = mock(ValueOperations.class);
        when(questionViewLockRedisTemplate.opsForValue()).thenReturn(opsMock);
        when(opsMock.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(questionViewCacheRepository.existsByQuestionId(givenQuestionId)).thenReturn(false);
        when(questionViewRepository.findViewCountByQuestionEntityId(givenQuestionId)).thenReturn(Optional.of(givenViewCount));

        // when
        questionViewService.incrementViewCount(givenQuestionId);

        // then
        verify(questionViewCacheRepository).setViewCount(givenQuestionId, givenViewCount);
        verify(questionViewCacheRepository).incrementViewCount(givenQuestionId);
    }

    @Test
    @DisplayName("캐싱 되어 있는 질문 조회 수가 없으면 DB에서 조회하여 가져온다")
    void getViewCounts_returnList() throws Exception {
        //given
        QuestionEntity questionEntity = mock(QuestionEntity.class);
        QuestionViewEntity questionViewEntity = QuestionViewFixture.of(questionEntity);
        long expectedViewCount = 100L;
        questionViewEntity.setViewCount(expectedViewCount);

        when(questionViewCacheRepository.getViewCounts(any())).thenReturn(new ArrayList<>());
        when(questionViewRepository.findByQuestionEntityIds(any())).thenReturn(List.of(questionViewEntity));

        //when
        List<QuestionViewResponse> response = questionViewService.getViewCounts(List.of(1L));

        //then
        verify(questionViewRepository, times(1)).findByQuestionEntityIds(any());
    }

    @Test
    @DisplayName("질문 아이디가 모두 캐싱되어 있으면 DB에서 조회하지 않는다")
    void getViewCounts_doNotQueryDb() throws Exception {
        //given
        QuestionEntity questionEntity = mock(QuestionEntity.class);
        QuestionViewEntity questionViewEntity = QuestionViewFixture.of(questionEntity);
        long expectedViewCount = 100L;
        questionViewEntity.setViewCount(expectedViewCount);
        when(questionViewCacheRepository.getViewCounts(any())).thenReturn(new ArrayList<>());

        //when
        List<QuestionViewResponse> response = questionViewService.getViewCounts(List.of());

        //then
        verify(questionViewRepository, never()).findByQuestionEntityIds(any());
    }

    // TODO test - redis와 DB에 view를 합쳐서 리턴
}