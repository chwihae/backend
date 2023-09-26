package com.chwihae.service.user;

import com.chwihae.config.redis.UserContextCacheRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserLevel;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.dto.user.UserStatisticsResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.service.comment.CommentService;
import com.chwihae.service.question.QuestionViewService;
import com.chwihae.service.user.question.UserQuestionsFilterStrategyProvider;
import com.chwihae.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final CommentService commentService;
    private final VoteService voteService;
    private final UserRepository userRepository;
    private final UserContextCacheRepository userContextCacheRepository;
    private final QuestionViewService questionViewService;
    private final UserQuestionsFilterStrategyProvider questionsFilterStrategyProvider;

    @Transactional
    public UserEntity createUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }

    // TODO test
    public Page<QuestionListResponse> getUserQuestions(Long userId, UserQuestionFilterType type, Pageable pageable) {
        Page<QuestionListResponse> page = questionsFilterStrategyProvider.getFilter(type).filter(userId, pageable); // 1. Find page from DB
        List<QuestionViewResponse> allViewCounts = findAllQuestionViewCounts(page.getContent()); // 2. Get question view from cache and DB
        setPageViewCounts(page.getContent(), allViewCounts); // 3. Set question views for each page element
        return page;
    }

    public UserContext getUserContextOrException(Long userId) {
        Optional<UserContext> userContextOpt = userContextCacheRepository.getUserContext(userId);
        return userContextOpt
                .orElseGet(() -> userRepository.findById(userId)
                        .map(userEntity -> userContextCacheRepository.setUserContext(UserContext.fromEntity(userEntity)))
                        .orElseThrow(() -> {
                            log.warn("Error in method [getUserContextOrException] - User not found with ID: {}", userId);
                            return new CustomException(USER_NOT_FOUND);
                        }));
    }

    public UserStatisticsResponse getUserStatistics(Long userId) {
        int commentCount = commentService.getUserCommentCount(userId);
        int voteCount = voteService.getUserVoteCount(userId);
        UserLevel userLevel = UserLevel.getLevel(voteCount, commentCount);
        return UserStatisticsResponse.of(userLevel, commentCount, voteCount);
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
}
