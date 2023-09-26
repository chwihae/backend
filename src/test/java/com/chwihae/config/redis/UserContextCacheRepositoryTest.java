package com.chwihae.config.redis;

import com.chwihae.dto.user.UserContext;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class UserContextCacheRepositoryTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        userContextCacheRepository.clear();
    }

    @Test
    @DisplayName("사용자 컨텍스트를 캐시에 저장하고 가져올 수 있다")
    void setUserContextAndGetUserContext() {
        // given
        Long userId = 1L;
        UserContext userContext = new UserContext(userId, "email@example.com", null, null, null);

        // when
        userContextCacheRepository.setUserContext(userContext);
        Optional<UserContext> retrievedUserContext = userContextCacheRepository.getUserContext(userId);

        // then
        Assertions.assertThat(retrievedUserContext)
                .isPresent()
                .hasValueSatisfying(it -> {
                    Assertions.assertThat(it.getId()).isEqualTo(userId);
                });
    }

    @Test
    @DisplayName("캐시에 없는 사용자 컨텍스트를 가져오면 empty를 반환한다")
    void getUserContext_returnEmpty() {
        // given
        Long userId = 2L;

        // when
        Optional<UserContext> retrievedUserContext = userContextCacheRepository.getUserContext(userId);

        // then
        Assertions.assertThat(retrievedUserContext).isEmpty();
    }
}
