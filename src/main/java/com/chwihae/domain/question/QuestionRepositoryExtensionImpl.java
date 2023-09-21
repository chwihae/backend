package com.chwihae.domain.question;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class QuestionRepositoryExtensionImpl extends QuerydslRepositorySupport implements QuestionRepositoryExtension {

    public QuestionRepositoryExtensionImpl() {
        super(QuestionEntity.class);
    }

    @Override
    public Page<QuestionEntity> findByStatusAndType(QuestionStatus status, QuestionType type, Pageable pageable) {
        QQuestionEntity question = QQuestionEntity.questionEntity;

        JPQLQuery<QuestionEntity> query = from(question);

        if (type != null) {
            query.where(question.type.eq(type));
        }

        if (status != null) {
            query.where(question.status.eq(status));
        }

        JPQLQuery<QuestionEntity> paginatedQuery = getQuerydsl().applyPagination(pageable, query);
        List<QuestionEntity> questionEntities = paginatedQuery.fetch();
        return new PageImpl<>(questionEntities, pageable, query.fetchCount());
    }
}
