package com.chwihae.domain.question;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

import static com.chwihae.domain.question.QQuestionEntity.questionEntity;

public class QuestionRepositoryExtensionImpl extends QuerydslRepositorySupport implements QuestionRepositoryExtension {

    public QuestionRepositoryExtensionImpl() {
        super(QuestionEntity.class);
    }

    @Override
    public Page<QuestionEntity> findByTypeAndStatus(QuestionStatus status, QuestionType type, Pageable pageable) {
        BooleanBuilder conditions = new BooleanBuilder();
        appendTypeCondition(conditions, Optional.ofNullable(type));
        appendStatusCondition(conditions, Optional.ofNullable(status));

        JPQLQuery<QuestionEntity> query = from(questionEntity).where(conditions);
        JPQLQuery<QuestionEntity> paginatedQuery = getQuerydsl().applyPagination(pageable, query);
        List<QuestionEntity> questionEntities = paginatedQuery.fetch();

        return new PageImpl<>(questionEntities, pageable, query.fetchCount());
    }

    private void appendTypeCondition(BooleanBuilder conditions, Optional<QuestionType> type) {
        type.ifPresent(it -> conditions.and(QQuestionEntity.questionEntity.type.eq(it)));
    }

    private void appendStatusCondition(BooleanBuilder conditions, Optional<QuestionStatus> status) {
        status.ifPresent(it -> conditions.and(QQuestionEntity.questionEntity.status.eq(it)));
    }
}
