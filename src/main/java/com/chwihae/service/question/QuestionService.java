package com.chwihae.service.question;

import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.commenter.CommenterAliasRepository;
import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.commenter.CommenterSequenceRepository;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.event.question.QuestionViewEvent;
import com.chwihae.exception.CustomException;
import com.chwihae.service.user.UserService;
import com.chwihae.service.user.question.UserQuestionsFilterStrategyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.chwihae.exception.CustomExceptionError.FORBIDDEN;
import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final UserService userService;
    private final QuestionViewService questionViewService;

    private final CommenterSequenceRepository commenterSequenceRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommenterAliasRepository commenterAliasRepository;
    private final UserQuestionsFilterStrategyProvider questionsFilterStrategyProvider;
    private final ApplicationEventPublisher eventPublisher;

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
        UserEntity userEntity = userService.findUserOrException(userId);
        QuestionEntity questionEntity = questionRepository.save(request.toEntity(userEntity));
        optionRepository.saveAll(buildOptionEntities(request.getOptions(), questionEntity));
        commenterSequenceRepository.save(buildCommenterSequence(questionEntity));
        questionViewService.createQuestionView(questionEntity);
        return questionEntity.getId();
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        ensureQuestionIsClosed(questionEntity);
        ensureUserIsQuestioner(questionEntity, userId);
        deleteQuestion(questionId, questionEntity);
    }


    public Page<QuestionListResponse> getUserQuestions(Long userId, UserQuestionFilterType type, Pageable pageable) {
        Page<QuestionListResponse> page = questionsFilterStrategyProvider.getFilter(type).filter(userId, pageable); // 1. Find page from DB
        List<QuestionViewResponse> allViewCounts = findAllQuestionViewCounts(page.getContent()); // 2. Get question view from cache and DB
        setPageViewCounts(page.getContent(), allViewCounts); // 3. Set question views for each page element
        return page;
    }

    public QuestionDetailResponse getQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        eventPublisher.publishEvent(new QuestionViewEvent(questionId));
        return buildQuestionDetailResponse(questionId, userId, questionEntity);
    }

    private void deleteQuestion(Long questionId, QuestionEntity questionEntity) {
        voteRepository.deleteAllByQuestionId(questionId); // vote
        optionRepository.deleteAllByQuestionId(questionId); // option
        bookmarkRepository.deleteAllByQuestionId(questionId); // bookmark
        commenterSequenceRepository.deleteAllByQuestionId(questionId); // commenter sequence
        questionViewService.deleteAllByQuestionId(questionId); // question view
        commenterAliasRepository.deleteAllByQuestionId(questionId); // commenter alias
        commentRepository.deleteAllByQuestionId(questionId); // comment
        questionRepository.delete(questionEntity);
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

    public QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }

    private List<OptionEntity> buildOptionEntities(List<OptionCreateRequest> options, QuestionEntity questionEntity) {
        return options.stream()
                .map(option -> OptionEntity.builder()
                        .questionEntity(questionEntity)
                        .name(option.getName())
                        .build())
                .toList();
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

    private CommenterSequenceEntity buildCommenterSequence(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}
