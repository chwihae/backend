package com.chwihae.infra.fixture;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class QuestionEntityFixture {

    public static QuestionEntity of(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1))
                .type(QuestionType.SPEC)
                .build();
    }
}
