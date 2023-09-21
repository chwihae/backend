package com.chwihae.fixture;

import com.chwihae.domain.user.UserEntity;

import java.util.UUID;

public class UserEntityFixture {

    public static UserEntity of(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    public static UserEntity of() {
        return of(UUID.randomUUID().toString() + System.currentTimeMillis() + Thread.currentThread().getId());
    }
}
