package com.chwihae.batch;

import com.chwihae.service.question.core.QuestionViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SyncQuestionViewScheduler {

    private final QuestionViewService questionViewService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void syncQuestionViewCount() {
        questionViewService.syncQuestionViewCount();
    }
}
