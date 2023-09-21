package com.chwihae.domain.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.fixture.UserEntityFixture;
import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@IntegrationTestSupport
class VoteRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    VoteRepository voteRepository;

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
                .optionEntity(optionEntity)
                .userEntity(userEntity)
                .build();
    }
}