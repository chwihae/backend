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
    private int viewCount;
    private int commentCount;
    private int voteCount;
    private int bookmarkCount;
    private boolean bookmarked;
    private boolean editable;

    public static QuestionDetailResponse of(QuestionEntity questionEntity,
                                            int viewCount,
                                            int bookmarkCount,
                                            int commentCount,
                                            int voteCount,
                                            boolean bookmarked,
                                            boolean editable) {
        return QuestionDetailResponse.builder()
                .id(questionEntity.getId())
                .title(questionEntity.getTitle())
                .content(questionEntity.getContent())
                .type(questionEntity.getType())
                .closeAt(questionEntity.getCloseAt())
                .status(questionEntity.getStatus())
                .viewCount(viewCount)
                .bookmarkCount(bookmarkCount)
                .commentCount(commentCount)
                .voteCount(voteCount)
                .bookmarked(bookmarked)
                .editable(editable)
                .build();
    }

    @Builder
    private QuestionDetailResponse(Long id,
                                   String title,
                                   String content,
                                   QuestionType type,
                                   LocalDateTime closeAt,
                                   QuestionStatus status,
                                   int viewCount,
                                   int bookmarkCount,
                                   int commentCount,
                                   int voteCount,
                                   boolean bookmarked,
                                   boolean editable) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.closeAt = closeAt;
        this.status = status;
        this.bookmarked = bookmarked;
        this.editable = editable;
        this.viewCount = viewCount;
        this.bookmarkCount = bookmarkCount;
        this.voteCount = voteCount;
        this.commentCount = commentCount;
    }
}
