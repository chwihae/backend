package com.chwihae.infra.fixture;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;

public abstract class BookmarkFixture {
    public static BookmarkEntity of(QuestionEntity questionEntity, UserEntity userEntity) {
        return BookmarkEntity.builder()
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }
}
