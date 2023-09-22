package com.chwihae.service.vote;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.vote.VoteEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.infra.AbstractIntegrationTest;
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
import java.util.concurrent.*;

import static com.chwihae.exception.CustomExceptionError.DUPLICATE_VOTE;
import static com.chwihae.exception.CustomExceptionError.VOTE_NOT_FOUND;

class VoteServiceConcurrencyTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        voteRepository.physicallyDeleteAll();
        optionRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("투표를 동시에 요청해도 투표는 하나만 된다")
    void createVote_concurrency() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity questionEntity = questionRepository.save(createQuestion(questioner));

        List<OptionEntity> optionEntities = new ArrayList<>();
        final int optionSize = 5;
        for (int optionNumber = 1; optionNumber <= optionSize; optionNumber++) {
            optionEntities.add(createOption(questionEntity));
        }
        optionRepository.saveAll(optionEntities);

        final int requestCount = 10;
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int request = 1; request <= requestCount; request++) {
            final int optionId = new Random().nextInt(optionEntities.size());
            OptionEntity optionEntity = optionEntities.get(optionId);
            tasks.add(() -> {
                voteService.createVote(questionEntity.getId(), optionEntity.getId(), voter.getId());
                return null;
            });
        }

        //when
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : futures) {
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
        Assertions.assertThat(exceptionCount).isEqualTo(requestCount - 1);

        //finally
        executorService.shutdown();
    }

    @Test
    @DisplayName("투표를 동시에 취소 요청해도 한 번 삭제 처리된다")
    void deleteVote_concurrency() throws Exception {
        //given
        UserEntity questioner = UserEntityFixture.of();
        UserEntity voter = UserEntityFixture.of();
        userRepository.saveAll(List.of(questioner, voter));
        QuestionEntity question = questionRepository.save(createQuestion(questioner));
        OptionEntity option = optionRepository.save(createOption(question));
        voteRepository.save(createVote(option, voter));

        final int requestCount = 10;
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int request = 1; request <= requestCount; request++) {
            tasks.add(() -> {
                voteService.deleteVote(question.getId(), option.getId(), voter.getId());
                return null;
            });
        }

        //when
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : futures) {
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

        Assertions.assertThat(exceptionCount).isEqualTo(requestCount - 1);
        Assertions.assertThat(voteRepository.findAll()).isEmpty();

        //finally
        executorService.shutdown();
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