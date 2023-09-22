package com.chwihae.domain.commenter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.chwihae.domain.commenter.CommenterAliasPrefix.별랑이;

class CommenterAliasPrefixTest {

    @Test
    @DisplayName("댓글 작성자 별칭을 반환한다")
    void getAlias_returnsAlias() throws Exception {
        //given
        int sequence = 100;

        //when
        String alias = CommenterAliasPrefix.getAlias(sequence);

        //then
        Assertions.assertThat(alias).isEqualTo(별랑이.name() + sequence);
    }

}