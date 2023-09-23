package com.chwihae.controller.user;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.dto.user.UserStatisticsResponse;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/statistics")
    public ApiResponse<UserStatisticsResponse> getUserStatistics(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.getUserStatistics(userContext.getId()));
    }

    @GetMapping("/questions")
    public ApiResponse<Page<QuestionListResponse>> getUserQuestions(@RequestParam("type") UserQuestionFilterType type,
                                                                    @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable,
                                                                    @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.getUserQuestions(userContext.getId(), type, pageable));
    }
}
