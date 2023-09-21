package com.chwihae.domain.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    boolean existsByQuestionEntityIdAndUserEntityId(Long questionId, Long userId);
}

