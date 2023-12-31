package com.chwihae.service.question.strategy;

import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.dto.question.response.QuestionListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookmarkedQuestionsFilter implements UserQuestionsFilterStrategy {

    private final QuestionRepository questionRepository;

    @Override
    public Page<QuestionListResponse> filter(Long userId, Pageable pageable) {
        return questionRepository.findBookmarkedByUserIdWithCounts(userId, pageable);
    }
}
