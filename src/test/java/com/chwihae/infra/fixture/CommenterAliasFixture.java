package com.chwihae.infra.fixture;

import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;

import java.util.UUID;

public abstract class CommenterAliasFixture {
    public static CommenterAliasEntity of(UserEntity user, QuestionEntity questionEntity) {
        return CommenterAliasEntity.builder()
                .alias(UUID.randomUUID().toString())
                .questionEntity(questionEntity)
                .userEntity(user)
                .build();
    }
}
