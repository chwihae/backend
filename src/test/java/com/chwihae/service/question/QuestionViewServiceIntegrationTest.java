package com.chwihae.service.question;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class QuestionViewServiceIntegrationTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        Set<String> keys = userContextRedisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            userContextRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("질문 조회 엔티티를 저장한다")
    void createQuestionView() {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));

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
        QuestionEntity question = questionRepository.save(createQuestion(user));
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
        QuestionEntity question = questionRepository.save(createQuestion(user));
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
        QuestionEntity question = questionRepository.save(createQuestion(user));
        questionViewService.createQuestionView(question);
        questionViewCacheRepository.setViewCount(question.getId(), cachedViewCount);

        //when
        questionViewService.syncQuestionViewCount();

        //then
        assertThat(questionViewRepository.findByQuestionEntityId(question.getId()).get().getViewCount()).isEqualTo(cachedViewCount);
        assertThat(questionViewCacheRepository.getViewCount(question.getId())).isEmpty();
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .type(QuestionType.ETC)
                .build();
    }
}