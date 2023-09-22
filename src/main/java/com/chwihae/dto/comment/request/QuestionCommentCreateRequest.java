package com.chwihae.dto.comment.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCommentCreateRequest {
    @Size(max = 1000)
    private String content;
}
