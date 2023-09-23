package com.chwihae.config.redis;

import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionViewCacheRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 조회 수를 캐시에 저장하고 가져올 수 있다")
    void setAndGetQuestionView() {
        // given
        Long questionId = 1L;
        Integer viewCount = 100;

        // when
        questionViewCacheRepository.setQuestionView(questionId, viewCount);
        Optional<Integer> result = questionViewCacheRepository.getQuestionView(questionId);

        // then
        Assertions.assertThat(result)
                .isPresent().hasValueSatisfying(it -> {
                    Assertions.assertThat(it).isEqualTo(viewCount);
                });
    }

    @Test
    @DisplayName("질문 조회 수를 증가시킬 수 있다")
    void incrementViewCount() {
        // given
        Long questionId = 2L;
        int viewCount = 50;
        questionViewCacheRepository.setQuestionView(questionId, viewCount);

        // when
        questionViewCacheRepository.incrementViewCount(questionId);
        Optional<Integer> result = questionViewCacheRepository.getQuestionView(questionId);

        // then
        Assertions.assertThat(result)
                .isPresent()
                .hasValueSatisfying(it -> {
                    Assertions.assertThat(it).isEqualTo(viewCount + 1);
                });
    }

    @Test
    @DisplayName("특정 키의 존재 여부를 확인할 수 있다")
    void existsByKey() {
        // given
        Long questionId = 3L;
        questionViewCacheRepository.setQuestionView(questionId, 30);

        // when
        boolean result = questionViewCacheRepository.existsByKey(questionId);

        // then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 질문 조회 키를 가져올 수 있다")
    void findAllQuestionViewKeys() {
        // given
        Long questionId1 = 4L;
        Long questionId2 = 5L;
        questionViewCacheRepository.setQuestionView(questionId1, 40);
        questionViewCacheRepository.setQuestionView(questionId2, 50);

        // when
        Set<String> keys = questionViewCacheRepository.findAllQuestionViewKeys();

        // then
        Assertions.assertThat(keys)
                .containsOnly(questionViewCacheRepository.getKey(questionId1), questionViewCacheRepository.getKey(questionId2));
    }

    @Test
    @DisplayName("특정 키를 삭제할 수 있다")
    void deleteKey() {
        // given
        Long questionId = 6L;
        String key = questionViewCacheRepository.getKey(questionId);
        questionViewCacheRepository.setQuestionView(questionId, 60);
        assertTrue(questionViewCacheRepository.existsByKey(questionId));

        // when
        questionViewCacheRepository.deleteKey(key);

        // then
        Assertions.assertThat(questionViewCacheRepository.existsByKey(questionId))
                .isFalse();
    }

    @Test
    @DisplayName("캐시에 없는 질문의 조회 수는 empty를 반환한다")
    void getQuestionView_returnEmpty() {
        // given
        Long questionId = 7L;

        // when
        Optional<Integer> result = questionViewCacheRepository.getQuestionView(questionId);

        // then
        Assertions.assertThat(result).isEmpty();
    }
}
