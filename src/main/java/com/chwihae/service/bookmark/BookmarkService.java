package com.chwihae.service.bookmark;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import com.chwihae.service.question.query.QuestionQueryService;
import com.chwihae.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BookmarkService {

    private final UserService userService;
    private final QuestionQueryService questionQueryService;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean bookmark(Long questionId, Long userId) {
        QuestionEntity questionEntity = questionQueryService.findQuestionOrException(questionId);
        ensureUserIsNotQuestioner(userId, questionEntity);
        UserEntity userEntity = userService.findUserWithLockOrException(userId);
        return bookmarkRepository.findByQuestionEntityIdAndUserEntityId(questionEntity.getId(), userEntity.getId())
                .map(this::deleteBookmark)
                .orElseGet(() -> saveBookmark(userEntity, questionEntity));
    }

    @Transactional
    public void deleteAllByQuestionId(Long questionId) {
        bookmarkRepository.deleteAllByQuestionId(questionId);
    }

    private boolean saveBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        bookmarkRepository.save(buildBookmark(userEntity, questionEntity));
        return true;
    }

    private void ensureUserIsNotQuestioner(Long userId, QuestionEntity questionEntity) {
        if (questionEntity.isCreatedBy(userId)) {
            throw new CustomException(CustomExceptionError.FORBIDDEN, "질문 작성자는 저장할 수 없습니다");
        }
    }

    private boolean deleteBookmark(BookmarkEntity it) {
        bookmarkRepository.delete(it);
        return false;
    }

    private BookmarkEntity buildBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        return BookmarkEntity.builder()
                .userEntity(userEntity)
                .questionEntity(questionEntity)
                .build();
    }
}
