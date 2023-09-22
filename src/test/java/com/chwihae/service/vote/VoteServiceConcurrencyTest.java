package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.utils.ClassUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static com.chwihae.exception.CustomExceptionError.DUPLICATE_VOTE;
import static com.chwihae.exception.CustomExceptionError.VOTE_NOT_FOUND;

class VoteServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        voteRepository.physicallyDeleteAll();
        optionRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
        executorService.shutdown();
    }

    @Test
    @DisplayName("투표를 동시에 요청해도 투표는 하나만 된다")
    void createVote_concurrency() throws Exception {
        //given
        final int REQUEST_COUNT = 10;
        final int OPTION_SIZE = 5;
        List<Callable<Void>> createVoteTasks = new ArrayList<>();

        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));

        List<OptionEntity> optionEntities = new ArrayList<>();
        IntStream.of(0, OPTION_SIZE).forEach(optionNumber -> {
            optionEntities.add(createOption(questionEntity));
        });
        optionRepository.saveAll(optionEntities);

        IntStream.rangeClosed(1, REQUEST_COUNT).forEach(req -> {
            int optionId = new Random().nextInt(optionEntities.size());
            OptionEntity optionEntity = optionEntities.get(optionId);
            createVoteTasks.add(() -> {
                voteService.createVote(questionEntity.getId(), optionEntity.getId(), voter.getId());
                return null;
            });
        });


        //when
        List<Future<Void>> createVoteFutures = executorService.invokeAll(createVoteTasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : createVoteFutures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Assertions.assertThat(e.getCause()).isInstanceOf(CustomException.class);
                CustomException customException = ClassUtils.getSafeCastInstance(e.getCause(), CustomException.class);
                if (customException.error() == DUPLICATE_VOTE) {
                    exceptionCount++;
                }
            }
        }

        Assertions.assertThat(voteRepository.findAll()).hasSize(1);
        Assertions.assertThat(exceptionCount).isEqualTo(REQUEST_COUNT - 1);
    }

    @Test
    @DisplayName("투표를 동시에 취소 요청해도 한 번 삭제 처리된다")
    void deleteVote_concurrency() throws Exception {
        //given
        final int REQUEST_COUNT = 10;
        List<Callable<Void>> deleteVoteTasks = new ArrayList<>();

        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        OptionEntity option = optionRepository.save(createOption(question));
        voteRepository.save(createVote(option, voter));

        IntStream.rangeClosed(1, REQUEST_COUNT).forEach(req -> {
            deleteVoteTasks.add(() -> {
                voteService.deleteVote(question.getId(), option.getId(), voter.getId());
                return null;
            });
        });

        //when
        List<Future<Void>> deleteVoteFutures = executorService.invokeAll(deleteVoteTasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : deleteVoteFutures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Assertions.assertThat(e.getCause()).isInstanceOf(CustomException.class);
                CustomException customException = ClassUtils.getSafeCastInstance(e.getCause(), CustomException.class);
                if (customException.error() == VOTE_NOT_FOUND) {
                    exceptionCount++;
                }
            }
        }

        Assertions.assertThat(exceptionCount).isEqualTo(REQUEST_COUNT - 1);
        Assertions.assertThat(voteRepository.findAll()).isEmpty();
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