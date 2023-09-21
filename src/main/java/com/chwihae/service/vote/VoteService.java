package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.option.response.VoteOptionResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.chwihae.exception.CustomExceptionError.*;
import static com.chwihae.utils.TimeZone.KST;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class VoteService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final VoteRepository voteRepository;
    private final PlatformTransactionManager transactionManager;

    public VoteOptionResponse getVoteOptions(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        if (canUserViewVoteResults(questionEntity, userId)) {
            return VoteOptionResponse.of(true, optionRepository.findWithVoteCountByQuestionEntityId(questionId));
        }
        return VoteOptionResponse.of(false, optionRepository.findWithoutVoteCountByQuestionEntityId(questionId));
    }

    @Transactional
    public void createVote(Long questionId, Long optionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        ensureQuestionIsNotClosed(questionEntity);
        ensureQuestionerCannotVote(questionEntity, userId);
        ensureUserHasNotVoted(questionId, userId);

        OptionEntity optionEntity = findOptionOrException(optionId);
        UserEntity userEntity = findUserOrException(userId);
        saveVoteOrException(questionEntity, optionEntity, userEntity);
    }

    private boolean canUserViewVoteResults(QuestionEntity questionEntity, Long userId) {
        return LocalDateTime.now(KST).isAfter(questionEntity.getCloseAt()) ||
                questionEntity.isCreatedBy(userId) ||
                voteRepository.existsByQuestionEntityIdAndUserEntityId(questionEntity.getId(), userId);
    }

    private void ensureQuestionerCannotVote(QuestionEntity questionEntity, Long userId) {
        if (questionEntity.isCreatedBy(userId)) {
            throw new CustomException(FORBIDDEN, "질문자는 본인의 질문에 투표할 수 없습니다");
        }
    }

    private void ensureQuestionIsNotClosed(QuestionEntity questionEntity) {
        if (questionEntity.isClosed()) {
            throw new CustomException(QUESTION_CLOSED);
        }
    }

    private void ensureUserHasNotVoted(Long questionId, Long userId) {
        if (voteRepository.existsByQuestionEntityIdAndUserEntityId(questionId, userId)) {
            throw new CustomException(DUPLICATE_VOTE);
        }
    }

    private boolean checkUserAlreadyVotedInNewTransaction(Long questionId, Long userId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return Objects.equals(Boolean.TRUE, transactionTemplate.execute(status -> voteRepository.existsByQuestionEntityIdAndUserEntityId(questionId, userId)));
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }

    private OptionEntity findOptionOrException(Long optionId) {
        return optionRepository.findById(optionId).orElseThrow(() -> new CustomException(OPTION_NOT_FOUND));
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private void saveVoteOrException(QuestionEntity questionEntity, OptionEntity optionEntity, UserEntity userEntity) {
        try {
            voteRepository.save(buildVoteEntity(questionEntity, optionEntity, userEntity));
        } catch (DataIntegrityViolationException ex) {
            if (checkUserAlreadyVotedInNewTransaction(questionEntity.getId(), userEntity.getId())) {
                throw new CustomException(DUPLICATE_VOTE);
            }
            throw ex;
        }
    }

    private VoteEntity buildVoteEntity(QuestionEntity questionEntity, OptionEntity optionEntity, UserEntity userEntity) {
        return VoteEntity.builder()
                .questionEntity(questionEntity)
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}
