package com.chwihae.infra.fixture;

import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.question.QuestionEntity;

public abstract class CommenterSequenceFixture {
    public static CommenterSequenceEntity of(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build();
    }
}
