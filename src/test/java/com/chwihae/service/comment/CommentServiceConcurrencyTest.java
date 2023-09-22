package com.chwihae.service.comment;

import com.chwihae.domain.commenter.CommenterAliasEntity;
import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class CommentServiceConcurrencyTest extends AbstractIntegrationTest {

    @AfterEach
    void tearDown() {
        commentRepository.physicallyDeleteAll();
        commenterAliasRepository.physicallyDeleteAll();
        commenterSequenceRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("같은 질문에 여러 사용자가 동시에 댓글을 남겨도 댓글 작성자 별칭은 겹치지 않고 순서대로 생성된다")
    void createComment_withConcurrency() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        CommenterSequenceEntity sequence = commenterSequenceRepository.save(createSequence(question));

        final int userCount = 10;
        List<UserEntity> userEntities = new ArrayList<>();
        for (int userIdx = 0; userIdx < userCount; userIdx++) {
            userEntities.add(UserEntityFixture.of());
        }
        userRepository.saveAll(userEntities);

        final int requestCount = 10;
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int request = 1; request <= requestCount; request++) {
            Long userId = userEntities.get(request - 1).getId();
            tasks.add(() -> {
                commentService.createComment(question.getId(), userId, "content");
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
                exceptionCount++;
            }
        }

        Assertions.assertThat(exceptionCount).isZero();
        Assertions.assertThat(commentRepository.findAll()).hasSize(requestCount);

        List<String> allAliases = commenterAliasRepository.findAll().stream()
                .map(CommenterAliasEntity::getAlias)
                .toList();

        long uniqueCount = allAliases.stream().distinct().count();

        Assertions.assertThat(allAliases)
                .hasSize(requestCount)
                .hasSize((int) uniqueCount);

        //finally
        executorService.shutdown();
    }

    public CommenterSequenceEntity createSequence(QuestionEntity questionEntity) {
        return CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build();
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
}