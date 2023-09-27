package com.chwihae.service.question.core;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.question.request.QuestionCreateRequest;
import com.chwihae.service.commenter.CommenterSequenceService;
import com.chwihae.service.option.OptionService;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestionCreateService {

    private final UserService userService;
    private final QuestionRepository questionRepository;
    private final QuestionViewService questionViewService;
    private final OptionService optionService;
    private final CommenterSequenceService commenterSequenceService;

    public Long createQuestion(QuestionCreateRequest request, Long userId) {
        UserEntity userEntity = userService.findUserOrException(userId);
        QuestionEntity questionEntity = questionRepository.save(request.toEntity(userEntity));
        optionService.createOptions(request.getOptions(), questionEntity);
        commenterSequenceService.createCommenterSequence(questionEntity);
        questionViewService.createQuestionView(questionEntity);
        return questionEntity.getId();
    }
}
