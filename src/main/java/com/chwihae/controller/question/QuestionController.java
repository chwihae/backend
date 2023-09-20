package com.chwihae.controller.question;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.IdResponse;
import com.chwihae.dto.option.response.OptionVoteResponse;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionValidator questionValidator;
    private final VoteService voteService;

    @PostMapping
    public ApiResponse<IdResponse> createQuestion(@RequestBody @Validated QuestionCreateRequest request,
                                                  @CurrentUser UserContext userContext) throws BindException {
        questionValidator.verify(request);
        return ApiResponse.created(IdResponse.of(questionService.createQuestion(request, userContext.getId())));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable Long questionId,
                                                     @CurrentUser UserContext userContext) {
        return ApiResponse.ok(questionService.getQuestion(questionId, userContext.getId()));
    }

    @GetMapping("/{questionId}/options")
    public ApiResponse<OptionVoteResponse> getOptions(@PathVariable Long questionId,
                                                      @CurrentUser UserContext userContext) {
        return ApiResponse.ok(voteService.getOptions(questionId, userContext.getId()));
    }

    @PostMapping("/{questionId}/options/{optionId}")
    public ApiResponse<Void> createVote(@PathVariable Long questionId,
                                        @PathVariable Long optionId,
                                        @CurrentUser UserContext userContext) {
        return ApiResponse.ok();
    }

    @DeleteMapping("/{questionId}/options/{optionId}")
    public ApiResponse<Void> deleteVote(@PathVariable Long questionId,
                                        @PathVariable Long optionId,
                                        @CurrentUser UserContext userContext) {
        return ApiResponse.ok();
    }
}
