package com.chwihae.dto.comment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionCommentRequest {
    @Size(max = 1000)
    @NotBlank
    private String content;

    @Builder
    private QuestionCommentRequest(String content) {
        this.content = content;
    }
}
