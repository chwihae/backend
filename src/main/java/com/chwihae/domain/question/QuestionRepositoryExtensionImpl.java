package com.chwihae.domain.question;

import com.chwihae.dto.question.response.QuestionListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

import static com.chwihae.domain.comment.QCommentEntity.commentEntity;
import static com.chwihae.domain.question.QQuestionEntity.questionEntity;
import static com.chwihae.domain.vote.QVoteEntity.voteEntity;

public class QuestionRepositoryExtensionImpl extends QuerydslRepositorySupport implements QuestionRepositoryExtension {

    public QuestionRepositoryExtensionImpl() {
        super(QuestionEntity.class);
    }

    @Override
    public Page<QuestionListResponse> findByTypeAndStatusWithCounts(QuestionStatus status, QuestionType type, Pageable pageable) {
        List<Tuple> tuples = fetchTuples(status, type, pageable);
        List<QuestionListResponse> questionListResponses = transformTuplesToDTOs(tuples);
        long totalCount = countQuestions(status, type);
        return new PageImpl<>(questionListResponses, pageable, totalCount);
    }

    private List<Tuple> fetchTuples(QuestionStatus status, QuestionType type, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        BooleanBuilder conditions = baseConditions(status, type);

        return queryFactory
                .select(questionEntity, commentCountSubQuery(), voteCountSubQuery())
                .from(questionEntity)
                .where(conditions)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private JPQLQuery<Long> commentCountSubQuery() {
        return JPAExpressions.select(commentEntity.count())
                .from(commentEntity)
                .where(commentEntity.questionEntity.id.eq(questionEntity.id));
    }

    private JPQLQuery<Long> voteCountSubQuery() {
        return JPAExpressions.select(voteEntity.count())
                .from(voteEntity)
                .where(voteEntity.questionEntity.id.eq(questionEntity.id));
    }

    private List<QuestionListResponse> transformTuplesToDTOs(List<Tuple> results) {
        return results.stream()
                .map(tuple -> QuestionListResponse.of(tuple.get(questionEntity), tuple.get(1, Long.class), tuple.get(2, Long.class)))
                .toList();
    }

    private long countQuestions(QuestionStatus status, QuestionType type) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        BooleanBuilder conditions = baseConditions(status, type);

        return queryFactory.selectFrom(questionEntity)
                .where(conditions)
                .fetchCount();
    }

    private BooleanBuilder baseConditions(QuestionStatus status, QuestionType type) {
        BooleanBuilder conditions = new BooleanBuilder();
        appendTypeCondition(conditions, Optional.ofNullable(type));
        appendStatusCondition(conditions, Optional.ofNullable(status));
        return conditions;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(order -> {
                    Path<Object> path = Expressions.path(Object.class, questionEntity, order.getProperty());
                    return new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, path);
                })
                .toArray(OrderSpecifier[]::new);
    }

    private void appendTypeCondition(BooleanBuilder conditions, Optional<QuestionType> type) {
        type.ifPresent(it -> conditions.and(questionEntity.type.eq(it)));
    }

    private void appendStatusCondition(BooleanBuilder conditions, Optional<QuestionStatus> status) {
        status.ifPresent(it -> conditions.and(questionEntity.status.eq(it)));
    }
}
