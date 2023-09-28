package com.chwihae.batch.syncquestionview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SyncQuestionViewScheduler {

    private final Job syncQuestionViewJob;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 */10 * * * ?")
    public void syncQuestionViewCount() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Sync question view count job started");
            jobLauncher.run(syncQuestionViewJob, jobParameters);
            log.info("Sync question view count job finished");
        } catch (JobExecutionException ex) {
            log.error("Error executing syncQuestionViewCount job", ex);
        }
    }
}
