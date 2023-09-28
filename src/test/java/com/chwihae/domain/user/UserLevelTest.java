package com.chwihae.domain.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserLevelTest {

    @CsvSource({"300,100,PROFESSOR",
            "300,99,DOCTOR",
            "100,30,DOCTOR",
            "100,20,MASTER",
            "20,5,MASTER",
            "20,4,BACHELOR",
            "0,0,BACHELOR"})
    @ParameterizedTest
    @DisplayName("투표수와 댓글수 경계값으로 사용자 레벨을 조회한다")
    void getLevel(long voteCount, long commentCount, UserLevel expected) throws Exception {
        //when
        UserLevel userLevel = UserLevel.getLevel(voteCount, commentCount);

        //then
        Assertions.assertThat(userLevel).isEqualByComparingTo(expected);
    }
}