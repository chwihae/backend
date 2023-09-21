package com.chwihae.dto.user;

import com.chwihae.domain.user.UserLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserStatisticsResponse {

    private UserLevel level = UserLevel.BACHELOR;
    private long commentCount = 0;
    private long voteCount = 0;

    public static UserStatisticsResponse of(UserLevel level, long commentCount, long voteCount) {
        return UserStatisticsResponse.builder()
                .level(level)
                .commentCount(commentCount)
                .voteCount(voteCount)
                .build();
    }

    @Builder
    private UserStatisticsResponse(UserLevel level, long commentCount, long voteCount) {
        this.level = level;
        this.commentCount = commentCount;
        this.voteCount = voteCount;
    }
}
