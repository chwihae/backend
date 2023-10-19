package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.fixture.OptionEntityFixture;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.fixture.VoteEntityFixture;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import com.chwihae.utils.ClassUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        executorService.shutdown();
        voteRepository.physicallyDeleteAll();
        optionRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("다수의 사용자가 동시에 질문에 투표를 해도 모든 투표가 정상 등록된다")
    void createVote_withMultipleRequests_concurrency() throws Exception {
        //given
        final int TOTAL_USER_SIZE = 100;
        final int TOTAL_OPTION_SIZE = 5;

        UserEntity questioner = UserEntityFixture.of();
        userRepository.save(questioner);

        List<UserEntity> voters = new ArrayList<>();
        IntStream.range(0, TOTAL_USER_SIZE).forEach(idx -> {
            voters.add(UserEntityFixture.of());
        });
        userRepository.saveAll(voters);

        QuestionEntity questionEntity = questionRepository.save(QuestionEntityFixture.of(questioner));
        List<OptionEntity> optionEntities = new ArrayList<>();
        IntStream.of(0, TOTAL_OPTION_SIZE).forEach(optionNumber -> {
            optionEntities.add(OptionEntityFixture.of(questionEntity));
        });
        optionRepository.saveAll(optionEntities);

        List<Callable<Void>> createVoteTasks = doGenerateConcurrentTasks(TOTAL_USER_SIZE, (userIndex) -> {
            int randomOptionId = new Random().nextInt(optionEntities.size());
            OptionEntity optionEntity = optionEntities.get(randomOptionId);
            return () -> {
                voteService.createVote(questionEntity.getId(), optionEntity.getId(), voters.get(userIndex).getId());
                return null;
            };
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

        Assertions.assertThat(voteRepository.findAll()).hasSize(TOTAL_USER_SIZE);
        Assertions.assertThat(exceptionCount).isZero();
    }

    @Test
    @DisplayName("사용자가 같은 질문에 대해서 여러 옵션에 동시에 투표를 요청해도 투표는 하나만 생성하고, 나머지 요청은 예외를 던진다")
    void createVote_concurrency() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;
        final int TOTAL_OPTION_SIZE = 5;

        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity questionEntity = questionRepository.save(QuestionEntityFixture.of(questioner));

        List<OptionEntity> optionEntities = new ArrayList<>();
        IntStream.of(0, TOTAL_OPTION_SIZE).forEach(optionNumber -> {
            optionEntities.add(OptionEntityFixture.of(questionEntity));
        });
        optionRepository.saveAll(optionEntities);

        List<Callable<Void>> createVoteTasks = doGenerateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            int randomOptionId = new Random().nextInt(optionEntities.size());
            OptionEntity optionEntity = optionEntities.get(randomOptionId);
            voteService.createVote(questionEntity.getId(), optionEntity.getId(), voter.getId());
            return null;
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
        Assertions.assertThat(exceptionCount).isEqualTo(TOTAL_REQUEST_COUNT - 1);
    }

    @Test
    @DisplayName("사용자가 투표했던 옵션을 동시에 취소 요청해도 한 번 처리되고, 나머지 요청은 예외를 던진다")
    void deleteVote_concurrency() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;

        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity question = questionRepository.save(QuestionEntityFixture.of(questioner));
        OptionEntity option = optionRepository.save(OptionEntityFixture.of(question));
        voteRepository.save(VoteEntityFixture.of(option, voter));

        List<Callable<Void>> deleteVoteTasks = doGenerateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            voteService.deleteVote(question.getId(), option.getId(), voter.getId());
            return null;
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

        Assertions.assertThat(exceptionCount).isEqualTo(TOTAL_REQUEST_COUNT - 1);
        Assertions.assertThat(voteRepository.findAll()).isEmpty();
    }
}