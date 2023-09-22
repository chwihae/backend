package com.chwihae.dto.comment.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionCommentCreateRequest {
    @Size(max = 1000)
    private String content;

    @Builder
    private QuestionCommentCreateRequest(String content) {
        this.content = content;
    }
}
