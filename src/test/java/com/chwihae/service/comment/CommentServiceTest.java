package com.chwihae.service.comment;

import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.commenter.CommenterAliasPrefix;
import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
class CommentServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자는 질문글에 댓글을 작성을 남길 수 있다")
    void createComment_pass() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        commenterSequenceRepository.save(createCommenterSequence(question));
        String content = "댓글 내용";

        //when
        commentService.createComment(question.getId(), user.getId(), content);

        //then
        Assertions.assertThat(commentRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사용자가 처음 질문글에 댓글을 작성하면 해당 질문글에서 댓글 작성자 별칭이 생성된다")
    void createComment_createUniqueAlias() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        commenterSequenceRepository.save(createCommenterSequence(question));
        String content = "댓글 내용";
        String expectedAlias = CommenterAliasPrefix.getAlias(1);

        //when
        commentService.createComment(question.getId(), user.getId(), content);

        //then
        Assertions.assertThat(commenterAliasRepository.findAll())
                .hasSize(1)
                .extracting("alias")
                .containsOnly(expectedAlias);
    }

    @Test
    @DisplayName("질문글에 댓글을 남긴 사용자가 해당 질문글에서 댓글을 다시 남겨도 작성자 별칭이 새로 생성되지 않는다")
    void createComment_whenAlreadyCommented_doNotCreateUniqueAlias() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        String expectedAlias = CommenterAliasPrefix.getAlias(1);
        String content = "content";
        CommenterSequenceEntity sequence = commenterSequenceRepository.save(createCommenterSequence(question));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias(expectedAlias, question, user));
        CommentEntity comment = commentRepository.save(createComment(user, content, commenterAlias, question));

        //when
        commentService.createComment(question.getId(), user.getId(), content);

        //then
        Assertions.assertThat(commenterAliasRepository.findAll())
                .hasSize(1)
                .extracting("alias")
                .containsOnly(expectedAlias);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 댓글을 남기면 예외가 발생한다")
    void createComment_withNotExistingQuestionId_throwsException() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        long notExistingUserId = 0L;
        String content = "content";

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(question.getId(), notExistingUserId, content))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 질문글에 댓글을 남기면 예외가 발생한다")
    void createComment_withNotExistingUserId_throwsException() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingUserId = 0L;
        String content = "content";

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(notExistingQuestionId, notExistingUserId, content))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.QUESTION_NOT_FOUND);
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

    public CommentEntity createComment(UserEntity userEntity, String content, CommenterAliasEntity commenterAliasEntity, QuestionEntity questionEntity) {
        return CommentEntity.builder()
                .content(content)
                .userEntity(userEntity)
                .commenterAliasEntity(commenterAliasEntity)
                .questionEntity(questionEntity)
                .build();
    }

    public CommenterAliasEntity createAlias(String alias, QuestionEntity questionEntity, UserEntity userEntity) {
        return CommenterAliasEntity.builder()
                .alias(alias)
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }
}