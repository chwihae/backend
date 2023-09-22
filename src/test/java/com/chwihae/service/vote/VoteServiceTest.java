package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.dto.option.response.VoteOptionResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.chwihae.exception.CustomExceptionError.*;

@Transactional
class VoteServiceTest extends AbstractIntegrationTest {

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
        Assertions.assertThat(response.isShowVoteCount()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(2L, 2L);
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
        Assertions.assertThat(response.isShowVoteCount()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(2L, 2L);
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
        Assertions.assertThat(response.isShowVoteCount()).isFalse();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(null, null);
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
        Assertions.assertThat(response.isShowVoteCount()).isTrue();
        Assertions.assertThat(response.getOptions())
                .hasSize(2)
                .extracting("voteCount")
                .containsExactly(2L, 2L);
    }

    @Test
    @DisplayName("마감되지 않은 질문에 질문 작성자가 아닌 투표자는 투표를 할 수 있다")
    void createVote_byUserNotVoteToNotClosedQuestion_pass() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter = UserEntityFixture.of("voter@email.com");
        userRepository.saveAll(List.of(questioner, voter));

        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(questionEntity, "name"));

        //when
        voteService.createVote(questionEntity.getId(), option.getId(), voter.getId());

        //then
        Assertions.assertThat(optionRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("질문 작성자는 투표를 할 수 없다")
    void createVote_byQuestioner_throwsException() throws Exception {
        //given
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(questionEntity, "name"));

        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(questionEntity.getId(), option.getId(), questioner.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("투표자는 마감된 질문에는 투표를 할 수 없다")
    void createVote_whenQuestionClosed_throwsException() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter = UserEntityFixture.of("voter@email.com");
        userRepository.saveAll(List.of(questioner, voter));
        LocalDateTime closeAt = LocalDateTime.now().minusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(questionEntity, "name"));

        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(questionEntity.getId(), option.getId(), voter.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(QUESTION_CLOSED);
    }

    @Test
    @DisplayName("투표자는 중복 투표를 할 수 없다")
    void createVote_whenDuplicatedVote_throwsException() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter = UserEntityFixture.of("voter@email.com");
        userRepository.saveAll(List.of(questioner, voter));
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity option = optionRepository.save(createOption(questionEntity, "name"));

        voteService.createVote(questionEntity.getId(), option.getId(), voter.getId());

        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(questionEntity.getId(), option.getId(), voter.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(DUPLICATE_VOTE);
    }

    @Test
    @DisplayName("투표자는 존재하지 않은 질문에 투표를 할 수 없다")
    void createVote_whenQuestionNotExists_throwsException() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter = UserEntityFixture.of("voter@email.com");
        userRepository.saveAll(List.of(questioner, voter));
        long notExistingQuestionId = 0L;
        long notExistingOptionId = 0L;
        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(notExistingQuestionId, notExistingOptionId, voter.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("투표자는 존재하지 않은 옵션에 투표를 할 수 없다")
    void createVote_whenOptionNotExists_throwsException() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("questioner@email.com");
        UserEntity voter = UserEntityFixture.of("voter@email.com");
        userRepository.saveAll(List.of(questioner, voter));
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        long notExistingOptionId = 0L;
        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(questionEntity.getId(), notExistingOptionId, voter.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("투표자는 존재하지 않은 옵션에 투표를 할 수 없다")
    void createVote_whenVoterNotExists_throwsException() throws Exception {
        //given
        long notExistingVoterId = 0L;
        UserEntity questioner = userRepository.save(UserEntityFixture.of("questioner@email.com"));
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner, closeAt));
        OptionEntity optionEntity = optionRepository.save(createOption(questionEntity, "name"));
        //when //then
        Assertions.assertThatThrownBy(() -> voteService.createVote(questionEntity.getId(), optionEntity.getId(), notExistingVoterId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(USER_NOT_FOUND);
    }

    // TODO 마감되지 않은 질문에 투표를 취소했던 사용자는 다시 투표를 할 수 있다

    @Test
    @DisplayName("사용자가 투표를 취소하면 투표를 삭제한다")
    void deleteVote_pass() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(user, closeAt));
        OptionEntity option = optionRepository.save(createOption(question, "name"));
        voteRepository.save(createVote(option, user));

        //when
        voteService.deleteVote(question.getId(), option.getId(), user.getId());

        //then
        Assertions.assertThat(voteRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 투표하지 않았는데 투표를 취소하면 예외가 발생한다")
    void deleteVote_throwsException() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        LocalDateTime closeAt = LocalDateTime.now().plusDays(1);
        QuestionEntity question = questionRepository.save(createQuestion(user, closeAt));
        OptionEntity option = optionRepository.save(createOption(question, "name"));

        //when //then
        Assertions.assertThatThrownBy(() -> voteService.deleteVote(question.getId(), option.getId(), user.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(VOTE_NOT_FOUND);
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
                .questionEntity(optionEntity.getQuestionEntity())
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}