package com.chwihae.domain.question;

import com.chwihae.domain.BaseEntity;
import com.chwihae.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.chwihae.domain.question.QuestionStatus.COMPLETED;
import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;
import static com.chwihae.utils.TimeZone.KST;
import static jakarta.persistence.EnumType.STRING;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "question")
@SQLDelete(sql = "UPDATE question SET deleted_at = NOW() WHERE id_question = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class QuestionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_question", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false, columnDefinition = "varchar(50) COMMENT '질문 제목'")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text COMMENT '질문 내용'")
    private String content;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20) COMMENT '질문 상태'")
    private QuestionStatus status;

    @Enumerated(STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(20) COMMENT '질문 타입'")
    private QuestionType type;

    @Column(name = "close_at", nullable = false, columnDefinition = "datetime COMMENT '질문 종료 시간'")
    private LocalDateTime closeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_questioner", nullable = false, foreignKey = @ForeignKey(name = "fk_question_users"), columnDefinition = "bigint COMMENT '사용자 PK'")
    private UserEntity userEntity;

    @Builder
    private QuestionEntity(UserEntity userEntity,
                           String title,
                           String content,
                           QuestionType type,
                           LocalDateTime closeAt) {
        this.userEntity = userEntity;
        this.title = title;
        this.content = content;
        this.status = IN_PROGRESS;
        this.type = type;
        this.closeAt = closeAt;
    }

    public boolean isCreatedBy(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }

    public boolean isClosed() {
        return Objects.equals(this.status, COMPLETED) || this.closeAt.isBefore(LocalDateTime.now(KST));
    }
}
