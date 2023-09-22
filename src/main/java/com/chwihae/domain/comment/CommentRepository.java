package com.chwihae.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    long countByQuestionEntityId(Long questionId);

    long countByUserEntityId(Long userId);

    @EntityGraph(attributePaths = "commenterAliasEntity")
    @Query("SELECT ce " +
            "FROM CommentEntity ce " +
            "WHERE ce.questionEntity.id = :questionId AND ce.userEntity.id = :userId")
    Optional<CommentEntity> findTopCommentByQuestionIdAndUserId(Long questionId, Long userId);

    @EntityGraph(attributePaths = "commenterAliasEntity")
    Page<CommentEntity> findWithAliasByQuestionEntityId(Long questionId, Pageable pageable);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM comment", nativeQuery = true)
    void physicallyDeleteAll();
}