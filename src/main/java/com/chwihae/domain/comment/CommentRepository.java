package com.chwihae.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    int countByQuestionEntityId(Long questionId);

    int countByUserEntityId(Long userId);

    //@EntityGraph(attributePaths = "commenterAliasEntity")
    @Query(value = "SELECT * " +
            "FROM comment c " +
            "WHERE c.id_question = :questionId AND c.id_commenter = :userId", nativeQuery = true)
    Optional<CommentEntity> findFirstByQuestionEntityIdAndUserEntityId(@Param("questionId") Long questionId,
                                                                       @Param("userId") Long userId);

    @EntityGraph(attributePaths = "commenterAliasEntity")
    Page<CommentEntity> findWithAliasByQuestionEntityId(Long questionId, Pageable pageable);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM comment", nativeQuery = true)
    void physicallyDeleteAll();

    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedAt = NOW() " +
            "WHERE ce.questionEntity.id = :questionId")
    void deleteAllByQuestionId(@Param("questionId") Long questionId);
}