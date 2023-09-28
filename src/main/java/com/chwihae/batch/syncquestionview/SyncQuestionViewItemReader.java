package com.chwihae.batch.syncquestionview;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.Closeable;
import java.util.Optional;

@RequiredArgsConstructor
public class SyncQuestionViewItemReader implements ItemReader<QuestionViewResponse>, Closeable {

    private final RedisTemplate<String, Long> questionViewRedisTemplate;
    private final QuestionViewCacheRepository questionViewCacheRepository;
    private final int batchSize;
    private Cursor<String> cursor = null;

    @Override
    public QuestionViewResponse read() throws Exception {

        if (cursor == null) {
            ScanOptions scanOptions = ScanOptions.scanOptions().match(QuestionViewCacheRepository.KEY_PATTERN).count(batchSize).build();
            cursor = questionViewRedisTemplate.scan(scanOptions);
        }

        while (cursor.hasNext()) {
            String key = cursor.next();
            Optional<Long> questionIdOpt = questionViewCacheRepository.extractQuestionIdFromKey(key);
            if (questionIdOpt.isEmpty()) {
                continue;
            }

            Long questionId = questionIdOpt.get();
            Optional<Long> viewCountOpt = questionViewCacheRepository.getViewCount(questionId);
            if (viewCountOpt.isEmpty()) {
                continue;
            }

            Long viewCount = viewCountOpt.get();
            return new QuestionViewResponse(questionId, viewCount);
        }

        close();

        return null;
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
