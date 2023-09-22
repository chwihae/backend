package com.chwihae.controller.user;

import com.chwihae.infra.test.AbstractMockMvcTest;
import com.chwihae.infra.support.WithTestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.INVALID_TOKEN;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/users/statistics - 성공")
    @WithTestUser
    void getUserStatistics_returnsSuccessCode() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/statistics")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("GET /api/v1/users/statistics - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getUserStatistics_returnsInvalidCode() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/statistics")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }
}