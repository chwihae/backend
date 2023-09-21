package com.chwihae.dto.question.response;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QuestionListResponse {

    private Long id;
    private String title;
    private QuestionType type;
    private QuestionStatus status;
    private long viewCount = 0;
    private long commentCount = 0;
    private long bookmarkCount = 0;

    public static QuestionListResponse of(QuestionEntity questionEntity) {
        return QuestionListResponse.builder()
                .id(questionEntity.getId())
                .status(questionEntity.getStatus())
                .type(questionEntity.getType())
                .build();
    }

    @Builder
    private QuestionListResponse(Long id, String title, QuestionType type, QuestionStatus status) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.status = status;
    }
}
