package com.chwihae.event.question;

import com.chwihae.service.question.QuestionViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Async
@RequiredArgsConstructor
@Component
public class QuestionViewEventListener {

    private final QuestionViewService questionViewService;

    @EventListener
    public void handleQuestionViewEvent(QuestionViewEvent event) {
        questionViewService.incrementViewCount(event.questionId());
    }
}
