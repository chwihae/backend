package com.chwihae.infra.fixture;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;

public abstract class VoteEntityFixture {
    public static VoteEntity of(OptionEntity optionEntity, UserEntity userEntity) {
        return VoteEntity.builder()
                .questionEntity(optionEntity.getQuestionEntity())
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}
