package com.chwihae.service.question;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class QuestionViewService {

    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;
    private final RedisTemplate<String, Integer> questionViewRedisTemplate;

    public Integer getViewCount(Long questionId) {
        return questionViewCacheRepository.getQuestionView(questionId)
                .orElseGet(() -> {
                    return questionViewRepository.findViewCountByQuestionEntityId(questionId)
                            .map(viewCount -> {
                                questionViewCacheRepository.setQuestionView(questionId, viewCount);
                                return viewCount;
                            })
                            .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
                });
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)  // per 1 minutes
    public void syncQuestionViewCount() {
        Set<String> keys = Optional.ofNullable(questionViewRedisTemplate.keys("question:*:views"))
                .orElse(Collections.emptySet());

        keys.forEach(key -> {
            Optional<Long> questionIdOpt = extractQuestionIdFromKey(key);

            if (questionIdOpt.isEmpty()) {
                return; // Skip to the next iteration if questionId couldn't be extracted
            }

            Long questionId = questionIdOpt.get();
            Integer viewCount = questionViewRedisTemplate.opsForValue().get(key);

            if (viewCount == null) {
                return; // Skip to the next iteration if viewCount is null
            }

            questionViewRepository.findByQuestionEntityId(questionId).ifPresent(entity -> {
                entity.setViewCount(entity.getViewCount() + viewCount);
                questionViewRepository.save(entity);
            });

            // Delete the key from Redis, regardless of whether we updated the database or not
            questionViewRedisTemplate.delete(key);
        });
    }

    private Optional<Long> extractQuestionIdFromKey(String key) {
        Pattern pattern = Pattern.compile("^question:(\\d+):views$");
        Matcher matcher = pattern.matcher(key);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }
        return Optional.empty();
    }
}
