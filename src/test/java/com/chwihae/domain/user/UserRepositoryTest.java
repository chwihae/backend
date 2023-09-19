package com.chwihae.domain.user;

import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@IntegrationTestSupport
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findByEmailTest() throws Exception {
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

}