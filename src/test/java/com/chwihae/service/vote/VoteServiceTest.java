package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.option.response.VoteOptionResponse;
import com.chwihae.fixture.UserEntityFixture;
import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;

@Transactional
@IntegrationTestSupport
class VoteServiceTest {

    @Autowired
    VoteService voteService;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("질문자는 투표 결과를 조회할 수 있다")
    void getOptions_byQuestioner_returnsOptionVoteResponse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        UserEntity voter4 = UserEntityFixture.of("voter4@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3, voter4));

        LocalDateTime closeAt = LocalDateTime.now().plusDays(10);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(option1, voter1);
        VoteEntity vote2 = createVote(option2, voter2);
        VoteEntity vote3 = createVote(option1, voter3);
        VoteEntity vote4 = createVote(option2, voter4);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4));

        //when
        VoteOptionResponse response = voteService.getVoteOptions(questionEntity.getId(), questioner.getId());

        //then
        Assertions.assertThat(response.isCanViewVoteResult()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("id", "name", "voteCount")
                .containsExactly(
                        tuple(option1.getId(), option1.getName(), 2L),
                        tuple(option2.getId(), option2.getName(), 2L)
                );
    }

    @Test
    @DisplayName("투표자는 투표 결과를 조회할 수 있다")
    void getOptions_byVoter_returnsOptionVoteResponse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        UserEntity voter4 = UserEntityFixture.of("voter4@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3, voter4));

        LocalDateTime closeAt = LocalDateTime.now().plusDays(10);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(option1, voter1);
        VoteEntity vote2 = createVote(option2, voter2);
        VoteEntity vote3 = createVote(option1, voter3);
        VoteEntity vote4 = createVote(option2, voter4);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4));

        //when
        VoteOptionResponse response = voteService.getVoteOptions(questionEntity.getId(), voter1.getId());

        //then
        Assertions.assertThat(response.isCanViewVoteResult()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("id", "name", "voteCount")
                .containsExactly(
                        tuple(option1.getId(), option1.getName(), 2L),
                        tuple(option2.getId(), option2.getName(), 2L)
                );
    }

    @Test
    @DisplayName("투표하지 않은 사용자는 투표 결과를 조회할 수 없다")
    void getOptions_byUserWhoNotVote_returnsOptionVoteResponse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        UserEntity voter4 = UserEntityFixture.of("voter4@email.com");
        UserEntity other = UserEntityFixture.of("other@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3, voter4, other));

        LocalDateTime closeAt = LocalDateTime.now().plusDays(10);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(option1, voter1);
        VoteEntity vote2 = createVote(option2, voter2);
        VoteEntity vote3 = createVote(option1, voter3);
        VoteEntity vote4 = createVote(option2, voter4);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4));

        //when
        VoteOptionResponse response = voteService.getVoteOptions(questionEntity.getId(), other.getId());

        //then
        Assertions.assertThat(response.isCanViewVoteResult()).isFalse();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("id", "name", "voteCount")
                .containsExactly(
                        tuple(option1.getId(), option1.getName(), null),
                        tuple(option2.getId(), option2.getName(), null)
                );
    }

    @Test
    @DisplayName("투표를 하지 않았더라도 질문 마감 시간이 종료되면 투표 결과를 조회할 수 있다")
    void getOptions_byUserWhoNotVote_whenQuestionEnd_returnsOptionVoteResponse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter1 = UserEntityFixture.of("voter1@email.com");
        UserEntity voter2 = UserEntityFixture.of("voter2@email.com");
        UserEntity voter3 = UserEntityFixture.of("voter3@email.com");
        UserEntity voter4 = UserEntityFixture.of("voter4@email.com");
        UserEntity other = UserEntityFixture.of("other@email.com");
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3, voter4, other));

        LocalDateTime closeAt = LocalDateTime.now().minusDays(10);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option1 = createOption(questionEntity, "name1");
        OptionEntity option2 = createOption(questionEntity, "name2");
        optionRepository.saveAll(List.of(option1, option2));

        VoteEntity vote1 = createVote(option1, voter1);
        VoteEntity vote2 = createVote(option2, voter2);
        VoteEntity vote3 = createVote(option1, voter3);
        VoteEntity vote4 = createVote(option2, voter4);
        voteRepository.saveAll(List.of(vote1, vote2, vote3, vote4));

        //when
        VoteOptionResponse response = voteService.getVoteOptions(questionEntity.getId(), other.getId());

        //then
        Assertions.assertThat(response.isCanViewVoteResult()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("id", "name", "voteCount")
                .containsExactly(
                        tuple(option1.getId(), option1.getName(), 2L),
                        tuple(option2.getId(), option2.getName(), 2L)
                );
    }

    public QuestionEntity createQuestion(UserEntity userEntity, LocalDateTime closeAt) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .content("content")
                .closeAt(closeAt)
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
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}