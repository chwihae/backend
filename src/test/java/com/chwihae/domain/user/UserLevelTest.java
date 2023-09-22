package com.chwihae.domain.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.chwihae.domain.user.UserLevel.BACHELOR;

class UserLevelTest {

    @Test
    @DisplayName("사용자 레벨을 조회한다")
    void getLevel() throws Exception {
        //given
        long voteCount = 10L;
        long commentCount = 10L;

        //when
        UserLevel userLevel = UserLevel.getLevel(voteCount, commentCount);

        //then
        Assertions.assertThat(userLevel).isEqualByComparingTo(BACHELOR);
    }

}