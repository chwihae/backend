package com.chwihae.dto.comment;

import com.chwihae.domain.comment.CommentEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class Comment {
    private Long id;
    private String content;
    private String commenterAlias;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;
    private boolean editable;

    public static Comment of(CommentEntity commentEntity, Boolean editable, String alias) {
        return Comment.builder()
                .id(commentEntity.getId())
                .content(commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .commenterAlias(alias)
                .editable(editable)
                .build();
    }

    @Builder
    private Comment(Long id, String content, LocalDateTime createdAt, String commenterAlias, boolean editable) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.commenterAlias = commenterAlias;
        this.editable = editable;
    }
}
