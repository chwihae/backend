package com.chwihae.domain.commenter;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CommenterSequenceRepository extends JpaRepository<CommenterSequenceEntity, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CommenterSequenceEntity> findForUpdateByQuestionEntityId(Long questionId);

    @Modifying
    @Query("UPDATE CommenterSequenceEntity cse " +
            "SET cse.sequence = cse.sequence+1 " +
            "WHERE cse.questionEntity.id = :questionId")
    void updateSequenceByQuestionEntityId(@Param("questionId") Long questionId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM commenter_sequence", nativeQuery = true)
    void physicallyDeleteAll();
}
