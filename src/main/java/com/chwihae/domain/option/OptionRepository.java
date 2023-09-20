package com.chwihae.domain.option;

import com.chwihae.dto.option.response.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<OptionEntity, Long> {

    @Query("SELECT NEW com.chwihae.dto.option.response.Option(oe.id, oe.name ,COUNT(ve.id))  " +
            "FROM OptionEntity oe " +
            "LEFT JOIN VoteEntity ve ON oe.id = ve.optionEntity.id " +
            "WHERE oe.questionEntity.id = :questionId " +
            "GROUP BY oe.id " +
            "ORDER BY oe.createdAt ASC")
    List<Option> findWithVoteCountByQuestionEntityId(@Param("questionId") Long questionId);

    @Query("SELECT NEW com.chwihae.dto.option.response.Option(oe.id, oe.name)  " +
            "FROM OptionEntity oe " +
            "WHERE oe.questionEntity.id = :questionId " +
            "ORDER BY oe.createdAt ASC")
    List<Option> findWithoutVoteCountByQuestionEntityId(@Param("questionId") Long questionId);
}
