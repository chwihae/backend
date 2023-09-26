package com.chwihae.dto.question.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionViewResponse {
    private Long questionId;
    private Long viewCount;
}
