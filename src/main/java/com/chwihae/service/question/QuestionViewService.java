package com.chwihae.service.question;

import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
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

    private static final long TEN_MINUTES_IN_MILLISECONDS = 10 * 60 * 1000L;
    private final QuestionViewRepository questionViewRepository;
    private final QuestionViewCacheRepository questionViewCacheRepository;

    @Transactional
    public void createQuestionView(QuestionEntity questionEntity) {
        questionViewRepository.save(QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build());
    }

    public Integer getViewCount(Long questionId) {
        return questionViewCacheRepository.getQuestionView(questionId)
                .orElseGet(() -> questionViewRepository.findViewCountByQuestionEntityId(questionId)
                        .map(viewCount -> {
                            questionViewCacheRepository.setQuestionView(questionId, viewCount);
                            return viewCount;
                        })
                        .orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND))
                );
    }

    /**
     * NOTE: This method may encounter concurrency issues, so caution is advised.
     */
    public void incrementViewCount(Long questionId) {
        if (!questionViewCacheRepository.existsByKey(questionId)) {
            int viewCount = questionViewRepository.findViewCountByQuestionEntityId(questionId)
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

    private Optional<Long> extractQuestionIdFromKey(String key) {
        Pattern pattern = Pattern.compile("^question:(\\d+):views$");
        Matcher matcher = pattern.matcher(key);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }
        return Optional.empty();
    }
}
