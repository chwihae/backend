package com.chwihae.service.question.filter;

import com.chwihae.dto.user.UserQuestionFilterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static com.chwihae.dto.user.UserQuestionFilterType.*;

@Component
public class UserQuestionsFilterStrategyProvider {

    private final Map<UserQuestionFilterType, UserQuestionsFilterStrategy> questionFilterMap = new EnumMap<>(UserQuestionFilterType.class);

    @Autowired
    public UserQuestionsFilterStrategyProvider(MyQuestionsFilter myQuestionsFilter,
                                               BookmarkedQuestionsFilter bookmarkedQuestionsFilter,
                                               VotedQuestionsFilter votedQuestionsFilter) {
        questionFilterMap.put(ME, myQuestionsFilter);
        questionFilterMap.put(BOOKMARKED, bookmarkedQuestionsFilter);
        questionFilterMap.put(VOTED, votedQuestionsFilter);
    }

    public UserQuestionsFilterStrategy getFilter(UserQuestionFilterType type) {
        return Optional.ofNullable(questionFilterMap.get(type))
                .orElseThrow(() -> new IllegalStateException("Invalid user question filter type"));
    }
}
