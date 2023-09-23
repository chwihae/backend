package com.chwihae.service.question;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
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

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionViewService {

    private static final long ONE_HOUR_IN_MILLISECONDS = 3_600_000L;
    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;
    private final RedisTemplate<String, Integer> questionViewRedisTemplate;

    @Transactional
    public void createQuestionView(QuestionEntity questionEntity) {
        questionViewRepository.save(QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build());
    }

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
    @Scheduled(fixedDelay = ONE_HOUR_IN_MILLISECONDS)
    public void syncQuestionViewCount() {
        Set<String> keys = Optional.ofNullable(questionViewRedisTemplate.keys("question:*:views"))
                .orElse(Collections.emptySet());

        keys.forEach(key -> {
            Optional<Long> questionIdOpt = extractQuestionIdFromKey(key);

            if (questionIdOpt.isEmpty()) {
                return;
            }

            Long questionId = questionIdOpt.get();
            Integer viewCountFromRedis = questionViewRedisTemplate.opsForValue().get(key);

            if (viewCountFromRedis == null) {
                return;
            }

            questionViewRepository.findByQuestionEntityId(questionId).ifPresentOrElse(
                    entity -> {
                        entity.setViewCount(entity.getViewCount() + viewCountFromRedis);
                        questionViewRepository.save(entity);
                        questionViewRedisTemplate.opsForValue().set(key, entity.getViewCount());
                    },
                    () -> {
                        questionViewRedisTemplate.delete(key);
                    }
            );
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
