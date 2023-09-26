package com.chwihae.service.question;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        return questionViewCacheRepository.getQuestionView(questionId)
                .orElseGet(() -> questionViewRepository.findViewCountByQuestionEntityId(questionId)
                        .map(viewCount -> {
                            questionViewCacheRepository.setQuestionView(questionId, viewCount);
                            return viewCount;
                        })
                        .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND))
                );
    }

    // TODO test - redis에 저장된 view가 없을경우
    // TODO test - redisd와 DB에 view를 합쳐서 리턴
    public List<QuestionViewResponse> getViewCounts(List<Long> questionIds) {
        List<QuestionViewResponse> viewsFromCache = questionViewCacheRepository.getQuestionViews(questionIds); // 1. Get view count from cache

        List<Long> idsNotInCache = questionIds.stream() // 2. Find question id not in cache
                .filter(it -> viewsFromCache.stream().noneMatch(view -> Objects.equals(view.getQuestionId(), it)))
                .toList();

        List<QuestionViewResponse> viewsFromDb = getViewsFromDb(idsNotInCache); // 3. Get view count that not exists in cache from DB
        viewsFromDb.forEach(it -> questionViewCacheRepository.setQuestionView(it.getQuestionId(), it.getViewCount())); // 4. Save view count in cache
        viewsFromCache.addAll(viewsFromDb); // 5. Cache + DB

        return viewsFromCache;
    }

    private List<QuestionViewResponse> getViewsFromDb(List<Long> idsNotInCache) {
        if (Objects.isNull(idsNotInCache) || idsNotInCache.isEmpty()) {
            return new ArrayList<>();
        }
        List<QuestionViewEntity> viewsEntities = questionViewRepository.findViewCountsByQuestionEntityIds(idsNotInCache);
        return viewsEntities.stream()
                .map(it -> new QuestionViewResponse(it.getQuestionId(), it.getViewCount()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * NOTE: This method may encounter concurrency issues, so caution is advised.
     */
    public void incrementViewCount(Long questionId) {
        if (!questionViewCacheRepository.existsByKey(questionId)) {
            long viewCount = questionViewRepository.findViewCountByQuestionEntityId(questionId)
                    .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
            questionViewCacheRepository.setQuestionView(questionId, viewCount);
        }
        questionViewCacheRepository.incrementViewCount(questionId);
    }

    @Transactional
    @Scheduled(fixedDelay = TEN_MINUTES_IN_MILLISECONDS)
    public void syncQuestionViewCount() {
        Set<String> keys = Optional.ofNullable(questionViewCacheRepository.findAllQuestionViewKeys())
                .orElse(Collections.emptySet());

        for (String key : keys) {
            Optional<Long> questionIdOpt = extractQuestionIdFromKey(key);
            questionIdOpt.ifPresent(this::updateViewCount);
            questionViewCacheRepository.deleteKey(key);
        }
    }

    private void updateViewCount(Long questionId) {
        questionViewCacheRepository.getQuestionView(questionId).ifPresent(viewCount -> {
            questionViewRepository.findByQuestionEntityId(questionId)
                    .ifPresent(entity -> {
                        entity.setViewCount(viewCount);
                        questionViewRepository.save(entity);
                    });
        });
    }

    public Optional<Long> extractQuestionIdFromKey(String key) {
        Pattern pattern = Pattern.compile("^question:(\\d+):views$");
        Matcher matcher = pattern.matcher(key);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }
        return Optional.empty();
    }
}
