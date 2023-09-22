package com.chwihae.service.commenter;

import com.chwihae.domain.commenter.CommenterSequenceEntity;
import com.chwihae.domain.commenter.CommenterSequenceRepository;
import com.chwihae.domain.question.QuestionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommenterSequenceService {

    private final CommenterSequenceRepository commenterSequenceRepository;

    @Transactional
    public void createCommenterSequence(QuestionEntity questionEntity) {
        commenterSequenceRepository.save(CommenterSequenceEntity.builder()
                .questionEntity(questionEntity)
                .build());
    }
}
