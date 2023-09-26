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
    private long viewCount;
    private long commentCount;
    private long bookmarkCount;

    public static QuestionListResponse of(QuestionEntity questionEntity,
                                          long commentCount,
                                          long bookmarkCount) {
        return QuestionListResponse.builder()
                .id(questionEntity.getId())
                .title(questionEntity.getTitle())
                .status(questionEntity.getStatus())
                .type(questionEntity.getType())
                .commentCount(commentCount)
                .bookmarkCount(bookmarkCount)
                .build();
    }

    @Builder
    private QuestionListResponse(Long id,
                                 String title,
                                 QuestionType type,
                                 QuestionStatus status,
                                 long commentCount,
                                 long bookmarkCount) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.status = status;
        this.commentCount = commentCount;
        this.bookmarkCount = bookmarkCount;
    }
}
