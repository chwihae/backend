package com.chwihae.domain.option;

import com.chwihae.dto.option.response.Option;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.chwihae.domain.option.QOptionEntity.optionEntity;
import static com.chwihae.domain.vote.QVoteEntity.voteEntity;


public class OptionRepositoryExtensionImpl extends QuerydslRepositorySupport implements OptionRepositoryExtension {

    public OptionRepositoryExtensionImpl() {
        super(OptionEntity.class);
    }

    @Override
    public List<Option> findOptionsWithResultsByQuestionId(Long questionId, boolean showVoteCount) {
        JPAQuery<Option> query = createBaseQuery(showVoteCount);
        appendVoteCountQuery(query, showVoteCount);
        appendFiltersAndOrder(query, questionId);
        List<Option> options = query.fetch();
        if (!showVoteCount) {
            options.forEach(option -> option.setVoteCount(null));
        }
        return options;
    }

    private JPAQuery<Option> createBaseQuery(boolean showVoteCount) {
        NumberExpression<Long> voteCountExpression = Expressions.asNumber(0L);
        if (showVoteCount) {
            voteCountExpression = voteEntity.count();
        }

        return new JPAQuery<>(getEntityManager())
                .select(Projections.constructor(Option.class, optionEntity.id, optionEntity.name, voteCountExpression))
                .from(optionEntity);
    }

    private void appendVoteCountQuery(JPAQuery<Option> query, boolean canViewResults) {
        if (canViewResults) {
            query.leftJoin(voteEntity)
                    .on(optionEntity.id.eq(voteEntity.optionEntity.id).and(voteEntity.deletedAt.isNull()));  // soft delete된 vote를 제외
        }
    }

    private void appendFiltersAndOrder(JPAQuery<Option> query, Long questionId) {
        query.where(optionEntity.questionEntity.id.eq(questionId))
                .groupBy(optionEntity.id, optionEntity.name)
                .orderBy(optionEntity.createdAt.asc());
    }
}
