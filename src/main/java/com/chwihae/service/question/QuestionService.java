package com.chwihae.service.question;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.dto.question.response.QuestionResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    @Transactional
    public Long createQuestion(QuestionCreateRequest request, Long userId) {
        UserEntity userEntity = findUserOrException(userId);
        QuestionEntity questionEntity = questionRepository.save(request.toEntity(userEntity));
        optionRepository.saveAll(buildOptionEntities(request.getOptions(), questionEntity));
        return questionEntity.getId();
    }

    public QuestionResponse getQuestion(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        boolean isEditable = questionEntity.isCreatedBy(userId);
        return QuestionResponse.of(questionEntity, isEditable);
    }

    private List<OptionEntity> buildOptionEntities(List<OptionCreateRequest> options, QuestionEntity questionEntity) {
        return options.stream()
                .map(option -> OptionEntity.builder()
                        .questionEntity(questionEntity)
                        .name(option.getName())
                        .build())
                .toList();
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }
}
