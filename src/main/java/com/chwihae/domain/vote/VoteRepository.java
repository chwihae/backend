package com.chwihae.domain.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    boolean existsByQuestionEntityIdAndUserEntityId(Long questionId, Long userId);

    int countByUserEntityId(Long userId);

    int countByQuestionEntityId(Long questionId);

    Optional<VoteEntity> findByQuestionEntityIdAndUserEntityId(Long questionId, Long userId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<VoteEntity> findForUpdateByQuestionEntityIdAndOptionEntityIdAndUserEntityId(Long questionId, Long optionId, Long userId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM vote", nativeQuery = true)
    void physicallyDeleteAll();

    @Transactional
    @Modifying
    @Query("UPDATE VoteEntity ve " +
            "SET ve.deletedAt = NOW(), ve.valid = NULL " +
            "WHERE ve.questionEntity.id = :questionId")
    void deleteAllByQuestionId(@Param("questionId") Long questionId);
}

