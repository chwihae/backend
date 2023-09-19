package com.chwihae.service.user;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTestSupport
class UserInfoServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;
    
    @Test
    @DisplayName("이메일로 사용자를 저장하여 반환한다")
    void createUserTest() throws Exception {
        //given
        String email = "test@email.com";

        //when
        UserEntity userEntity = userService.createUser(email);

        //then
        Assertions.assertThat(userRepository.findAll()).hasSize(1);
    }

}