package com.chwihae.service.question.core;

import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.commenter.CommenterAliasRepository;
import com.chwihae.domain.commenter.CommenterSequenceRepository;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.FORBIDDEN;
import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestionDeletionService {

    private final QuestionViewRepository questionViewRepository;
    private final CommenterSequenceRepository commenterSequenceRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommenterAliasRepository commenterAliasRepository;

    public void deleteQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        ensureQuestionIsClosed(questionEntity);
        ensureUserIsQuestioner(questionEntity, userId);

        voteRepository.deleteAllByQuestionId(questionId); // vote
        optionRepository.deleteAllByQuestionId(questionId); // option
        bookmarkRepository.deleteAllByQuestionId(questionId); // bookmark
        commenterSequenceRepository.deleteAllByQuestionId(questionId); // commenter sequence
        questionViewRepository.deleteAllByQuestionId(questionId); // question view
        commenterAliasRepository.deleteAllByQuestionId(questionId); // commenter alias
        commentRepository.deleteAllByQuestionId(questionId); // comment
        questionRepository.delete(questionEntity);
    }


    private void ensureUserIsQuestioner(QuestionEntity questionEntity, Long userId) {
        if (!questionEntity.isCreatedBy(userId)) {
            throw new CustomException(FORBIDDEN, "질문 작성자가 아니면 질문을 삭제할 수 없습니다");
        }
    }

    private void ensureQuestionIsClosed(QuestionEntity questionEntity) {
        if (!questionEntity.isClosed()) {
            throw new CustomException(FORBIDDEN, "마감되지 않은 질문은 삭제할 수 없습니다");
        }
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }
}
