package com.chwihae.batch.closequestion;

import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.infra.test.AbstractMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class CloseQuestionSchedulerTest extends AbstractMockTest {

    @InjectMocks
    CloseQuestionScheduler closeQuestionScheduler;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    JobLauncher jobLauncher;

    @Mock
    Job closeQuestionJob;

    @DisplayName("질문 종료 시간이 지난 경우 배치 작업 실행")
    @Test
    void closeQuestion_whenExistsQuestionClose_executeBatch() throws JobExecutionException {
        // given
        when(questionRepository.existsByCloseAtBefore(any(LocalDateTime.class))).thenReturn(true);

        // when
        closeQuestionScheduler.closeQuestion();

        // then
        verify(jobLauncher).run(eq(closeQuestionJob), any(JobParameters.class));
    }

    @DisplayName("질문 종료 시간이 아직 안 지난 경우 배치 작업을 실행하지 않음")
    @Test
    void closeQuestion_whenNotExistsQuestionClose_doNotExecuteBatch() throws JobExecutionException {
        // given
        when(questionRepository.existsByCloseAtBefore(any(LocalDateTime.class)))
                .thenReturn(false);

        // when
        closeQuestionScheduler.closeQuestion();

        // then
        verify(jobLauncher, times(0)).run(eq(closeQuestionJob), any(JobParameters.class));
    }

    @DisplayName("배치 작업 실행 중 에러 발생 시 로그를 남긴다")
    @Test
    void closeQuestion_whenExecutingBatchJobThrowsException() throws JobExecutionException {
        // given
        when(questionRepository.existsByCloseAtBefore(any(LocalDateTime.class))).
                thenReturn(true);

        doThrow(JobInstanceAlreadyCompleteException.class)
                .when(jobLauncher)
                .run(any(), any(JobParameters.class));

        // when // then
        closeQuestionScheduler.closeQuestion();
    }
}
