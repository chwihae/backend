package com.chwihae.controller.question;

import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.infra.MockMvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.chwihae.exception.CustomExceptionError.INVALID_ARGUMENT;
import static com.chwihae.exception.CustomExceptionError.INVALID_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class QuestionControllerTest extends MockMvcTestSupport {

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (질문 마감 시간이 유효하지 않은 경우)")
    void createQuestion_withInvalidCloseAt_returnsInvalidParameter() throws Exception {
        //given
        UserEntity userEntity = createUser("test@email.com");

        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1);

        List<OptionCreateRequest> options = new ArrayList<>();
        for (int optionName = 0; optionName < 2; optionName++) {
            options.add(OptionCreateRequest.builder()
                    .name(String.valueOf(optionName))
                    .build()
            );
        }

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(QuestionType.SPEC)
                .closeAt(closeAt)
                .content("content")
                .options(options)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .header(AUTHORIZATION, token(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (유효하지 않은 요청 파라미터)")
    void createQuestion_withInvalidParameter_returnsInvalidParameter() throws Exception {
        //given
        UserEntity userEntity = createUser("test@email.com");
        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .header(AUTHORIZATION, token(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (인증 토큰 없이)")
    void createQuestion_withInvalidToken_returnsInvalidToken() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/questions - 실패 (인증 토큰 없이)")
    void createQuestion_withoutToken_returnsInvalidToken() throws Exception {
        //when //then
        mockMvc.perform(
                        post("/api/v1/questions")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }
}