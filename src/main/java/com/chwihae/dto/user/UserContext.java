package com.chwihae.dto.user;

import com.chwihae.domain.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContext implements UserDetails {

    private Long id;
    private String email;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime modifiedAt;
    @JsonIgnore
    private LocalDateTime deletedAt;

    public static UserContext fromEntity(UserEntity userEntity) {
        return new UserContext(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getCreatedAt(),
                userEntity.getModifiedAt(),
                userEntity.getDeletedAt()
        );
    }
    
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return UUID.randomUUID().toString();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return String.valueOf(this.id);
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return deletedAt == null;
    }
}
