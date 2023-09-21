package com.chwihae.domain.question;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.fixture.UserEntityFixture;
import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chwihae.domain.question.QuestionStatus.IN_PROGRESS;

@Transactional
@IntegrationTestSupport
class QuestionRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Test
    @DisplayName("질문 상태로 pagination 조회한다")
    void findAllByStatus_returnsPagination() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        QuestionEntity question1 = createQuestion(userEntity, QuestionType.SPEC);
        QuestionEntity question2 = createQuestion(userEntity, QuestionType.COMPANY);
        QuestionEntity question3 = createQuestion(userEntity, QuestionType.ETC);
        QuestionEntity question4 = createQuestion(userEntity, QuestionType.STUDY);
        questionRepository.saveAll(List.of(question1, question2, question3, question4));

        final int pageSize = 2;
        final int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<QuestionEntity> questionEntities = questionRepository.findAllByStatus(IN_PROGRESS, pageRequest);

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