package com.chwihae.batch.syncquestionview;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.infra.test.AbstractMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SyncQuestionViewJobConfigStepMockTest extends AbstractMockTest {

    @InjectMocks
    SyncQuestionViewJobConfig syncQuestionViewJobConfig;

    @Mock
    JobRepository mockJobRepository;

    @Mock
    RedisTemplate<String, Long> mockQuestionViewRedisTemplate;

    @Mock
    SyncQuestionViewItemReader syncQuestionViewItemReader;

    @Mock
    QuestionViewRepository mockQuestionViewRepository;

    @Mock
    QuestionViewCacheRepository mockQuestionViewCacheRepository;

    @Mock
    PlatformTransactionManager mockTransactionManager;

    @Test
    @DisplayName("올바르게 syncQuestionViewStep를 구성한다")
    void syncQuestionViewStepCreationTest() {
        //when
        Step resultStep = syncQuestionViewJobConfig.syncQuestionViewStep();

        //then
        assertThat(resultStep).isNotNull();
        assertThat(resultStep.getName()).isEqualTo("syncQuestionViewStep");
    }

    @Test
    @DisplayName("올바르게 syncQuestionViewItemReader를 생성한다")
    void syncQuestionViewItemReader() {
        //when
        ItemReader<QuestionViewResponse> resultReader = syncQuestionViewJobConfig.syncQuestionViewItemReader();

        //then
        assertThat(resultReader).isNotNull();
    }

    @Test
    @DisplayName("캐시에서 데이터를 읽는 도중 실패하면 예외가 발생한다")
    void read_whenCachingError_throwException() throws Exception {
        //given
        final int chunkSize = SyncQuestionViewJobConfig.CHUNK_SIZE;
        Cursor<String> mockCursor = mock(Cursor.class);
        when(mockCursor.hasNext()).thenThrow(new RuntimeException("Redis read error"));
        when(mockQuestionViewRedisTemplate.scan(any(ScanOptions.class))).thenReturn(mockCursor);

        SyncQuestionViewItemReader itemReader = new SyncQuestionViewItemReader(mockQuestionViewRedisTemplate, mockQuestionViewCacheRepository, chunkSize);

        //when //then
        Assertions.assertThatThrownBy(itemReader::read)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis read error");
    }

    @Test
    @DisplayName("캐싱되어 있는 데이터를 DB에 저장하는 도중 실패하면 예외가 발생한다")
    void write_whenDBExceptionOccur_throwException() {
        // Given
        Long testQuestionId = 1L;
        Long viewCount = 5L;
        QuestionViewResponse cachedResponse = new QuestionViewResponse(testQuestionId, viewCount);
        List<QuestionViewResponse> items = Collections.singletonList(cachedResponse);

        doThrow(new RuntimeException("Database write error")).when(mockQuestionViewRepository).saveAll(anyList());

        // When & Then
        Assertions.assertThatThrownBy(() -> syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(items)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database write error");
    }

    @Test
    @DisplayName("캐싱되어있는 질문 조회수 데이터 수가 청크 사이즈보다 작을 때 DB에 올바르게 동기화된다")
    void batch_whenChunkSizeLessThan20() throws Exception {
        // Given
        final int size = SyncQuestionViewJobConfig.CHUNK_SIZE - 1;
        List<QuestionViewResponse> chunk = createMockQuestionView(size);

        // When
        syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(chunk));

        // Then
        verify(mockQuestionViewRepository, times(1)).saveAll(anyList());
        assertThat(chunk).hasSize(19);
    }

    @Test
    @DisplayName("캐싱되어있는 질문 조회수 데이터 수가 청크 사이즈보다 클 때 DB에 올바르게 동기화된다")
    void batch_whenChunkSizeMoreThenChuckSize() throws Exception {
        // Given
        final int size = SyncQuestionViewJobConfig.CHUNK_SIZE + 1;
        List<QuestionViewResponse> chunk = createMockQuestionView(size);

        // When
        syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(chunk));

        // Then
        verify(mockQuestionViewRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("캐시 데이터가 비어 있는 경우 업데이트를 하지 않는다")
    void write_whenSourcesAreEmpty_noActionsArePerformed() throws Exception {
        // When
        syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(Collections.emptyList()));

        // Then
        verify(mockQuestionViewRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("캐시에서 읽어온 조회수가 DB의 조회수보다 클 때만 DB에 반영된다")
    void write_onlyIncrementsViewCount() throws Exception {
        // Given
        Long testQuestionId = 1L;
        Long initialViewCount = 5L;
        Long newViewCount = 10L;

        QuestionEntity questionEntity = mock(QuestionEntity.class);
        when(questionEntity.getId()).thenReturn(testQuestionId);
        QuestionViewResponse cachedResponse = new QuestionViewResponse(testQuestionId, newViewCount);
        QuestionViewEntity existingEntity = QuestionViewEntity.builder().questionEntity(questionEntity).build();

        when(mockQuestionViewRepository.findByQuestionEntityIds(anyList())).thenReturn(Collections.singletonList(existingEntity));

        // When
        syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(Collections.singletonList(cachedResponse)));

        // Then
        assertThat(existingEntity.getViewCount()).isEqualTo(newViewCount);
    }

    @Test
    @DisplayName("캐시에서 읽어온 조회수가 DB의 조회수보다 작거나 같을 때는 DB에 반영되지 않는다")
    void write_doesNotDecrementOrKeepSameViewCount() throws Exception {
        // Given
        Long testQuestionId = 1L;
        Long initialViewCount = 10L;
        Long newViewCount = 5L;

        QuestionEntity questionEntity = mock(QuestionEntity.class);
        when(questionEntity.getId()).thenReturn(testQuestionId);
        QuestionViewResponse cachedResponse = new QuestionViewResponse(testQuestionId, newViewCount);
        QuestionViewEntity existingEntity = QuestionViewEntity.builder().questionEntity(questionEntity).build();
        existingEntity.setViewCount(initialViewCount);

        when(mockQuestionViewRepository.findByQuestionEntityIds(anyList())).thenReturn(Collections.singletonList(existingEntity));

        // When
        syncQuestionViewJobConfig.syncQuestionViewItemWriter().write(new Chunk<>(Collections.singletonList(cachedResponse)));

        // Then
        assertThat(existingEntity.getViewCount()).isEqualTo(initialViewCount);
    }

    private List<QuestionViewResponse> createMockQuestionView(int size) {
        List<QuestionViewResponse> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new QuestionViewResponse((long) i, (long) i));
        }
        return list;
    }
}
