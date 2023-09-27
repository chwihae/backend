package com.chwihae.service.option;

import com.chwihae.domain.option.OptionEntity;
import com.chwihae.domain.option.OptionRepository;
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
}
