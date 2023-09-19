package com.chwihae.domain.user;


import com.chwihae.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id_users = ?")
@Where(clause = "deleted_at is NULL")
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_users", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email", updatable = false, nullable = false, unique = true, columnDefinition = "varchar(50) COMMENT '이메일'")
    private String email;

    @Builder
    public UserEntity(String email) {
        this.email = email;
    }
}