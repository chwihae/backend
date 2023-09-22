package com.chwihae.dto.question.response;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionStatus;
import com.chwihae.domain.question.QuestionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class QuestionDetailResponse {

    private Long id;
    private String title;
    private String content;
    private QuestionType type;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closeAt;
    private QuestionStatus status;
    private long viewCount = 0;
    private long commentCount = 0;
    private long voteCount = 0;
    private long bookmarkCount = 0;
    private boolean bookmarked = false;
    private boolean editable;

    public static QuestionDetailResponse of(QuestionEntity questionEntity, long commentCount, long voteCount, boolean isEditable) {
        return QuestionDetailResponse.builder()
                .id(questionEntity.getId())
                .title(questionEntity.getTitle())
                .content(questionEntity.getContent())
                .type(questionEntity.getType())
                .closeAt(questionEntity.getCloseAt())
                .status(questionEntity.getStatus())
                .commentCount(commentCount)
                .voteCount(voteCount)
                .editable(isEditable)
                .build();
    }

    @Builder
    private QuestionDetailResponse(Long id,
                                   String title,
                                   String content,
                                   QuestionType type,
                                   LocalDateTime closeAt,
                                   QuestionStatus status,
                                   long commentCount,
                                   long voteCount,
                                   boolean editable) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.closeAt = closeAt;
        this.status = status;
        this.editable = editable;
        this.voteCount = voteCount;
        this.commentCount = commentCount;
    }
}
