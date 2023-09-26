package com.chwihae.domain.comment;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "comment",
        indexes = {
                @Index(name = "idx_comment_question", columnList = "id_question"),
                @Index(name = "idx_comment_commenter", columnList = "id_commenter"),
                @Index(name = "idx_comment_commenter_question", columnList = "id_question, id_commenter"),
        }
)
@SQLDelete(sql = "UPDATE comment SET deleted_at = NOW() WHERE id_comment = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment", nullable = false, updatable = false)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "varchar(1000) COMMENT '댓글 내용'")
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_commenter_alias", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_comment_commenter_alias"), columnDefinition = "bigint COMMENT '댓글 작성자 별칭 PK'")
    private CommenterAliasEntity commenterAliasEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commenter", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_comment_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_comment_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @Builder
    private CommentEntity(UserEntity userEntity, QuestionEntity questionEntity, CommenterAliasEntity commenterAliasEntity, String content) {
        this.userEntity = userEntity;
        this.questionEntity = questionEntity;
        this.commenterAliasEntity = commenterAliasEntity;
        this.content = content;
    }

    public boolean isCreatedBy(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }

    public void update(String content) {
        this.content = content;
    }

    public String getAlias() {
        return Objects.nonNull(this.commenterAliasEntity) ? this.commenterAliasEntity.getAlias() : null;
    }
}
