package com.chwihae.config.redis;

import com.chwihae.dto.question.response.QuestionViewResponse;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionViewCacheRepositoryTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        questionViewCacheRepository.clear();
    }

    @Test
    @DisplayName("질문 조회 수를 캐시에 저장하고 가져올 수 있다")
    void setAndGetQuestionView() {
        // given
        Long questionId = 1L;
        Long viewCount = 100L;

        // when
        questionViewCacheRepository.setViewCount(questionId, viewCount);
        Optional<Long> result = questionViewCacheRepository.getViewCount(questionId);

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
        Long viewCount = 50L;
        questionViewCacheRepository.setViewCount(questionId, viewCount);

        // when
        questionViewCacheRepository.incrementViewCount(questionId);
        Optional<Long> result = questionViewCacheRepository.getViewCount(questionId);

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
        questionViewCacheRepository.setViewCount(questionId, 30L);

        // when
        boolean result = questionViewCacheRepository.existsByQuestionId(questionId);

        // then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 질문 조회 키를 가져올 수 있다")
    void findAllQuestionViewKeys() {
        // given
        Long questionId1 = 4L;
        Long questionId2 = 5L;
        questionViewCacheRepository.setViewCount(questionId1, 40L);
        questionViewCacheRepository.setViewCount(questionId2, 50L);

        // when
        Set<String> keys = questionViewCacheRepository.findAllKeys();

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
        questionViewCacheRepository.setViewCount(questionId, 60L);
        assertTrue(questionViewCacheRepository.existsByQuestionId(questionId));

        // when
        questionViewCacheRepository.deleteKey(key);

        // then
        Assertions.assertThat(questionViewCacheRepository.existsByQuestionId(questionId))
                .isFalse();
    }

    @Test
    @DisplayName("캐시에 없는 질문의 조회 수는 empty를 반환한다")
    void getQuestionView_returnEmpty() {
        // given
        Long questionId = 7L;

        // when
        Optional<Long> result = questionViewCacheRepository.getViewCount(questionId);

        // then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("질문 아이디 리스트로 조회 수 리스트를 조회한다")
    void getViewCounts_returnList() throws Exception {
        //given
        Long questionId1 = 4L;
        long questionViewCount1 = 40L;
        Long questionId2 = 5L;
        long questionViewCount2 = 50L;
        questionViewCacheRepository.setViewCount(questionId1, questionViewCount1);
        questionViewCacheRepository.setViewCount(questionId2, questionViewCount2);

        //when
        List<QuestionViewResponse> response = questionViewCacheRepository.getViewCounts(List.of(questionId1, questionId2));

        //then
        Assertions.assertThat(response)
                .hasSize(2)
                .extracting("questionId", "viewCount")
                .containsOnly(
                        tuple(questionId1, questionViewCount1),
                        tuple(questionId2, questionViewCount2)
                );
    }

    @Test
    @DisplayName("캐싱된 질문 아이디가 없으면 빈 배열을 반환한다")
    void getViewCounts_returnEmptyList() throws Exception {
        //when
        List<QuestionViewResponse> response = questionViewCacheRepository.getViewCounts(List.of());

        //then
        Assertions.assertThat(response).isEmpty();
    }
}
