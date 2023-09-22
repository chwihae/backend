package com.chwihae.domain.bookmark;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "bookmark",
        indexes = {
                @Index(name = "idx_bookmark_user", columnList = "id_users"),
                @Index(name = "idx_bookmark_question", columnList = "id_question"),
                @Index(name = "idx_bookmark_question_user", columnList = "id_question, id_users")
        }
)
@SQLDelete(sql = "UPDATE bookmark SET deleted_at = NOW() WHERE id_bookmark = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class BookmarkEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bookmark", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmark_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmark_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @Builder
    private BookmarkEntity(UserEntity userEntity, QuestionEntity questionEntity) {
        this.userEntity = userEntity;
        this.questionEntity = questionEntity;
    }
}
