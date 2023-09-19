package com.chwihae.domain.comment;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "comment")
@SQLDelete(sql = "UPDATE comment SET deleted_at = NOW() WHERE id_comment = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment", nullable = false, updatable = false)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "text COMMENT '댓글 내용'")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commenter", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    public CommentEntity(UserEntity userEntity, QuestionEntity questionEntity, String content) {
        this.userEntity = userEntity;
        this.questionEntity = questionEntity;
        this.content = content;
    }
}
