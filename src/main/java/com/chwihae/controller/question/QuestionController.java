package com.chwihae.controller.question;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.IdResponse;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.question.QuestionService;
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

    @PostMapping
    public ApiResponse<IdResponse> createQuestion(@RequestBody @Validated QuestionCreateRequest request,
                                                  @CurrentUser UserContext userContext) throws BindException {
        questionValidator.verify(request);
        return ApiResponse.created(IdResponse.of(questionService.createQuestionWithOptions(request, userContext.getId())));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable Long questionId,
                                                     @CurrentUser UserContext userContext) {
        return ApiResponse.ok(questionService.getQuestion(questionId, userContext.getId()));
    }
}
