package com.chwihae.dto.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BooleanResponse {

    private Boolean result;

    public static BooleanResponse of(Boolean result) {
        return BooleanResponse.builder()
                .result(result)
                .build();
    }
    
    @Builder
    private BooleanResponse(Boolean result) {
        this.result = result;
    }
}
