package com.chwihae.service.question.query;

import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.event.question.QuestionViewEvent;
import com.chwihae.exception.CustomException;
import com.chwihae.service.question.core.QuestionViewService;
import com.chwihae.service.question.strategy.UserQuestionsFilterStrategyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionQueryService {

    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final QuestionRepository questionRepository;
    private final QuestionViewService questionViewService;
    private final UserQuestionsFilterStrategyProvider questionsFilterStrategyProvider;
    private final ApplicationEventPublisher eventPublisher;

    public Page<QuestionListResponse> getQuestionsByTypeAndStatus(QuestionType type, QuestionStatus status, Pageable pageable) {
        Page<QuestionListResponse> page = questionRepository.findByTypeAndStatusWithCounts(status, type, pageable);
        List<QuestionViewResponse> allViewCounts = findAllQuestionViewCounts(page.getContent());
        setPageViewCounts(page.getContent(), allViewCounts);
        return page;
    }

    public Page<QuestionListResponse> getUserQuestions(Long userId, UserQuestionFilterType type, Pageable pageable) {
        Page<QuestionListResponse> page = questionsFilterStrategyProvider.getFilter(type).filter(userId, pageable);
        List<QuestionViewResponse> allViewCounts = findAllQuestionViewCounts(page.getContent());
        setPageViewCounts(page.getContent(), allViewCounts);
        return page;
    }

    private List<QuestionViewResponse> findAllQuestionViewCounts(List<QuestionListResponse> content) {
        List<Long> questionIds = content.stream()
                .map(QuestionListResponse::getId)
                .distinct().toList();
        return questionViewService.getViewCounts(questionIds);
    }

    private void setPageViewCounts(List<QuestionListResponse> content, List<QuestionViewResponse> allViewCounts) {
        content.forEach(it ->
                allViewCounts.stream()
                        .filter(view -> Objects.equals(view.getQuestionId(), it.getId()))
                        .findFirst()
                        .ifPresent(view -> it.setViewCount(view.getViewCount()))
        );
    }

    public QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }

    public QuestionDetailResponse getQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        eventPublisher.publishEvent(new QuestionViewEvent(questionId));
        return buildQuestionDetailResponse(questionId, userId, questionEntity);
    }

    private QuestionDetailResponse buildQuestionDetailResponse(Long questionId, Long userId, QuestionEntity questionEntity) {
        boolean bookmarked = bookmarkRepository.existsByQuestionEntityIdAndUserEntityId(questionId, userId);
        long viewCount = questionViewService.getViewCount(questionId);
        int bookmarkCount = bookmarkRepository.countByQuestionEntityId(questionId);
        int voteCount = voteRepository.countByQuestionEntityId(questionId);
        int commentCount = commentRepository.countByQuestionEntityId(questionId);
        boolean isEditable = questionEntity.isCreatedBy(userId);

        return QuestionDetailResponse.of(
                questionEntity,
                viewCount,
                bookmarkCount,
                commentCount,
                voteCount,
                bookmarked,
                isEditable
        );
    }
}
