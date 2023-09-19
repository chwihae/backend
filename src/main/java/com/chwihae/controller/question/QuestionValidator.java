package com.chwihae.controller.question;

import com.chwihae.dto.question.request.QuestionCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class QuestionValidator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MAX_DAYS_LIMIT = 4;

    public void verify(QuestionCreateRequest request) throws BindException {
        LocalDateTime now = LocalDateTime.now(KST);

        if (request.getCloseAt().isAfter(now.plusDays(MAX_DAYS_LIMIT)) || request.getCloseAt().isBefore(now)) {
            BindException bindException = new BindException(request, "questionCreateRequest");
            bindException.addError(new FieldError("questionCreateRequest", "closeAt", "CloseAt time should be after now and before now plus " + (MAX_DAYS_LIMIT - 1) + " days."));
            throw bindException;
        }
    }
}
