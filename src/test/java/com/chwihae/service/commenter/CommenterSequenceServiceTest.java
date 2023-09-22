package com.chwihae.service.commenter;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.test.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
class CommenterSequenceServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("댓글 작성자 순서를 저장한다")
    void createCommenterSequence() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));

        //when
        commenterSequenceService.createCommenterSequence(question);

        //then
        Assertions.assertThat(commenterSequenceRepository.findAll()).hasSize(1);
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