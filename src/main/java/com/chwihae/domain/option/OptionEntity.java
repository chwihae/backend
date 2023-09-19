package com.chwihae.domain.option;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.question.QuestionEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "option")
@SQLDelete(sql = "UPDATE option SET deleted_at = NOW() WHERE id_option = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class OptionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_option", nullable = false, updatable = false)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "varchar(100) COMMENT '옵션 내용'")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_option_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @Builder
    private OptionEntity(String content, QuestionEntity questionEntity) {
        this.content = content;
        this.questionEntity = questionEntity;
    }
}
