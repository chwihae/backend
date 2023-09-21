package com.chwihae.domain.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface QuestionRepositoryExtension {
    Page<QuestionEntity> findByTypeAndStatus(QuestionStatus status, QuestionType type, Pageable pageable);
}
