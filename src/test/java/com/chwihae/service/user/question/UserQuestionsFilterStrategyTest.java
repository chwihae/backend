package com.chwihae.service.user.question;

import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.infra.test.AbstractMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

class UserQuestionsFilterStrategyTest extends AbstractMockTest {

    @InjectMocks
    BookmarkedQuestionsFilter bookmarkedQuestionsFilter;

    @InjectMocks
    MyQuestionsFilter myQuestionsFilter;

    @InjectMocks
    VotedQuestionsFilter votedQuestionsFilter;

    @Mock
    QuestionRepository questionRepository;

    @Test
    @DisplayName("사용자가 북마크한 질문 리스트를 페이지네이션으로 조회한다")
    void bookmarkedQuestionsFilter_filter() throws Exception {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<QuestionListResponse> bookmarkedQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            QuestionListResponse question = new QuestionListResponse();
            question.setId((long) i);
            bookmarkedQuestions.add(question);
        }

        when(questionRepository.findBookmarkedByUserIdWithCounts(userId, pageable))
                .thenReturn(new PageImpl<>(bookmarkedQuestions, pageable, 10));

        // when
        Page<QuestionListResponse> result = bookmarkedQuestionsFilter.filter(userId, pageable);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(result.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(result.getNumberOfElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("사용자가 작성한 질문 리스트를 페이지네이션으로 조회한다")
    void myQuestionsFilter_filter() throws Exception {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<QuestionListResponse> userQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            QuestionListResponse question = new QuestionListResponse();
            question.setId((long) i);
            userQuestions.add(question);
        }

        when(questionRepository.findMyByUserIdWithCounts(userId, pageable)).thenReturn(new PageImpl<>(userQuestions, pageable, 10));

        // when
        Page<QuestionListResponse> result = myQuestionsFilter.filter(userId, pageable);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(result.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(result.getNumberOfElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("사용자가 투표한 질문 리스트를 페이지네이션으로 조회한다")
    void votedQuestionsFilter_filter() throws Exception {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<QuestionListResponse> votedQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            QuestionListResponse question = new QuestionListResponse();
            question.setId((long) i);
            votedQuestions.add(question);
        }

        when(questionRepository.findVotedByUserIdWithCounts(userId, pageable)).thenReturn(new PageImpl<>(votedQuestions, pageable, 10));

        // when
        Page<QuestionListResponse> result = votedQuestionsFilter.filter(userId, pageable);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(result.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(result.getNumberOfElements()).isEqualTo(10);
    }
}