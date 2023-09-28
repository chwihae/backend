package com.chwihae.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionViewRepository extends JpaRepository<QuestionViewEntity, Long> {
    @Query("SELECT qve.viewCount " +
            "FROM QuestionViewEntity qve " +
            "WHERE qve.questionEntity.id = :questionId")
    Optional<Long> findViewCountByQuestionEntityId(@Param("questionId") Long questionId);

    @Query("SELECT qve " +
            "FROM QuestionViewEntity qve " +
            "WHERE qve.questionEntity.id IN :questionIds")
    List<QuestionViewEntity> findByQuestionEntityIds(@Param("questionIds") List<Long> questionIds);

    @Transactional
    @Modifying
    @Query("UPDATE QuestionViewEntity qve " +
            "SET qve.deletedAt = NOW() " +
            "WHERE qve.questionEntity.id = :questionId")
    void deleteAllByQuestionId(@Param("questionId") Long questionId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM question_view", nativeQuery = true)
    void physicallyDeleteAll();
}
