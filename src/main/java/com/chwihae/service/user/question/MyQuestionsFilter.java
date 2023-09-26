package com.chwihae.service.user.question;

import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.service.question.QuestionViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MyQuestionsFilter implements UserQuestionsFilterStrategy {

    private final QuestionRepository questionRepository;
    private final QuestionViewService questionViewService;

    @Override
    public Page<QuestionListResponse> filter(Long userId, Pageable pageable) {
        Page<QuestionListResponse> page = questionRepository.findMyByUserIdWithCounts(userId, pageable);
        page.getContent().forEach(it -> it.setViewCount(questionViewService.getViewCount(it.getId())));
        return page;
    }
}
