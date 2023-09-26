package com.chwihae.config.redis;

import com.chwihae.dto.question.response.QuestionViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Repository
public class QuestionViewCacheRepository {

    private static final Duration VIEW_CACHE_TTL = Duration.ofDays(1);
    private final RedisTemplate<String, Long> questionViewRedisTemplate;

    public Long setQuestionView(Long questionId, Long viewCount) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().set(key, viewCount, VIEW_CACHE_TTL);
        return viewCount;
    }

    // TODO test -
    public List<QuestionViewResponse> getQuestionViews(List<Long> questionIds) {
        if (Objects.isNull(questionIds) || questionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> existingKeys = questionIds.stream()
                .map(this::getKey)
                .distinct()
                .filter(questionViewRedisTemplate::hasKey)
                .toList();

        List<Long> viewCounts = questionViewRedisTemplate.opsForValue().multiGet(existingKeys);

        if (Objects.isNull(viewCounts) || viewCounts.isEmpty()) {
            return new ArrayList<>();
        }

        return IntStream.range(0, existingKeys.size())
                .mapToObj(it -> {
                    Long questionId = extractQuestionIdFromKey(existingKeys.get(it));
                    Long viewCount = Optional.ofNullable(viewCounts.get(it)).orElse(0L);
                    return new QuestionViewResponse(questionId, viewCount);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Optional<Long> getQuestionView(Long questionId) {
        String key = getKey(questionId);
        Long viewCount = questionViewRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(viewCount);
    }

    public void incrementViewCount(Long questionId) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().increment(key, 1);
    }

    public boolean existsByKey(Long questionId) {
        String key = getKey(questionId);
        return Boolean.TRUE.equals(questionViewRedisTemplate.hasKey(key));
    }

    public void clear() {
        Set<String> keys = questionViewRedisTemplate.keys("question:*:views");
        if (keys != null && !keys.isEmpty()) {
            questionViewRedisTemplate.delete(keys);
        }
    }

    private Long extractQuestionIdFromKey(String key) {
        String[] parts = key.split(":");
        return Long.parseLong(parts[1]);
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
