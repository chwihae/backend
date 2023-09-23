package com.chwihae.infra.test;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBatchTest
public abstract class AbstractBatchTest extends AbstractIntegrationTest {

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;
}
