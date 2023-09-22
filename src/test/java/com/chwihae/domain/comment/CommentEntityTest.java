package com.chwihae.domain.comment;

import com.chwihae.domain.user.UserEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentEntityTest {

    @Test
    @DisplayName("댓글 작성자이면 true를 반환한다")
    void isCreatedBy_returnsTrue() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = commentEntity.isCreatedBy(userEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("댓글 작성자이면 false를 반환한다")
    void isCreatedBy_returnsFalse() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .build();

        long otherUserId = 10L;

        //when
        boolean result = commentEntity.isCreatedBy(otherUserId);

        //then
        Assertions.assertThat(result).isFalse();
    }

}