package com.chwihae.domain.question;

import com.chwihae.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "question_view")
@SQLDelete(sql = "UPDATE question_view SET deleted_at = NOW() WHERE id_view = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class QuestionViewEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_view", nullable = false, updatable = false)
    private Long id;

    @Column(name = "view_count", nullable = false, columnDefinition = "int COMMENT '질문 조회 수'")
    private Long viewCount = 0L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_question_view_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @Builder
    private QuestionViewEntity(QuestionEntity questionEntity) {
        this.questionEntity = questionEntity;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getQuestionId() {
        return Objects.nonNull(this.questionEntity) ? this.questionEntity.getId() : null;
    }
}
