package com.chwihae.service.question;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.event.question.QuestionViewEvent;
import com.chwihae.exception.CustomException;
import com.chwihae.service.bookmark.BookmarkService;
import com.chwihae.service.comment.CommentService;
import com.chwihae.service.commenter.CommenterSequenceService;
import com.chwihae.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CommenterSequenceService commenterSequenceService;
    private final CommentService commentService;
    private final VoteService voteService;
    private final BookmarkService bookmarkService;
    private final ApplicationEventPublisher eventPublisher;

    public Page<QuestionListResponse> getQuestionsByTypeAndStatus(QuestionType type, QuestionStatus status, Pageable pageable) {
        return questionRepository.findByTypeAndStatusWithCounts(status, type, pageable);
    }

    @Transactional
    public Long createQuestion(QuestionCreateRequest request, Long userId) {
        UserEntity userEntity = findUserOrException(userId);
        QuestionEntity questionEntity = questionRepository.save(request.toEntity(userEntity));
        optionRepository.saveAll(buildOptionEntities(request.getOptions(), questionEntity));
        commenterSequenceService.createCommenterSequence(questionEntity);
        return questionEntity.getId();
    }

    public QuestionDetailResponse getQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        eventPublisher.publishEvent(new QuestionViewEvent(questionId));
        boolean bookmarked = bookmarkService.isBookmarked(questionId, userId);
        long bookmarkCount = bookmarkService.getBookmarkCount(questionId);
        long voteCount = voteService.getVoteCount(questionId);
        long commentCount = commentService.getCommentCount(questionId);
        boolean isEditable = questionEntity.isCreatedBy(userId);
        return QuestionDetailResponse.of(questionEntity, bookmarkCount, commentCount, voteCount, bookmarked, isEditable);
    }

    private List<OptionEntity> buildOptionEntities(List<OptionCreateRequest> options, QuestionEntity questionEntity) {
        return options.stream()
                .map(option -> OptionEntity.builder()
                        .questionEntity(questionEntity)
                        .name(option.getName())
                        .build())
                .toList();
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }
}
