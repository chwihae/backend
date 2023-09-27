package com.chwihae.config.redis;

import com.chwihae.dto.question.response.QuestionViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Repository
public class QuestionViewCacheRepository {

    public static final String KEY_FORMAT = "question:%d:views";
    public static final String KEY_PATTERN = "question:*:views";
    public static final String KEY_PATTERN_REGEX = "^question:(\\d+):views$";
    private static final Duration CACHE_TTL = Duration.ofDays(1);
    private final RedisTemplate<String, Long> questionViewRedisTemplate;

    public Long setViewCount(Long questionId, Long viewCount) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().set(key, viewCount, CACHE_TTL);
        return viewCount;
    }

    public Optional<Long> getViewCount(Long questionId) {
        String key = getKey(questionId);
        Long viewCount = questionViewRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(viewCount);
    }

    public List<QuestionViewResponse> getViewCounts(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return new ArrayList<>();
        }

        List<String> existingKeys = getExistingKeys(questionIds);
        List<Long> viewCounts = questionViewRedisTemplate.opsForValue().multiGet(existingKeys);
        if (CollectionUtils.isEmpty(viewCounts)) {
            return new ArrayList<>();
        }

        return IntStream.range(0, existingKeys.size())
                .mapToObj(index -> createQuestionViewResponse(existingKeys.get(index), viewCounts.get(index)))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void deleteKey(String key) {
        questionViewRedisTemplate.delete(key);
    }

    public void incrementViewCount(Long questionId) {
        String key = getKey(questionId);
        questionViewRedisTemplate.opsForValue().increment(key, 1);
    }

    public boolean existsByQuestionId(Long questionId) {
        String key = getKey(questionId);
        return Boolean.TRUE.equals(questionViewRedisTemplate.hasKey(key));
    }

    public void clear() {
        Set<String> keys = questionViewRedisTemplate.keys(KEY_PATTERN);
        if (keys != null && !keys.isEmpty()) {
            questionViewRedisTemplate.delete(keys);
        }
    }

    public Optional<Long> extractQuestionIdFromKey(String key) {
        Pattern pattern = Pattern.compile(KEY_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(key);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }
        return Optional.empty();
    }

    public Set<String> findAllKeys() {
        return questionViewRedisTemplate.keys(KEY_PATTERN);
    }

    public String getKey(Long questionId) {
        return String.format(KEY_FORMAT, questionId);
    }

    private List<String> getExistingKeys(List<Long> questionIds) {
        return questionIds.stream()
                .map(this::getKey)
                .distinct()
                .filter(questionViewRedisTemplate::hasKey)
                .toList();
    }

    private QuestionViewResponse createQuestionViewResponse(String key, Long viewCount) {
        return extractQuestionIdFromKey(key)
                .map(questionId -> new QuestionViewResponse(questionId, Optional.ofNullable(viewCount).orElse(0L)))
                .orElse(null);
    }
}
