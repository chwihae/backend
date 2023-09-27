package com.chwihae.service.question.core;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@Transactional
class QuestionViewServiceIntegrationTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        questionViewCacheRepository.clear();
    }

    @Test
    @DisplayName("질문 조회 엔티티를 저장한다")
    void createQuestionView() {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));

        //when
        questionViewService.createQuestionView(question);

        //then
        assertThat(questionViewRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("질문 조회수를 조회한다")
    void getViewCount() {
        //given
        Long questionId = 1L;
        Long expectedViewCount = 100L;

        questionViewCacheRepository.setViewCount(questionId, expectedViewCount);

        //when
        Long viewCount = questionViewService.getViewCount(questionId);

        //then
        assertThat(viewCount).isEqualTo(expectedViewCount);
    }

    @Test
    @DisplayName("질문 조회 엔티티가 DB에 없을 경우 예외 발생가 발생한다")
    void getViewCount_whenNotFound_throwsException() {
        //given
        Long questionId = 1L;

        //when/Then
        assertThatThrownBy(() -> questionViewService.getViewCount(questionId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("error", QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("캐싱되어 있는 질문 조회 수를 1 증가시킨다")
    void incrementViewCount() {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        questionViewService.createQuestionView(question);
        questionViewCacheRepository.setViewCount(question.getId(), 0L);

        //when
        questionViewService.incrementViewCount(question.getId());

        //then
        assertThat(questionViewCacheRepository.getViewCount(question.getId()))
                .isPresent()
                .hasValueSatisfying(it -> {
                    assertThat(it).isOne();
                });
    }

    @Test
    @DisplayName("질문 조회 수가 캐싱되어 있지 않으면 DB에서 가져와서 1 증가시킨다")
    void incrementViewCount_whenQuestionIdNotCached() {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        questionViewService.createQuestionView(question);

        //when
        questionViewService.incrementViewCount(question.getId());

        //then
        assertThat(questionViewCacheRepository.getViewCount(question.getId()))
                .isPresent()
                .hasValueSatisfying(it -> {
                    assertThat(it).isOne();
                });
    }

    @Test
    @DisplayName("캐시된 모든 질문의 조회수를 동기화하고, 해당 키를 삭제한다")
    void syncQuestionViewCount() {
        //given
        Long cachedViewCount = 100L;

        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        questionViewService.createQuestionView(question);
        questionViewCacheRepository.setViewCount(question.getId(), cachedViewCount);

        //when
        questionViewService.syncQuestionViewCount();

        //then
        assertThat(questionViewRepository.findByQuestionEntityId(question.getId()).get().getViewCount()).isEqualTo(cachedViewCount);
        assertThat(questionViewCacheRepository.getViewCount(question.getId())).isEmpty();
    }

    @Test
    @DisplayName("캐싱 되어있는 질문 아이디 리스트가 없으면 질문 조회 엔티티 리스트를 반환받는다")
    void findViewCountsByQuestionEntityIds_returnList() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = QuestionEntityFixture.of(user);
        QuestionEntity question2 = QuestionEntityFixture.of(user);
        questionRepository.saveAll(List.of(question1, question2));
        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        long viewCount1 = 100L;
        view1.setViewCount(viewCount1);
        long viewCount2 = 100L;
        view2.setViewCount(viewCount2);
        questionViewRepository.saveAll(List.of(view1, view2));

        //when
        List<QuestionViewResponse> response = questionViewService.getViewCounts(List.of(question1.getId(), question2.getId()));

        //then
        Assertions.assertThat(response)
                .hasSize(2)
                .extracting("questionId", "viewCount")
                .containsOnly(
                        tuple(question1.getId(), viewCount1),
                        tuple(question2.getId(), viewCount2)
                );
    }

    @Test
    @DisplayName("질문 아이디에 해당되는 질문 조회 엔티티가 없으면 빈배열을 반환한다")
    void findViewCountsByQuestionEntityIds_returnEmpty() throws Exception {
        //when
        List<QuestionViewResponse> response = questionViewService.getViewCounts(List.of());

        //then
        Assertions.assertThat(response).isEmpty();
    }
}