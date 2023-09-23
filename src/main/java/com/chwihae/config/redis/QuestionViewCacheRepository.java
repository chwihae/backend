package com.chwihae.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class QuestionViewCacheRepository {

    private static final Duration VIEW_CACHE_TTL = Duration.ofDays(1);
    private final RedisTemplate<String, Integer> questionViewRedisTemplate;

    public Integer setQuestionView(Long questionId, Integer viewCount) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().set(key, viewCount, VIEW_CACHE_TTL);
        return viewCount;
    }

    public Optional<Integer> getQuestionView(Long questionId) {
        String key = getKey(questionId);
        Integer viewCount = questionViewRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(viewCount);
    }

    public void incrementViewCount(Long questionId) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().increment(key, 1);
    }

    public boolean existsByKey(Long questionId) {
        String key = getKey(questionId);
        return questionViewRedisTemplate.hasKey(key);
    }

    public String getKey(Long questionId) {
        return "question:" + questionId + ":views";
    }

    public Set<String> findAllQuestionViewKeys() {
        return questionViewRedisTemplate.keys("question:*:views");
    }

    public void deleteKey(String key) {
        questionViewRedisTemplate.delete(key);
    }
}
