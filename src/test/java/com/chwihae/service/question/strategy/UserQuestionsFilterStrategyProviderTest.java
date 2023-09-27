package com.chwihae.service.question.strategy;

import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserQuestionsFilterStrategyProviderTest extends AbstractIntegrationTest {

    @CsvSource({"ME", "BOOKMARKED", "VOTED"})
    @ParameterizedTest
    @DisplayName("UserQuestionFilterType 으로 필터를 가져온다")
    void getFilter_returnFilter(UserQuestionFilterType type) throws Exception {
        //when //then
        Assertions.assertThat(userQuestionsFilterStrategyProvider.getFilter(type))
                .isNotNull()
                .isInstanceOf(UserQuestionsFilterStrategy.class);
    }

    @Test
    @DisplayName("존재하지 않는 유저 질문 필터 타입을 요청할 때 예외를 던진다")
    void getFilter_withInvalidType_throwsException() {
        //when //then
        Assertions.assertThatThrownBy(() -> userQuestionsFilterStrategyProvider.getFilter(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid user question filter type");
    }
}