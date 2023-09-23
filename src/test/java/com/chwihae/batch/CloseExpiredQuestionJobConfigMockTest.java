package com.chwihae.batch;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.infra.test.AbstractMockTest;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CloseExpiredQuestionJobConfigMockTest extends AbstractMockTest {

    @InjectMocks
    CloseExpiredQuestionJobConfig closeExpiredQuestionJobConfig;

    @Mock
    JobRepository jobRepository;

    @Mock
    EntityManagerFactory entityManagerFactory;

    @Mock
    PlatformTransactionManager transactionManager;

    @Mock
    QuestionRepository questionRepository;

    @Test
    @DisplayName("ItemWriter가 정상적으로 동작하면 저장을 수행한다")
    void testSuccessfulItemWriter() throws Exception {
        // given
        List<QuestionEntity> questionEntities = new ArrayList<>();
        QuestionEntity mockEntity = mock(QuestionEntity.class);
        questionEntities.add(mockEntity);

        Chunk<QuestionEntity> chunk = new Chunk<>(questionEntities);

        ItemWriter<QuestionEntity> writer = closeExpiredQuestionJobConfig.closeQuestionItemWriter();

        // when
        writer.write(chunk);

        // then
        verify(questionRepository).saveAll(any());
    }

    @Test
    @DisplayName("closeQuestionItemWrite가 DB 저장 중 문제가 발생하면 예외가 발생한다")
    void testFailedItemWriter() throws Exception {
        // given
        List<QuestionEntity> questionEntities = new ArrayList<>();
        QuestionEntity mockEntity = mock(QuestionEntity.class);
        questionEntities.add(mockEntity);

        Chunk<QuestionEntity> chunk = new Chunk<>(questionEntities);

        ItemWriter<QuestionEntity> writer = mock(ItemWriter.class);

        doThrow(new RuntimeException()).when(writer).write(any());

        //when //then
        assertThatThrownBy(() -> writer.write(any()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("closeQuestionItemReader가 정상적으로 동작해야 한다")
    void testSuccessfulItemReader() {
        // given
        JpaPagingItemReader<QuestionEntity> reader = closeExpiredQuestionJobConfig.closeQuestionItemReader();

        //when //then
        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("closeQuestionItemReader에서 EntityManager 생성 중 문제가 발생하면 예외가 발생한다")
    void testFailedItemReader() {
        // given
        JpaPagingItemReader<QuestionEntity> reader = closeExpiredQuestionJobConfig.closeQuestionItemReader();

        //when //then
        assertThatThrownBy(reader::read)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("closeQuestionStep이 정상적으로 생성되어야 한다")
    void testSuccessfulStepCreation() {
        // when
        Step result = closeExpiredQuestionJobConfig.closeQuestionStep();

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("closeQuestionJob이 정상적으로 생성되어야 한다")
    void testSuccessfulJobCreation() {
        // when
        Job result = closeExpiredQuestionJobConfig.closeQuestionJob();

        // then
        assertThat(result).isNotNull();
    }
}
