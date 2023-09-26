package com.chwihae.service.user.question;

import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.infra.test.AbstractMockTest;
import com.chwihae.service.question.QuestionViewService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class UserQuestionsFilterStrategyProviderTest extends AbstractMockTest {

    @Mock
    QuestionRepository questionRepository;

    @Mock
    QuestionViewService questionViewService;

    @Test
    @DisplayName("유효한 유저 질문 필터 타입으로 요청하면 적절한 필터를 반환한다")
    void getFilter_returnFilter() {
        //given
        UserQuestionsFilterStrategyProvider provider = new UserQuestionsFilterStrategyProvider(
                new MyQuestionsFilter(questionRepository, questionViewService),
                new BookmarkedQuestionsFilter(questionRepository, questionViewService),
                new VotedQuestionsFilter(questionRepository, questionViewService)
        );

        //when //then
        Assertions.assertThat(provider.getFilter(UserQuestionFilterType.ME)).isInstanceOf(MyQuestionsFilter.class);
        Assertions.assertThat(provider.getFilter(UserQuestionFilterType.BOOKMARKED)).isInstanceOf(BookmarkedQuestionsFilter.class);
        Assertions.assertThat(provider.getFilter(UserQuestionFilterType.VOTED)).isInstanceOf(VotedQuestionsFilter.class);
    }

    @Test
    @DisplayName("존재하지 않는 유저 질문 필터 타입을 요청할 때 예외를 던진다")
    void getFilter_withInvalidType_throwsException() {
        //given
        UserQuestionsFilterStrategyProvider provider = new UserQuestionsFilterStrategyProvider(
                new MyQuestionsFilter(questionRepository, questionViewService),
                new BookmarkedQuestionsFilter(questionRepository, questionViewService),
                new VotedQuestionsFilter(questionRepository, questionViewService)
        );

        //when //then
        Assertions.assertThatThrownBy(() -> provider.getFilter(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid user question filter type");
    }
}