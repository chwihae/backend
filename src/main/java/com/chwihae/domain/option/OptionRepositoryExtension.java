package com.chwihae.domain.option;

import com.chwihae.dto.option.response.Option;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface OptionRepositoryExtension {
    List<Option> findOptionsWithResultsByQuestionId(Long questionId, boolean canViewResults);
}
