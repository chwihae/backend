package com.chwihae.domain.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    @Query(value =
            "SELECT EXISTS (" +
                    "    SELECT 1 " +
                    "    FROM vote v " +
                    "    JOIN option o ON v.id_option = o.id_option " +
                    "    WHERE o.id_question = :questionId " +
                    "    AND v.id_voter = :userId" +
                    ")", nativeQuery = true)
    boolean existsByQuestionEntityIdAndUserEntityId(@Param("questionId") Long questionId, @Param("userId") Long userId);
}

