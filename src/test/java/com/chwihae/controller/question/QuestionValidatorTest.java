package com.chwihae.controller.question;

import com.chwihae.dto.question.request.QuestionCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
class QuestionValidatorTest {

    QuestionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuestionValidator();
    }

    @Test
    @DisplayName("질문 마감 시간이 올바르면 통과한다")
    void verify_success() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .closeAt(now.plusMinutes(30))
                .build();

        //when //then
        validator.verify(request);
    }

    @Test
    @DisplayName("질문 마감 시간이 현재 시간보다 앞서면 예외가 발생한다")
    void verify_whenCloseAtIsBeforeNow_throwsBindException() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .closeAt(now.minusMinutes(10))
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> validator.verify(request))
                .isInstanceOf(BindException.class)
                .satisfies(ex -> {
                    BindException bindException = (BindException) ex;
                    FieldError fieldError = bindException.getFieldError("closeAt");
                    Assertions.assertThat(fieldError).isNotNull();
                    log.info("Error Message : {}", fieldError.getDefaultMessage());
                });
    }

    @Test
    @DisplayName("질문 마감 시간이 정해진 시간보다 늦으면 예외가 발생한다")
    void verify_whenCloseAtIsAfterDeadline_throwsBindException() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .closeAt(now.plusDays(5))
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> validator.verify(request))
                .isInstanceOf(BindException.class)
                .satisfies(ex -> {
                    BindException bindException = (BindException) ex;
                    FieldError fieldError = bindException.getFieldError("closeAt");
                    Assertions.assertThat(fieldError).isNotNull();
                    log.info("Error Message : {}", fieldError.getDefaultMessage());
                });
    }
}