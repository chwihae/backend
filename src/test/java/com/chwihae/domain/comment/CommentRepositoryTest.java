package com.chwihae.domain.comment;

import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
class CommentRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디와 사용자 아이디로 댓글과 별칭을 함께 조회한다")
    void findWithCommenterAliasByQuestionEntityIdAndUserEntityId() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        String alias = "test alias";
        CommenterAliasEntity commenterAlias = commenterAliasRepository.save(createAlias(alias, question, user));
        String content = "content";
        commentRepository.save(createComment(question, user, commenterAlias, content));

        //when
        Optional<CommentEntity> optionalComment = commentRepository.findTopWithCommenterAliasByQuestionEntityIdAndUserEntityId(question.getId(), user.getId());

        //then
        Assertions.assertThat(optionalComment)
                .isPresent()
                .hasValueSatisfying(
                        it -> {
                            Assertions.assertThat(it.getContent()).isEqualTo(content);
                            Assertions.assertThat(it.getCommenterAliasEntity().getAlias()).isEqualTo(alias);
                        }
                );
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

    public CommentEntity createComment(QuestionEntity questionEntity, UserEntity userEntity, CommenterAliasEntity commenterAliasEntity, String content) {
        return CommentEntity.builder()
                .content(content)
                .userEntity(userEntity)
                .questionEntity(questionEntity)
                .commenterAliasEntity(commenterAliasEntity)
                .build();
    }
}