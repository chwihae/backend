package com.chwihae.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionViewRepository extends JpaRepository<QuestionViewEntity, Long> {
    Optional<QuestionViewEntity> findByQuestionEntityId(Long questionId);

    @Query("SELECT qve.viewCount " +
            "FROM QuestionViewEntity qve " +
            "WHERE qve.questionEntity.id = :questionId")
    Optional<Long> findViewCountByQuestionEntityId(@Param("questionId") Long questionId);

    // TODO test
    @Query("SELECT qve " +
            "FROM QuestionViewEntity qve " +
            "WHERE qve.questionEntity.id IN :questionIds")
    List<QuestionViewEntity> findViewCountsByQuestionEntityIds(@Param("questionIds") List<Long> questionIds);
}
