package com.chwihae.service.comment;

import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.commenter.*;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CommentRepository commentRepository;
    private final CommenterAliasRepository commenterAliasRepository;
    private final CommenterSequenceRepository commenterSequenceRepository;

    @Transactional
    public void createComment(Long questionId, Long userId, String content) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        UserEntity userEntity = findUserOrException(userId);
        CommenterAliasEntity commenterAliasEntity = getOrCreateCommenterAlias(userEntity, questionEntity);
        commentRepository.save(buildCommentEntity(questionEntity, userEntity, commenterAliasEntity, content));
    }

    private CommenterAliasEntity getOrCreateCommenterAlias(UserEntity userEntity, QuestionEntity questionEntity) {
        return commentRepository.findTopCommentByQuestionIdAndUserId(questionEntity.getId(), userEntity.getId())
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

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }

}
