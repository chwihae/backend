package com.chwihae.domain.vote;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.option.OptionEntity;
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
@Table(name = "vote")
@SQLDelete(sql = "UPDATE vote SET deleted_at = NOW() WHERE id_vote = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class VoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vote", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_option", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_option"), columnDefinition = "bigint COMMENT '옵션 PK'")
    private OptionEntity optionEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voter", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @Builder
    private VoteEntity(OptionEntity optionEntity, UserEntity userEntity) {
        this.optionEntity = optionEntity;
        this.userEntity = userEntity;
    }
}
