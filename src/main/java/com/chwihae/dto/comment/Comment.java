package com.chwihae.dto.comment;

import com.chwihae.domain.comment.CommentEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Comment {
    private Long id;
    private String content;
    private String commenterAlias;
    private boolean editable;

    public static Comment of(CommentEntity commentEntity, Boolean editable, String alias) {
        return Comment.builder()
                .id(commentEntity.getId())
                .content(commentEntity.getContent())
                .commenterAlias(alias)
                .editable(editable)
                .build();
    }

    @Builder
    private Comment(Long id, String content, String commenterAlias, boolean editable) {
        this.id = id;
        this.content = content;
        this.commenterAlias = commenterAlias;
        this.editable = editable;
    }
}
