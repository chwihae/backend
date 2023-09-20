package com.chwihae.controller.question;

import com.chwihae.dto.question.request.QuestionCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;

import static com.chwihae.utils.TimeZone.KST;

@Component
public class QuestionValidator {
    ;
    private static final int MAX_DAYS_LIMIT = 4;

    public void verify(QuestionCreateRequest request) throws BindException {
        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime maxTime = now.plusDays(MAX_DAYS_LIMIT);

        if (request.getCloseAt().isAfter(maxTime) || request.getCloseAt().isBefore(now)) {
            String errorMessage = String.format("CloseAt time should be after %s and before %s", now, maxTime);
            BindException bindException = new BindException(request, "questionCreateRequest");
            bindException.addError(new FieldError("questionCreateRequest", "closeAt", errorMessage));
            throw bindException;
        }
    }
}
