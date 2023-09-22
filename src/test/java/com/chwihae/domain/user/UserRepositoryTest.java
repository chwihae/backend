package com.chwihae.domain.user;

import com.chwihae.infra.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("존재하는 이메일로 사용자 조회 시 사용자를 반환한다")
    void findByEmail_returnsPresent() throws Exception {
        //given
        String email = "test@email.com";

        userRepository.save(UserEntity.builder()
                .email(email)
                .build());

        //when
        Optional<UserEntity> result = userRepository.findByEmail(email);

        //then
        Assertions.assertThat(result).isPresent();
    }

    @Test
    @DisplayName("등록된 이메일로 조회 시 true 반환한다")
    void existsByEmail_returnsTrue() throws Exception {
        //given
        String email = "test@email.com";

        userRepository.save(UserEntity.builder()
                .email(email)
                .build());

        //when
        boolean result = userRepository.existsByEmail(email);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("미등록 이메일로 조회 시 false 반환한다")
    void existsByEmail_returnsFalse() throws Exception {
        //given
        String email = "test@email.com";

        //when
        boolean result = userRepository.existsByEmail(email);

        //then
        Assertions.assertThat(result).isFalse();
    }
}