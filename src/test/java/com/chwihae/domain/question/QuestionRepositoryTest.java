package com.chwihae.domain.question;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Transactional
class QuestionRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("마감 시간이 지난 질문을 있으면 true를 반환한다")
    void existsByCloseAtBefore_returnTrue() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime closeAt = now.minusDays(1);
        questionRepository.save(createQuestion(user, closeAt));

        //when
        boolean result = questionRepository.existsByCloseAtBefore(now);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("마감 시간이 지난 질문이 없으면 false를 반환한다")
    void existsByCloseAtBefore_returnFalse() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime closeAt = now.plusHours(1);
        questionRepository.save(createQuestion(user, closeAt));

        //when
        boolean result = questionRepository.existsByCloseAtBefore(now);

        //then
        Assertions.assertThat(result).isFalse();
    }

    public QuestionEntity createQuestion(UserEntity userEntity, LocalDateTime closeAt) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(closeAt)
                .type(QuestionType.ETC)
                .build();
    }
}