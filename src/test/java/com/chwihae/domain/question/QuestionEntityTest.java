package com.chwihae.domain.question;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QuestionEntityTest {

    @Test
    @DisplayName("작성자이면 true를 반환한다")
    void isCreatedBy_returnsTrue() throws Exception {
        //given
        UserEntity userEntity = UserEntityFixture.of();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        QuestionEntity questionEntity = QuestionEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = questionEntity.isCreatedBy(userEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("작성자가 아니면 false를 반환한다")
    void isCreatedBy_returnsFalse() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of("test1@email.com");
        UserEntity userEntity2 = UserEntityFixture.of("test1@email.com");
        ReflectionTestUtils.setField(userEntity1, "id", 1L);
        ReflectionTestUtils.setField(userEntity2, "id", 2L);

        QuestionEntity questionEntity = QuestionEntity.builder()
                .userEntity(userEntity1)
                .build();

        //when
        boolean result = questionEntity.isCreatedBy(userEntity2.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }
}