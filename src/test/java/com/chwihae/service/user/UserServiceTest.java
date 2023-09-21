package com.chwihae.service.user;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.user.UserContext;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional
class UserServiceTest extends IntegrationTest {

    @Test
    @DisplayName("이메일로 사용자를 저장하여 반환한다")
    void createUser_returnsUserEntity() throws Exception {
        //given
        String email = "test@email.com";

        //when
        userService.createUser(email);

        //then
        Assertions.assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 조회될 경우 UserContext로 반환한다")
    void getUserContextOrException_returnsUserContext() throws Exception {
        //given
        String email = "test@email.com";

        UserEntity saved = userRepository.save(
                UserEntity.builder()
                        .email(email)
                        .build()
        );

        //when
        UserContext userContext = userService.getUserContextOrException(saved.getId());

        //then
        Assertions.assertThat(userContext)
                .extracting("id", "email")
                .containsExactly(saved.getId(), saved.getEmail());
    }

    @Test
    @DisplayName("사용자 아이디로 사용자가 조회되지 않으면 예외가 발생한다")
    void getUserContextOrException_throwsCustomException() throws Exception {
        //given
        long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> userService.getUserContextOrException(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(USER_NOT_FOUND);
    }
}