package com.chwihae.service.option;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.dto.option.request.OptionCreateRequest;
import com.chwihae.dto.option.response.Option;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.chwihae.exception.CustomExceptionError.OPTION_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OptionService {

    private final OptionRepository optionRepository;

    public OptionEntity findOptionOrException(Long optionId) {
        return optionRepository.findById(optionId).orElseThrow(() -> new CustomException(OPTION_NOT_FOUND));
    }

    public List<Option> findOptionsWithResultsByQuestionId(Long questionId, boolean showVoteCount) {
        return optionRepository.findOptionsWithResultsByQuestionId(questionId, showVoteCount);
    }

    public void createOptions(List<OptionCreateRequest> options, QuestionEntity questionEntity) {
        optionRepository.saveAll(buildOptionEntities(options, questionEntity));
    }

    private List<OptionEntity> buildOptionEntities(List<OptionCreateRequest> options, QuestionEntity questionEntity) {
        return options.stream()
                .map(option -> OptionEntity.builder()
                        .questionEntity(questionEntity)
                        .name(option.getName())
                        .build())
                .toList();
    }
}
