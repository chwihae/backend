package com.chwihae.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long>, QuestionRepositoryExtension {

    @Query("SELECT EXISTS(" +
            "SELECT 1 " +
            "FROM QuestionEntity qe " +
            "WHERE qe.closeAt < :now AND qe.status = com.chwihae.domain.question.QuestionStatus.IN_PROGRESS)")
    boolean existsByCloseAtBefore(LocalDateTime now);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM question", nativeQuery = true)
    void physicallyDeleteAll();
}
