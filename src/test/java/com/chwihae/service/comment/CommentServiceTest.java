package com.chwihae.service.comment;

import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.commenter.CommenterAliasPrefix;
import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.comment.Comment;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.infra.fixture.CommentEntityFixture;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Transactional
class CommentServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자는 질문글에 댓글을 작성을 남길 수 있다")
    void createComment_pass() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
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
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
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
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        String expectedAlias = CommenterAliasPrefix.getAlias(1);
        String content = "content";
        CommenterSequenceEntity sequence = commenterSequenceRepository.save(createCommenterSequence(question));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias(expectedAlias, question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

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
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
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

    @Test
    @DisplayName("질문에 등록된 댓글을 페이지네이션으로 조회한다")
    void getComments_returnsPagination() throws Exception {
        // Constants
        final int USER_COUNT = 10;
        final int COMMENT_COUNT = 10;
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;

        UserEntity questioner = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(questioner));
        commenterSequenceRepository.save(createCommenterSequence(question));

        List<UserEntity> users = IntStream.range(0, USER_COUNT)
                .mapToObj(i -> UserEntityFixture.of())
                .toList();
        userRepository.saveAll(users);

        IntStream.range(0, COMMENT_COUNT).forEach(commentIndex -> {
            CommenterAliasEntity alias = createAlias("alias" + commentIndex, question, users.get(commentIndex));
            CommentEntity comment = CommentEntityFixture.of(questioner, question, alias);
            commenterAliasRepository.save(alias);
            commentRepository.save(comment);
        });

        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //when
        Page<Comment> comments = commentService.getComments(question.getId(), questioner.getId(), pageRequest);

        //then
        Assertions.assertThat(comments.getTotalElements()).isEqualTo(COMMENT_COUNT);
        Assertions.assertThat(comments.getSize()).isEqualTo(PAGE_SIZE);
        Assertions.assertThat(comments.getNumber()).isEqualTo(PAGE_NUMBER);
    }

    @Test
    @DisplayName("존재하지 않는 질문의 댓글을 조회하면 예외가 발생한다")
    void getComments_throwsNotFoundException() throws Exception {
        //given
        final int PAGE_NUMBER = 0;
        final int PAGE_SIZE = 2;
        long notExistingQuestionId = 0;
        long notExistingUserId = 0;
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        //when //then
        Assertions.assertThatThrownBy(() -> commentService.getComments(notExistingQuestionId, notExistingUserId, pageRequest))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자는 댓글 내용을 수정할 수 있다")
    void modifyComment_byCommenter_modifyComment() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));
        String modifiedContent = "modified content";

        //when
        commentService.modifyComment(comment.getId(), user.getId(), modifiedContent);

        //then
        Assertions.assertThat(commentRepository.findById(comment.getId()).get().getContent()).isEqualTo(modifiedContent);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 예외가 발생한다")
    void modifyComment_whenCommentNotExists_throwException() throws Exception {
        //given
        long notExistingCommentId = 0L;
        long notExistingUserId = 0L;

        //when//then
        Assertions.assertThatThrownBy(() -> commentService.modifyComment(notExistingCommentId, notExistingUserId, "content"))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글 내용을 수정하면 예외가 발생한다")
    void modifyComment_byOther_throwsException() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));
        String modifiedContent = "modified content";
        long notExistingUserId = 0L;

        //when//then
        Assertions.assertThatThrownBy(() -> commentService.modifyComment(comment.getId(), notExistingUserId, modifiedContent))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("댓글 작성자는 댓글을 삭제할 수 있다")
    void deleteComment_byCommenter_delete() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));

        //when
        commentService.deleteComment(comment.getId(), user.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteComment_whenCommentNotExists_throwException() throws Exception {
        //given
        long notExistingCommentId = 0L;
        long notExistingUserId = 0L;

        //when//then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(notExistingCommentId, notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글 내용을 삭제하려고 하면 예외가 발생한다")
    void deleteComment_byOther_throwsException() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(user));
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias("alias", question, user));
        CommentEntity comment = commentRepository.save(CommentEntityFixture.of(user, question, commenterAlias));
        long notExistingUserId = 0L;

        //when//then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    public CommenterSequenceEntity createCommenterSequence(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
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