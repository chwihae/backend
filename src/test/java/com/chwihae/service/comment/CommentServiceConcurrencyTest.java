package com.chwihae.service.comment;

import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractConcurrencyTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

class CommentServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        commentRepository.physicallyDeleteAll();
        commenterAliasRepository.physicallyDeleteAll();
        commenterSequenceRepository.physicallyDeleteAll();
        questionRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("질문에 여러 사용자들이 동시에 댓글을 남겨도 댓글 작성자 별칭은 겹치지 않고 순서대로 생성된다")
    void createComment_withConcurrency() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;
        final int TOTAL_USER_COUNT = 10;

        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question = questionRepository.save(createQuestion(user));
        commenterSequenceRepository.save(createSequence(question));

        List<UserEntity> userEntities = new ArrayList<>();
        IntStream.range(0, TOTAL_USER_COUNT).forEach(userIdx -> {
            userEntities.add(UserEntityFixture.of());
        });
        userRepository.saveAll(userEntities);

        List<Callable<Void>> createCommentTasks = doGenerateConcurrentTasks(TOTAL_REQUEST_COUNT, (userIndex) -> {
            Long userId = userEntities.get(userIndex).getId();
            return () -> {
                commentService.createComment(question.getId(), userId, "content");
                return null;
            };
        });

        //when
        List<Future<Void>> createCommentFutures = executorService.invokeAll(createCommentTasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : createCommentFutures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptionCount++;
            }
        }

        Assertions.assertThat(exceptionCount).isZero();
        Assertions.assertThat(commentRepository.findAll()).hasSize(TOTAL_REQUEST_COUNT);
        Assertions.assertThat(commenterAliasRepository.findAll())
                .extracting("alias")
                .doesNotHaveDuplicates();
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