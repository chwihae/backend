package com.chwihae.service.question;

import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionDetailResponse;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.fixture.UserEntityFixture;
import com.chwihae.infra.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional
@IntegrationTestSupport
class QuestionServiceTest {

    @Autowired
    QuestionService questionService;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("질문과 옵션을 저장한 후 질문 아이디를 반환한다")
    void createQuestionWithOptions_returnsQuestionId() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        LocalDateTime closeAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30);

        final int optionSize = 2;
        List<OptionCreateRequest> options = new ArrayList<>();
        for (int optionName = 1; optionName <= optionSize; optionName++) {
            options.add(OptionCreateRequest.builder()
                    .name("option name " + optionName)
                    .build()
            );
        }

        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .title("title")
                .type(QuestionType.SPEC)
                .closeAt(closeAt)
                .content("content")
                .options(options)
                .build();

        //when
        Long id = questionService.createQuestion(request, userEntity.getId());

        //then
        Assertions.assertThat(id).isNotNull();
        Assertions.assertThat(questionRepository.findAll()).hasSize(1);
        Assertions.assertThat(optionRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("등록 시 사용자가 조회되지 않으면 예외가 발생한다")
    void createQuestionWithOptions_whenUserNotFound_throwsCustomException() throws Exception {
        //given
        long notExistingUserId = 0L;
        QuestionCreateRequest request = QuestionCreateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.createQuestion(request, notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("질문 아이디로 조회하여 질문을 반환한다")
    void getQuestion_returnsQuestionResponse() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        QuestionEntity questionEntity = questionRepository.save(createQuestion(userEntity));

        //when
        QuestionDetailResponse response = questionService.getQuestion(questionEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("title", "content", "type", "editable")
                .containsExactly(questionEntity.getTitle(), questionEntity.getContent(), questionEntity.getType(), true);
    }

    @Test
    @DisplayName("존재하지 않는 질문 아이디로 조회하면 예외가 발생한다")
    void getQuestion_withNotExistingQuestionId_throwsCustomException() throws Exception {
        //given
        long notExistingQuestionId = 0L;
        long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> questionService.getQuestion(notExistingQuestionId, notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.QUESTION_NOT_FOUND);
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
}