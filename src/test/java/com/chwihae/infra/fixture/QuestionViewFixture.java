package com.chwihae.infra.fixture;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionViewEntity;

public abstract class QuestionViewFixture {
    public static QuestionViewEntity of(QuestionEntity questionEntity) {
        return QuestionViewEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}
