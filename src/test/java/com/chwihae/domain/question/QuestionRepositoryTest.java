package com.chwihae.domain.question;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.test.AbstractIntegrationTest;
import com.chwihae.infra.fixture.UserEntityFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;

@Transactional
class QuestionRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 상태, 질문 타입으로 pagination 조회한다")
    void findByStatusAndType_returnsPagination() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = createQuestion(userEntity, QuestionType.SPEC);
        QuestionEntity question2 = createQuestion(userEntity, QuestionType.COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, QuestionType.ETC);
        QuestionEntity question4 = createQuestion(userEntity, QuestionType.SPEC);
        QuestionEntity question5 = createQuestion(userEntity, QuestionType.CAREER);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        final int pageSize = 2;
        final int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<QuestionEntity> questionEntities = questionRepository.findByTypeAndStatus(IN_PROGRESS, QuestionType.SPEC, pageRequest);

        //then
        Assertions.assertThat(questionEntities.getContent()).hasSize(pageSize);
    }

    private QuestionEntity createQuestion(UserEntity userEntity, QuestionType type) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title("title")
                .type(type)
                .closeAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10))
                .content("content")
                .build();
    }

}