package com.chwihae.domain.option;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.option.response.Option;
import com.chwihae.infra.test.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
class OptionRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디로 투표 수 합계와 함께 옵션 리스트를 조회한다")
    void findWithVoteCountByQuestionEntityId() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        UserEntity voter4 = UserEntityFixture.of("voter4@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3, voter4));

        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(option1, voter1);
        VoteEntity vote2 = createVote(option1, voter2);
        VoteEntity vote3 = createVote(option2, voter3);
        VoteEntity vote4 = createVote(option2, voter4);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4));

        //when
        List<Option> response = optionRepository.findOptionsWithResultsByQuestionId(questionEntity.getId(), true);

        //then
        Assertions.assertThat(response)
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(2L, 2L);
    }

    @Test
    @DisplayName("질문 아이디로 투표 수 합계 없이 옵션 리스트를 조회한다")
    void findByQuestionEntityId() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        //when
        List<Option> response = optionRepository.findOptionsWithResultsByQuestionId(questionEntity.getId(), false);

        //then
        Assertions.assertThat(response)
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(null, null);
    }

    public QuestionEntity createQuestion(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(LocalDateTime.of(2023, 11, 11, 0, 0))
                .type(QuestionType.SPEC)
                .build();
    }

    public OptionEntity createOption(QuestionEntity questionEntity, String name) {
        return OptionEntity.builder()
                .questionEntity(questionEntity)
                .name(name)
                .build();
    }

    public VoteEntity createVote(OptionEntity optionEntity, UserEntity userEntity) {
        return VoteEntity.builder()
                .questionEntity(optionEntity.getQuestionEntity())
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}