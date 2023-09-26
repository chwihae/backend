package com.chwihae.domain.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    int countByQuestionEntityId(Long questionId);

    boolean existsByQuestionEntityIdAndUserEntityId(Long questionId, Long userId);

    Optional<BookmarkEntity> findByQuestionEntityIdAndUserEntityId(Long questionId, Long userId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM bookmark", nativeQuery = true)
    void physicallyDeleteAll();

    @Transactional
    @Modifying
    @Query("UPDATE BookmarkEntity be " +
            "SET be.deletedAt = NOW() " +
            "WHERE be.questionEntity.id = :questionId")
    void deleteAllByQuestionId(@Param("questionId") Long questionId);
}
