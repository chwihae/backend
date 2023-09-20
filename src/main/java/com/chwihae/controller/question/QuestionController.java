package com.chwihae.controller.question;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionCreateResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.service.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionValidator questionValidator;

    @PostMapping
    public ApiResponse<QuestionCreateResponse> createQuestion(@RequestBody @Validated QuestionCreateRequest request,
                                                              @CurrentUser UserContext userContext) throws BindException {
        questionValidator.verify(request);
        return ApiResponse.ok(questionService.createQuestionWithOptions(request, userContext.getId()));
    }
}
