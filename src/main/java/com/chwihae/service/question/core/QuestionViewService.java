package com.chwihae.service.question.core;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionViewService {

    private static final long TEN_MINUTES_IN_MILLISECONDS = 10 * 60 * 1000L;
    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;

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

    /**
     * NOTE: This method may encounter concurrency issues, so caution is advised.
     */
    public void incrementViewCount(Long questionId) {
        if (!questionViewCacheRepository.existsByQuestionId(questionId)) {
            long viewCount = questionViewRepository.findViewCountByQuestionEntityId(questionId)
                    .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
            questionViewCacheRepository.setViewCount(questionId, viewCount);
        }
        questionViewCacheRepository.incrementViewCount(questionId);
    }

    @Transactional
    public void syncQuestionViewCount() {
        Set<String> keys = Optional.ofNullable(questionViewCacheRepository.findAllKeys())
                .orElse(Collections.emptySet());

        for (String key : keys) {
            Optional<Long> questionIdOpt = questionViewCacheRepository.extractQuestionIdFromKey(key);
            questionIdOpt.ifPresent(this::updateViewCount);
            questionViewCacheRepository.deleteKey(key);
        }
    }

    private void updateViewCount(Long questionId) {
        questionViewCacheRepository.getViewCount(questionId).ifPresent(viewCount -> {
            questionViewRepository.findByQuestionEntityId(questionId)
                    .ifPresent(entity -> {
                        entity.setViewCount(viewCount);
                        questionViewRepository.save(entity);
                    });
        });
    }
}
