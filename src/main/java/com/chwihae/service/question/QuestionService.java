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
import com.chwihae.dto.question.response.QuestionViewResponse;
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
import java.util.Objects;

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
    private final QuestionViewService questionViewService;
    private final ApplicationEventPublisher eventPublisher;

    // TODO test
    public Page<QuestionListResponse> getQuestionsByTypeAndStatus(QuestionType type, QuestionStatus status, Pageable pageable) {
        Page<QuestionListResponse> page = questionRepository.findByTypeAndStatusWithCounts(status, type, pageable); // 1. Find page from DB
        List<QuestionViewResponse> allViewCounts = findAllQuestionViewCounts(page.getContent()); // 2. Get question view from cache and DB
        setPageViewCounts(page.getContent(), allViewCounts); // 3. Set question views for each page element
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

    @Transactional
    public Long createQuestion(QuestionCreateRequest request, Long userId) {
        UserEntity userEntity = findUserOrException(userId);
        QuestionEntity questionEntity = questionRepository.save(request.toEntity(userEntity));
        optionRepository.saveAll(buildOptionEntities(request.getOptions(), questionEntity));
        commenterSequenceService.createCommenterSequence(questionEntity);
        questionViewService.createQuestionView(questionEntity);
        return questionEntity.getId();
    }

    public QuestionDetailResponse getQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        eventPublisher.publishEvent(new QuestionViewEvent(questionId));
        return buildQuestionDetailResponse(questionId, userId, questionEntity);
    }

    private QuestionDetailResponse buildQuestionDetailResponse(Long questionId, Long userId, QuestionEntity questionEntity) {
        boolean bookmarked = bookmarkService.isBookmarked(questionId, userId);
        long viewCount = questionViewService.getViewCount(questionId);
        int bookmarkCount = bookmarkService.getBookmarkCount(questionId);
        int voteCount = voteService.getQuestionVoteCount(questionId);
        int commentCount = commentService.getQuestionCommentCount(questionId);
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
