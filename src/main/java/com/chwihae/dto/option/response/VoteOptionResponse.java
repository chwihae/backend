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

    private boolean canViewVoteResult;
    private List<Option> options;

    public static VoteOptionResponse of(boolean canViewVoteResult, List<Option> options) {
        return VoteOptionResponse.builder()
                .canViewVoteResult(canViewVoteResult)
                .options(options)
                .build();
    }

    @Builder
    private VoteOptionResponse(boolean canViewVoteResult, List<Option> options) {
        this.canViewVoteResult = canViewVoteResult;
        this.options = options;
    }
}