package com.chwihae.dto.option.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class VoteOptionResponse {

    private Long votedOptionId;
    private boolean showVoteCount;
    private List<Option> options;

    public static VoteOptionResponse of(Long votedOptionId, boolean showVoteCount, List<Option> options) {
        return VoteOptionResponse.builder()
                .votedOptionId(votedOptionId)
                .showVoteCount(showVoteCount)
                .options(options)
                .build();
    }

    @Builder
    private VoteOptionResponse(Long votedOptionId, boolean showVoteCount, List<Option> options) {
        this.votedOptionId = votedOptionId;
        this.showVoteCount = showVoteCount;
        this.options = options;
    }
}