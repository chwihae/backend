package com.chwihae.domain.vote;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
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
@Table(name = "vote",
        indexes = {
                @Index(name = "idx_vote_id_voter", columnList = "id_voter"),
                @Index(name = "idx_vote_id_question", columnList = "id_question"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vote_user_question_valid", columnNames = {"id_voter", "id_question", "valid"})
        }
)
@SQLDelete(sql = "UPDATE vote SET deleted_at = NOW(), valid = NULL WHERE id_vote = ?")
@Where(clause = "deleted_at is NULL and valid = true")
@Entity
public class VoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vote", nullable = false, updatable = false)
    private Long id;

    @Column(name = "valid", columnDefinition = "bit DEFAULT 1 COMMENT '투표 유니크 검증을 위한 필드'")
    private Boolean valid = true;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_question", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_question"), columnDefinition = "bigint COMMENT '질문 PK'")
    private QuestionEntity questionEntity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_option", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_option"), columnDefinition = "bigint COMMENT '옵션 PK'")
    private OptionEntity optionEntity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_voter", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @Builder
    private VoteEntity(QuestionEntity questionEntity, OptionEntity optionEntity, UserEntity userEntity) {
        this.questionEntity = questionEntity;
        this.optionEntity = optionEntity;
        this.userEntity = userEntity;
    }
}