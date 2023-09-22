package com.chwihae.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum UserLevel {
    PROFESSOR(300, 100),
    DOCTOR(100, 30),
    MASTER(20, 5),
    BACHELOR(0, 0);

    private final long voteCount;
    private final long commentCount;

    public static UserLevel getLevel(long voteCount, long commentCount) {
        return Arrays.stream(UserLevel.values())
                .filter(level -> (voteCount >= level.voteCount) && (commentCount >= level.commentCount))
                .findFirst()
                .orElse(BACHELOR);
    }
}
