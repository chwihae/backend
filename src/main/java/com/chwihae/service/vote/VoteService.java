package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.option.response.OptionVoteResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static com.chwihae.utils.TimeZone.KST;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class VoteService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final VoteRepository voteRepository;

    public OptionVoteResponse getOptions(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        if (canUserViewVoteResults(questionId, userId, questionEntity)) {
            return OptionVoteResponse.of(true, optionRepository.findWithVoteCountByQuestionEntityId(questionId));
        }
        return OptionVoteResponse.of(false, optionRepository.findWithoutVoteCountByQuestionEntityId(questionId));
    }

    private boolean canUserViewVoteResults(Long questionId, Long userId, QuestionEntity questionEntity) {
        return LocalDateTime.now(KST).isAfter(questionEntity.getCloseAt()) ||
                questionEntity.isCreatedBy(userId) ||
                voteRepository.existsByQuestionEntityIdAndUserEntityId(questionId, userId);
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }
}
