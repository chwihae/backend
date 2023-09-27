package com.chwihae.service.comment;

import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.commenter.*;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.comment.Comment;
import com.chwihae.exception.CustomException;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.COMMENT_NOT_FOUND;
import static com.chwihae.exception.CustomExceptionError.FORBIDDEN;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final UserService userService;
    private final QuestionService questionService;

    private final CommentRepository commentRepository;
    private final CommenterAliasRepository commenterAliasRepository;
    private final CommenterSequenceRepository commenterSequenceRepository;

    public Page<Comment> getComments(Long questionId, Long userId, Pageable pageable) {
        QuestionEntity questionEntity = questionService.findQuestionOrException(questionId);
        return commentRepository.findWithAliasByQuestionEntityId(questionEntity.getId(), pageable)
                .map(it -> Comment.of(it, it.isCreatedBy(userId), it.getAlias()));
    }

    @Transactional
    public void createComment(Long questionId, Long userId, String content) {
        QuestionEntity questionEntity = questionService.findQuestionOrException(questionId);
        UserEntity userEntity = userService.findUserOrException(userId);
        CommenterAliasEntity commenterAliasEntity = getOrCreateCommenterAlias(userEntity, questionEntity);
        commentRepository.save(buildCommentEntity(questionEntity, userEntity, commenterAliasEntity, content));
    }

    @Transactional
    public void modifyComment(Long commentId, Long userId, String content) {
        CommentEntity comment = findCommentEntityOrException(commentId);
        ensureUserIsCommenter(comment, userId);
        comment.update(content);
        commentRepository.saveAndFlush(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommentEntity comment = findCommentEntityOrException(commentId);
        ensureUserIsCommenter(comment, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteAllByQuestionId(Long questionId) {
        commentRepository.deleteAllByQuestionId(questionId);
    }

    private CommenterAliasEntity getOrCreateCommenterAlias(UserEntity userEntity, QuestionEntity questionEntity) {
        return commentRepository.findFirstByQuestionEntityIdAndUserEntityId(questionEntity.getId(), userEntity.getId())
                .map(CommentEntity::getCommenterAliasEntity)
                .orElseGet(() -> createCommenterAlias(questionEntity, userEntity));
    }

    private CommenterAliasEntity createCommenterAlias(QuestionEntity questionEntity, UserEntity userEntity) {
        CommenterSequenceEntity commenterSequenceEntity = commenterSequenceRepository.findForUpdateByQuestionEntityId(questionEntity.getId())
                .orElseThrow(() -> new IllegalStateException("질문에 대한 댓글 작성자 순서 정보가 존재하지 않습니다."));
        int nextSequence = commenterSequenceEntity.getSequence() + 1;
        String alias = CommenterAliasPrefix.getAlias(nextSequence);
        CommenterAliasEntity commenterAliasEntity = commenterAliasRepository.save(buildCommenterAliasEntity(alias, userEntity, questionEntity));
        commenterSequenceRepository.updateSequenceByQuestionEntityId(questionEntity.getId());
        return commenterAliasEntity;
    }

    private CommentEntity buildCommentEntity(QuestionEntity questionEntity, UserEntity userEntity, CommenterAliasEntity commenterAliasEntity, String content) {
        return CommentEntity.builder()
                .content(content)
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .commenterAliasEntity(commenterAliasEntity)
                .build();
    }

    private CommenterAliasEntity buildCommenterAliasEntity(String alias, UserEntity userEntity, QuestionEntity questionEntity) {
        return CommenterAliasEntity.builder()
                .alias(alias)
                .userEntity(userEntity)
                .questionEntity(questionEntity)
                .build();
    }

    private void ensureUserIsCommenter(CommentEntity comment, Long userId) {
        if (!comment.isCreatedBy(userId)) {
            throw new CustomException(FORBIDDEN, "댓글 작성자가 아닙니다");
        }
    }

    private CommentEntity findCommentEntityOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
    }
}
