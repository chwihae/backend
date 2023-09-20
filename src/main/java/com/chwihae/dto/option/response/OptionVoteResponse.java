package com.chwihae.dto.option.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class OptionVoteResponse {

    private boolean canViewVoteResult;
    private List<Option> options;

    public static OptionVoteResponse of(boolean canViewVoteResult, List<Option> options) {
        return OptionVoteResponse.builder()
                .canViewVoteResult(canViewVoteResult)
                .options(options)
                .build();
    }

    @Builder
    private OptionVoteResponse(boolean canViewVoteResult, List<Option> options) {
        this.canViewVoteResult = canViewVoteResult;
        this.options = options;
    }
}