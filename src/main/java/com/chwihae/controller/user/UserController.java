package com.chwihae.controller.user;

import com.chwihae.config.security.CurrentUser;
import com.chwihae.controller.ApiResponse;
import com.chwihae.dto.user.UserContext;
import com.chwihae.dto.user.UserStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // TODO
    @GetMapping("/statistics")
    public ApiResponse<UserStatisticsResponse> getUserStatistics(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(UserStatisticsResponse.of(0, 0));
    }
}
