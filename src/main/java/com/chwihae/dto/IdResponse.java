package com.chwihae.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class IdResponse {

    private Long id;

    public static IdResponse of(Long id) {
        return IdResponse.builder()
                .id(id)
                .build();
    }

    @Builder
    private IdResponse(Long id) {
        this.id = id;
    }
}