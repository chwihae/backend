package com.chwihae.infra.fixture;

import com.chwihae.domain.comment.CommentEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;

public abstract class CommentEntityFixture {
    public static CommentEntity of(UserEntity userEntity, QuestionEntity questionEntity, CommenterAliasEntity commenterAliasEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .questionEntity(questionEntity)
                .commenterAliasEntity(commenterAliasEntity)
                .content("content")
                .build();
    }
}
