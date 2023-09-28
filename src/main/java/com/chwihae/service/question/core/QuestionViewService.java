package com.chwihae.service.question.core;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionViewService {

    private static final String LOCK_PREFIX = "question:%d:views:lock:";
    private static final Duration LOCK_EXPIRED_DURATION = Duration.ofSeconds(3);
    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;
    private final RedisTemplate<String, String> questionViewLockRedisTemplate;

    @Transactional
    public void createQuestionView(QuestionEntity questionEntity) {
        questionViewRepository.save(QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build());
    }

    public Long getViewCount(Long questionId) {
        return questionViewCacheRepository.getViewCount(questionId)
                .orElseGet(() -> questionViewRepository.findViewCountByQuestionEntityId(questionId)
                        .map(viewCount -> {
                            questionViewCacheRepository.setViewCount(questionId, viewCount);
                            return viewCount;
                        })
                        .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND))
                );
    }

    public List<QuestionViewResponse> getViewCounts(List<Long> questionIds) {
        List<QuestionViewResponse> viewsFromCache = questionViewCacheRepository.getViewCounts(questionIds); // 1. Get view count from cache

        List<Long> idsNotInCache = questionIds.stream() // 2. Find question id not in cache
                .filter(it -> viewsFromCache.stream().noneMatch(view -> Objects.equals(view.getQuestionId(), it)))
                .toList();
        if (CollectionUtils.isEmpty(idsNotInCache)) {
            return viewsFromCache;
        }

        List<QuestionViewResponse> viewsFromDb = getViewsFromDb(idsNotInCache); // 3. Get view count that not exists in cache from DB
        viewsFromDb.forEach(it -> questionViewCacheRepository.setViewCount(it.getQuestionId(), it.getViewCount())); // 4. Save view count in cache
        viewsFromCache.addAll(viewsFromDb); // 5. Cache + DB
        return viewsFromCache;
    }

    private List<QuestionViewResponse> getViewsFromDb(List<Long> idsNotInCache) {
        List<QuestionViewEntity> viewsEntities = questionViewRepository.findByQuestionEntityIds(idsNotInCache);
        return viewsEntities.stream()
                .map(it -> new QuestionViewResponse(it.getQuestionId(), it.getViewCount()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void incrementViewCount(Long questionId) {
        acquireLock(questionId);
        try {
            if (!questionViewCacheRepository.existsByQuestionId(questionId)) {
                long viewCount = questionViewRepository.findViewCountByQuestionEntityId(questionId)
                        .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
                questionViewCacheRepository.setViewCount(questionId, viewCount);
            }

            questionViewCacheRepository.incrementViewCount(questionId);
        } finally {
            releaseLock(questionId);
        }
    }

    private void acquireLock(Long questionId) {
        final int maxAttempts = 10;
        String lockKey = getLockKey(questionId);
        ValueOperations<String, String> ops = questionViewLockRedisTemplate.opsForValue();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Boolean isSuccess = ops.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRED_DURATION);
            if (Boolean.TRUE.equals(isSuccess)) {
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
        throw new IllegalStateException("Failed to acquire lock for " + questionId);
    }

    private void releaseLock(Long questionId) {
        String lockKey = getLockKey(questionId);
        String currentLockValue = questionViewLockRedisTemplate.opsForValue().get(lockKey);
        if ("LOCKED".equals(currentLockValue)) {
            questionViewLockRedisTemplate.delete(lockKey);
        }
    }

    private String getLockKey(Long questionId) {
        return LOCK_PREFIX + questionId;
    }
}
