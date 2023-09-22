package com.chwihae.service.bookmark;

import com.chwihae.domain.bookmark.BookmarkEntity;
import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.QUESTION_NOT_FOUND;
import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BookmarkService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean bookmark(Long questionId, Long userId) {
        QuestionEntity questionEntity = findQuestionOrException(questionId);
        ensureUserIsNotQuestioner(userId, questionEntity);
        UserEntity userEntity = findUserWithLockOrException(userId);
        return bookmarkRepository.findByQuestionEntityIdAndUserEntityId(questionEntity.getId(), userEntity.getId())
                .map(this::deleteBookmark)
                .orElseGet(() -> saveBookmark(userEntity, questionEntity));
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

    private UserEntity findUserWithLockOrException(Long userId) {
        return userRepository.findWithLockById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private QuestionEntity findQuestionOrException(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new CustomException(QUESTION_NOT_FOUND));
    }

    private BookmarkEntity buildBookmark(UserEntity userEntity, QuestionEntity questionEntity) {
        return BookmarkEntity.builder()
                .userEntity(userEntity)
                .questionEntity(questionEntity)
                .build();
    }
}
