package com.chwihae.service.question;

import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.dto.question.response.QuestionListResponse;
import com.chwihae.dto.user.UserQuestionFilterType;
import com.chwihae.service.question.core.QuestionCreateService;
import com.chwihae.service.question.core.QuestionDeletionService;
import com.chwihae.service.question.query.QuestionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionCreateService questionCreateService;
    private final QuestionDeletionService questionDeletionService;
    private final QuestionQueryService questionQueryService;

    @Transactional
    public Long createQuestion(QuestionCreateRequest request, Long userId) {
        return questionCreateService.createQuestion(request, userId);
    }

    public Page<QuestionListResponse> getQuestionsByTypeAndStatus(QuestionType type, QuestionStatus status,
                                                                  Pageable pageable) {
        return questionQueryService.getQuestionsByTypeAndStatus(type, status, pageable);
    }

    public Page<QuestionListResponse> getUserQuestions(Long userId, UserQuestionFilterType type, Pageable pageable) {
        return questionQueryService.getUserQuestions(userId, type, pageable);
    }

    public QuestionDetailResponse getQuestion(Long questionId, Long userId) {
        return questionQueryService.getQuestion(questionId, userId);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        questionDeletionService.deleteQuestion(questionId, userId);
    }
}
