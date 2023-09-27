package com.chwihae.service.user;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class UserServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("가입되지 않은 사용자 이메일로 동시에 로그인을 하여도 사용자는 하나만 생성된다")
    void getOrCreateUser_pass() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;
        String email = "test@email.com";

        List<Callable<UserEntity>> getOrCreateUserTasks = doGenerateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> userService.getOrCreateUser(email));

        //when
        List<Future<UserEntity>> futures = executorService.invokeAll(getOrCreateUserTasks);

        //then
        for (Future<UserEntity> future : futures) {
            try {
                future.get();
            } catch (ExecutionException exception) {
            }
        }

        Assertions.assertThat(userRepository.findAll()).hasSize(1);
    }
}