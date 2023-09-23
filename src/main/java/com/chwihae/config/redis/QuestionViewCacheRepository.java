package com.chwihae.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

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

    private String getKey(Long questionId) {
        return "question:" + questionId + ":views";
    }
}
