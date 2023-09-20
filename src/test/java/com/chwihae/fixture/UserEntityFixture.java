package com.chwihae.fixture;

import com.chwihae.domain.user.UserEntity;

public class UserEntityFixture {

    public static UserEntity of(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    public static UserEntity of() {
        return of("test@email.com");
    }
}
