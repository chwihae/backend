package com.chwihae.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionViewRepository extends JpaRepository<QuestionViewEntity, Long> {
    Optional<QuestionViewEntity> findByQuestionEntityId(Long questionId);

    @Query("SELECT qve.viewCount " +
            "FROM QuestionViewEntity qve " +
            "WHERE qve.questionEntity.id = :questionId")
    Optional<Integer> findViewCountByQuestionEntityId(@Param("questionId") Long questionId);
}