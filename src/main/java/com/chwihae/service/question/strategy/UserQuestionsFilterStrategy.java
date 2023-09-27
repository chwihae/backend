package com.chwihae.service.question.strategy;

import com.chwihae.dto.question.response.QuestionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQuestionsFilterStrategy {
    Page<QuestionListResponse> filter(Long userId, Pageable pageable);
}
