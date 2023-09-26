package com.chwihae.infra.fixture;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;

public abstract class OptionEntityFixture {
    public static OptionEntity of(QuestionEntity questionEntity) {
        return OptionEntity.builder()
                .questionEntity(questionEntity)
                .name("name")
                .build();
    }
}
