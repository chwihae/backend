package com.chwihae.controller.question;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.dto.comment.request.QuestionCommentCreateRequest;
import com.chwihae.dto.common.response.IdResponse;
import com.chwihae.dto.option.response.VoteOptionResponse;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.comment.CommentService;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionValidator questionValidator;
    private final VoteService voteService;
    private final CommentService commentService;

    @GetMapping
    public ApiResponse<Page<QuestionListResponse>> getQuestions(@RequestParam(value = "type", required = false) QuestionType type,
                                                                @RequestParam(value = "status", required = false) QuestionStatus status,
                                                                @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable) {
        return ApiResponse.ok(questionService.getQuestionsByTypeAndStatus(type, status, pageable));
    }

    @PostMapping
    public ApiResponse<IdResponse> createQuestion(@RequestBody @Validated QuestionCreateRequest request,
                                                  @CurrentUser UserContext userContext) throws BindException {
        questionValidator.verify(request);
        return ApiResponse.created(IdResponse.of(questionService.createQuestion(request, userContext.getId())));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<QuestionDetailResponse> getQuestion(@PathVariable Long questionId,
                                                           @CurrentUser UserContext userContext) {
        return ApiResponse.ok(questionService.getQuestion(questionId, userContext.getId()));
    }

    @GetMapping("/{questionId}/options")
    public ApiResponse<VoteOptionResponse> getOptions(@PathVariable Long questionId,
                                                      @CurrentUser UserContext userContext) {
        return ApiResponse.ok(voteService.getVoteOptions(questionId, userContext.getId()));
    }

    @PostMapping("/{questionId}/comments")
    public ApiResponse<VoteOptionResponse> createComment(@PathVariable Long questionId,
                                                         @RequestBody @Validated QuestionCommentCreateRequest request,
                                                         @CurrentUser UserContext userContext) {
        commentService.createComment(questionId, userContext.getId(), request.getContent());
        return ApiResponse.created();
    }

    @PostMapping("/{questionId}/options/{optionId}")
    public ApiResponse<Void> createVote(@PathVariable Long questionId,
                                        @PathVariable Long optionId,
                                        @CurrentUser UserContext userContext) {
        voteService.createVote(questionId, optionId, userContext.getId());
        return ApiResponse.created();
    }

    @DeleteMapping("/{questionId}/options/{optionId}")
    public ApiResponse<Void> deleteVote(@PathVariable Long questionId,
                                        @PathVariable Long optionId,
                                        @CurrentUser UserContext userContext) {
        voteService.deleteVote(questionId, optionId, userContext.getId());
        return ApiResponse.ok();
    }
}
