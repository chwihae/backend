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

import static com.chwihae.domain.bookmark.QBookmarkEntity.bookmarkEntity;
import static com.chwihae.domain.comment.QCommentEntity.commentEntity;
import static com.chwihae.domain.question.QQuestionEntity.questionEntity;
import static com.chwihae.domain.user.QUserEntity.userEntity;
import static com.chwihae.domain.vote.QVoteEntity.voteEntity;

public class QuestionRepositoryExtensionImpl extends QuerydslRepositorySupport implements QuestionRepositoryExtension {

    public QuestionRepositoryExtensionImpl() {
        super(QuestionEntity.class);
    }

    @Override
    public Page<QuestionListResponse> findByTypeAndStatusWithCounts(QuestionStatus status, QuestionType type, Pageable pageable) {
        List<Tuple> tuples = fetchTuplesByTypeAndStatus(status, type, pageable);
        List<QuestionListResponse> questionListResponses = transformTuplesToDTOs(tuples);
        long totalCount = countQuestions(status, type);
        return new PageImpl<>(questionListResponses, pageable, totalCount);
    }

    @Override
    public Page<QuestionListResponse> findMyByUserIdWithCounts(Long userId, Pageable pageable) {
        List<Tuple> tuples = fetchMyTuplesByUserId(userId, pageable);
        List<QuestionListResponse> questionListResponses = transformTuplesToDTOs(tuples);
        long totalCount = countQuestionsByUserId(userId);
        return new PageImpl<>(questionListResponses, pageable, totalCount);
    }

    @Override
    public Page<QuestionListResponse> findBookmarkedByUserIdWithCounts(Long userId, Pageable pageable) {
        List<Tuple> tuples = fetchBookmarkedTuplesByUserId(userId, pageable);
        List<QuestionListResponse> questionListResponses = transformTuplesToDTOs(tuples);
        long totalCount = countBookmarkedQuestionsByUserId(userId);
        return new PageImpl<>(questionListResponses, pageable, totalCount);
    }

    @Override
    public Page<QuestionListResponse> findVotedByUserIdWithCounts(Long userId, Pageable pageable) {
        List<Tuple> tuples = fetchVotedTuplesByUserId(userId, pageable);
        List<QuestionListResponse> questionListResponses = transformTuplesToDTOs(tuples);
        long totalCount = countVotedQuestionsByUserId(userId);
        return new PageImpl<>(questionListResponses, pageable, totalCount);
    }

    private List<Tuple> fetchTuplesByTypeAndStatus(QuestionStatus status, QuestionType type, Pageable pageable) {
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

    private List<Tuple> fetchMyTuplesByUserId(Long userId, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory
                .select(questionEntity, commentCountSubQuery(), voteCountSubQuery())
                .from(questionEntity)
                .join(questionEntity.userEntity, userEntity)
                .where(questionEntity.userEntity.id.eq(userId))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private List<Tuple> fetchVotedTuplesByUserId(Long userId, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory
                .select(questionEntity, commentCountSubQuery(), voteCountSubQuery())
                .from(questionEntity)
                .join(voteEntity).on(voteEntity.questionEntity.id.eq(questionEntity.id))
                .where(voteEntity.valid.eq(true).and(voteEntity.userEntity.id.eq(userId)))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private List<Tuple> fetchBookmarkedTuplesByUserId(Long userId, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory
                .select(questionEntity, commentCountSubQuery(), voteCountSubQuery())
                .from(questionEntity)
                .join(bookmarkEntity).on(bookmarkEntity.questionEntity.id.eq(questionEntity.id))
                .where(bookmarkEntity.userEntity.id.eq(userId))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private long countVotedQuestionsByUserId(Long userId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory.selectFrom(questionEntity)
                .join(voteEntity).on(voteEntity.questionEntity.id.eq(questionEntity.id))
                .where(voteEntity.userEntity.id.eq(userId))
                .fetchCount();
    }

    private long countBookmarkedQuestionsByUserId(Long userId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory.selectFrom(questionEntity)
                .join(bookmarkEntity).on(bookmarkEntity.questionEntity.id.eq(questionEntity.id))
                .where(bookmarkEntity.userEntity.id.eq(userId))
                .fetchCount();
    }

    private long countQuestionsByUserId(Long userId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFactory.selectFrom(questionEntity)
                .join(questionEntity.userEntity, userEntity)
                .where(userEntity.id.eq(userId))
                .fetchCount();
    }

//    private JPQLQuery<Long> viewCountSubQuery() {
//        return JPAExpressions.select(questionViewEntity.viewCount)
//                .from(questionViewEntity)
//                .where(questionViewEntity.questionEntity.id.eq(questionEntity.id));
//    }

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
                .map(tuple -> QuestionListResponse.of(
                        tuple.get(questionEntity),
                        Optional.ofNullable(tuple.get(1, Long.class)).orElse(0L),
                        Optional.ofNullable(tuple.get(2, Long.class)).orElse(0L)))
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
