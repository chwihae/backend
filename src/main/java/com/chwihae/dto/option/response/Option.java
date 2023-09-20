package com.chwihae.dto.option.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class Option {

    private Long id;
    private String name;
    private Long voteCount;

    public Option(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Option(Long id, String name, Long voteCount) {
        this.id = id;
        this.name = name;
        this.voteCount = voteCount;
    }
}