package com.chwihae.domain.commenter;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.test.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
class CommenterAliasRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디, 사용자 아이디 조합은 유니크 값이다")
    void uk_commenter_alias_question_alias() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        String duplicatedAlias = "alias";
        commenterAliasRepository.save(createAlias(duplicatedAlias, question, user));

        //when //then
        Assertions.assertThatThrownBy(() -> commenterAliasRepository.save(createAlias(duplicatedAlias, question, user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .type(QuestionType.ETC)
                .build();
    }

    public CommenterAliasEntity createAlias(String alias, QuestionEntity questionEntity, UserEntity userEntity) {
        return CommenterAliasEntity.builder()
                .alias(alias)
                .questionEntity(questionEntity)
                .userEntity(userEntity)
                .build();
    }

}