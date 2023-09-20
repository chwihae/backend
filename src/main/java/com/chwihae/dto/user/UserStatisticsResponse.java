package com.chwihae.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserStatisticsResponse {

    private long commentCount = 0;
    private long voteCount = 0;

    public static UserStatisticsResponse of(long commentCount, long voteCount) {
        return UserStatisticsResponse.builder()
                .commentCount(commentCount)
                .voteCount(voteCount)
                .build();
    }

    @Builder
    private UserStatisticsResponse(long commentCount, long voteCount) {
        this.commentCount = commentCount;
        this.voteCount = voteCount;
    }
}
