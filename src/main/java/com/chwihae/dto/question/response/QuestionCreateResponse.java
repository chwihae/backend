package com.chwihae.dto.question.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QuestionCreateResponse {

    private Long questionId;

    public static QuestionCreateResponse of(Long questionId) {
        return QuestionCreateResponse.builder()
                .questionId(questionId)
                .build();
    }

    @Builder
    private QuestionCreateResponse(Long questionId) {
        this.questionId = questionId;
    }
}
