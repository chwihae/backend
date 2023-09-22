package com.chwihae.domain.commenter;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
class CommenterSequenceRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디로 write lock을 걸고 댓글 작성자 순서를 조회한다")
    void findForUpdateByQuestionEntityId() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        commenterSequenceRepository.save(createCommenterSequence(question));

        //when
        Optional<CommenterSequenceEntity> optionalCommenterSequence = commenterSequenceRepository.findForUpdateByQuestionEntityId(question.getId());

        //then
        Assertions.assertThat(optionalCommenterSequence)
                .isPresent()
                .hasValueSatisfying(
                        it -> Assertions.assertThat(it.getSequence()).isZero()
                );
    }

    @Test
    @DisplayName("질문 아이디로 댓글 작성자 순서를 1 증가시킨다")
    void updateSequenceByQuestionEntityId() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        CommenterSequenceEntity sequence = commenterSequenceRepository.save(createCommenterSequence(question));

        //when
        commenterSequenceRepository.updateSequenceByQuestionEntityId(question.getId());
        entityManager.flush();
        entityManager.clear();

        //then
        Assertions.assertThat(commenterSequenceRepository.findById(sequence.getId()).get().getSequence()).isOne();
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

    public CommenterSequenceEntity createCommenterSequence(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}