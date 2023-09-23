package com.chwihae.domain.question;

import com.chwihae.dto.question.response.QuestionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface QuestionRepositoryExtension {

    Page<QuestionListResponse> findByTypeAndStatusWithCounts(QuestionStatus status, QuestionType type, Pageable pageable);

    Page<QuestionListResponse> findMyByUserIdWithCounts(Long userId, Pageable pageable);

    Page<QuestionListResponse> findBookmarkedByUserIdWithCounts(Long userId, Pageable pageable);

    Page<QuestionListResponse> findVotedByUserIdWithCounts(Long userId, Pageable pageable);
}
