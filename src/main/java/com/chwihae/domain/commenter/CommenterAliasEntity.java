package com.chwihae.domain.commenter;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "commenter_alias",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_commenter_alias_question_alias", columnNames = {"id_question", "alias"})
        }
)
@SQLDelete(sql = "UPDATE commenter_alias SET deleted_at = NOW() WHERE id_commenter_alias = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class CommenterAliasEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commenter_alias", nullable = false, updatable = false)
    private Long id;

    @Column(name = "alias", nullable = false, updatable = false, columnDefinition = "varchar(50) COMMENT '댓글 작성자 별칭'")
    private String alias;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_commenter", nullable = false, foreignKey = @ForeignKey(name = "fk_commenter_alias_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_commenter_alias_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;
}
