package com.chwihae.domain.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
class VoteRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OptionRepository optionRepository;

    @Autowired
    protected QuestionRepository questionRepository;

    @Autowired
    protected VoteRepository voteRepository;

    @Autowired
    protected EntityManager entityManager;

    @Test
    @DisplayName("사용자가 질문에 투표를 하였으면 true를 리턴한다")
    void existsByQuestionIdAndUserId_returnsTrue() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("test1@email.com");
        UserEntity voter = UserEntityFixture.of("test2@email.com");
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));
        OptionEntity option1 = createOption(questionEntity);
        OptionEntity option2 = createOption(questionEntity);
        optionRepository.saveAll(List.of(option1, option2));

        voteRepository.save(createVote(option1, voter));

        //when
        boolean result = voteRepository.existsByQuestionEntityIdAndUserEntityId(questionEntity.getId(), voter.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 투표하지 않았으면 false를 리턴한다")
    void existsByQuestionIdAndUserId_returnsFalse() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of("test1@email.com");
        UserEntity other = UserEntityFixture.of("test2@email.com");
        userRepository.saveAll(List.of(questioner, other));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));

        //when
        boolean result = voteRepository.existsByQuestionEntityIdAndUserEntityId(questionEntity.getId(), other.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("질문 아이디와 사용자 아이디로 투표를 조회한다")
    void findByQuestionEntityIdAndUserEntityId_returnPresent() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        OptionEntity option = optionRepository.save(createOption(question));
        VoteEntity vote = voteRepository.save(createVote(option, user));

        //when
        Optional<VoteEntity> optionalVoteEntity = voteRepository.findByQuestionEntityIdAndUserEntityId(question.getId(), user.getId());

        //then
        Assertions.assertThat(optionalVoteEntity).isPresent();
    }

    @Test
    @DisplayName("같은 옵션에 중복 투표하면 예외가 발생한다")
    void save_whenExistingOption_throwsException() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity));
        OptionEntity optionEntity = optionRepository.save(createOption(questionEntity));

        voteRepository.save(createVote(optionEntity, userEntity));

        //when //then
        Assertions.assertThatThrownBy(() -> voteRepository.save(createVote(optionEntity, userEntity)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("같은 옵션에 중복 저장, 삭제가 가능하다")
    void delete_repeatable() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity));
        OptionEntity optionEntity = optionRepository.save(createOption(questionEntity));

        VoteEntity save1 = voteRepository.save(createVote(optionEntity, userEntity));
        voteRepository.delete(save1);
        entityManager.flush();
        entityManager.clear();

        VoteEntity save2 = voteRepository.save(createVote(optionEntity, userEntity));

        //when
        voteRepository.delete(save2);
        entityManager.flush();
        entityManager.clear();

        //then
        Assertions.assertThat(voteRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 투표를 하였으면 투표 엔티티를 반환한다")
    void findByQuestionEntityIdAndOptionEntityIdAndUserEntityId_returnPresent() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        OptionEntity option = optionRepository.save(createOption(question));
        voteRepository.save(createVote(option, user));

        //when
        Optional<VoteEntity> optionalVote = voteRepository.findForUpdateByQuestionEntityIdAndOptionEntityIdAndUserEntityId(question.getId(), option.getId(), user.getId());

        //then
        Assertions.assertThat(optionalVote).isPresent();
    }

    @Test
    @DisplayName("사용자가 투표를 하지 않았으면 null을 반환한다")
    void findByQuestionEntityIdAndOptionEntityIdAndUserEntityId_returnEmpty() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        OptionEntity option = optionRepository.save(createOption(question));

        //when
        Optional<VoteEntity> optionalVote = voteRepository.findForUpdateByQuestionEntityIdAndOptionEntityIdAndUserEntityId(question.getId(), option.getId(), user.getId());

        //then
        Assertions.assertThat(optionalVote).isEmpty();
    }

    @Test
    @DisplayName("이미 삭제된 투표를 다시 삭제해도 아무런 문제가 없다")
    void delete_repetable() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        OptionEntity option = optionRepository.save(createOption(question));
        VoteEntity vote = voteRepository.save(createVote(option, user));

        voteRepository.delete(vote);
        entityManager.flush();
        entityManager.clear();

        //when //then
        voteRepository.delete(vote);
    }

    @Test
    @DisplayName("질문 아이디로 투표 수를 집계한다")
    void countByQuestionEntityId_returnCount() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter1 = UserEntityFixture.of();
        UserEntity voter2 = UserEntityFixture.of();
        UserEntity voter3 = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter1, voter2, voter3));

        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        OptionEntity option = optionRepository.save(createOption(question));

        VoteEntity vote1 = createVote(option, voter1);
        VoteEntity vote2 = createVote(option, voter2);
        VoteEntity vote3 = createVote(option, voter3);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when
        long voteCount = voteRepository.countByQuestionEntityId(question.getId());

        //then
        Assertions.assertThat(voteCount).isEqualTo(3L);
    }

    @Test
    @DisplayName("사용자 아이디로 투표 수를 집계한다")
    void countByUserEntityId_returnCount() throws Exception {
        //given
        UserEntity questioner1 = UserEntityFixture.of();
        UserEntity questioner2 = UserEntityFixture.of();
        UserEntity questioner3 = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner1, questioner2, questioner3, voter));

        QuestionEntity question1 = createQuestion(questioner1);
        QuestionEntity question2 = createQuestion(questioner2);
        QuestionEntity question3 = createQuestion(questioner3);
        questionRepository.saveAll(List.of(question1, question2, question3));

        OptionEntity option1 = createOption(question1);
        OptionEntity option2 = createOption(question2);
        OptionEntity option3 = createOption(question3);
        optionRepository.saveAll(List.of(option1, option2, option3));

        VoteEntity vote1 = createVote(option1, voter);
        VoteEntity vote2 = createVote(option2, voter);
        VoteEntity vote3 = createVote(option3, voter);
        voteRepository.saveAll(List.of(vote1, vote2, vote3));

        //when
        long voteCount = voteRepository.countByUserEntityId(voter.getId());

        //then
        Assertions.assertThat(voteCount).isEqualTo(3L);
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

    public OptionEntity createOption(QuestionEntity questionEntity) {
        return OptionEntity.builder()
                .questionEntity(questionEntity)
                .name("name")
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