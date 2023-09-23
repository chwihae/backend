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
    private int commentCount = 0;
    private int voteCount = 0;

    public static UserStatisticsResponse of(UserLevel level, int commentCount, int voteCount) {
        return UserStatisticsResponse.builder()
                .level(level)
                .commentCount(commentCount)
                .voteCount(voteCount)
                .build();
    }

    @Builder
    private UserStatisticsResponse(UserLevel level, int commentCount, int voteCount) {
        this.level = level;
        this.commentCount = commentCount;
        this.voteCount = voteCount;
    }
}
