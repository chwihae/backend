package com.chwihae.domain.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    Page<QuestionEntity> findAllByStatus(QuestionStatus status, Pageable pageable);
}
