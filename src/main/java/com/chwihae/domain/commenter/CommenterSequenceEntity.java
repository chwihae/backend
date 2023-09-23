package com.chwihae.domain.commenter;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.question.QuestionEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "commenter_sequence",
        indexes = {
                @Index(name = "idx_commenter_seq_question", columnList = "id_question")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_commenter_seq_question", columnNames = "id_question")
        }

)
@SQLDelete(sql = "UPDATE commenter_sequence SET deleted_at = NOW() WHERE id_commenter_seq = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class CommenterSequenceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commenter_seq", nullable = false, updatable = false)
    private Long id;

    @Column(name = "sequence", nullable = false, columnDefinition = "int COMMENT '댓글 작성자 부여 번호'")
    private int sequence = 0;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_commenter_sequence_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @Builder
    private CommenterSequenceEntity(QuestionEntity questionEntity) {
        this.questionEntity = questionEntity;
    }
}
