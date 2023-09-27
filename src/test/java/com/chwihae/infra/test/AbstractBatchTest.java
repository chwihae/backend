package com.chwihae.infra.test;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBatchTest
public abstract class AbstractBatchTest extends AbstractIntegrationTest {

    @Autowired
    protected Job closeQuestionJob;

    @Autowired
    protected JobLauncher jobLauncher;

    protected JobParameters getJobParameters() {
        return new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
    }
}
