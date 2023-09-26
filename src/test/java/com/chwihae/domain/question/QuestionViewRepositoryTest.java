package com.chwihae.domain.question;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.infra.fixture.QuestionEntityFixture;
import com.chwihae.infra.fixture.QuestionViewFixture;
import com.chwihae.infra.fixture.UserEntityFixture;
import com.chwihae.infra.test.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
class QuestionViewRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("질문 아이디 리스트로 질문 조회 엔티티 리스트를 반환받는다")
    void findViewCountsByQuestionEntityIds_returnList() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        QuestionEntity question1 = QuestionEntityFixture.of(user);
        QuestionEntity question2 = QuestionEntityFixture.of(user);
        questionRepository.saveAll(List.of(question1, question2));
        QuestionViewEntity view1 = QuestionViewFixture.of(question1);
        QuestionViewEntity view2 = QuestionViewFixture.of(question2);
        questionViewRepository.saveAll(List.of(view1, view2));

        //when
        List<QuestionViewEntity> questionViewEntityList = questionViewRepository.findByQuestionEntityIds(List.of(question1.getId(), question2.getId()));

        //then
        Assertions.assertThat(questionViewEntityList)
                .hasSize(2)
                .extracting("id")
                .containsOnly(view1.getId(), view2.getId());
    }

    @Test
    @DisplayName("질문 아이디에 해당되는 질문 조회 엔티티가 없으면 빈배열을 반환한다")
    void findViewCountsByQuestionEntityIds_returnEmpty() throws Exception {
        //when
        List<QuestionViewEntity> questionViewEntityList = questionViewRepository.findByQuestionEntityIds(List.of());

        //then
        Assertions.assertThat(questionViewEntityList).isEmpty();
    }

}