package com.chwihae.service.user;

import com.chwihae.config.redis.UserContextCacheRepository;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserLevel;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.user.UserContext;
import com.chwihae.dto.user.UserStatisticsResponse;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserContextCacheRepository userContextCacheRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public UserEntity getOrCreateUser(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> saveUser(email));
    }

    public UserContext getUserContextOrException(Long userId) {
        Optional<UserContext> userContextOpt = userContextCacheRepository.getUserContext(userId);
        return userContextOpt
                .orElseGet(() -> userRepository.findById(userId)
                        .map(userEntity -> userContextCacheRepository.setUserContext(UserContext.fromEntity(userEntity)))
                        .orElseThrow(() -> {
                            log.warn("Error in method [getUserContextOrException] - User not found with ID: {}", userId);
                            return new CustomException(USER_NOT_FOUND);
                        }));
    }

    public UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public UserEntity findUserWithLockOrException(Long userId) {
        return userRepository.findWithLockById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public UserStatisticsResponse getUserStatistics(Long userId) {
        int commentCount = commentRepository.countByUserEntityId(userId);
        int voteCount = voteRepository.countByUserEntityId(userId);
        UserLevel userLevel = UserLevel.getLevel(voteCount, commentCount);
        return UserStatisticsResponse.of(userLevel, commentCount, voteCount);
    }

    public void setUserContext(UserContext userContext) {
        userContextCacheRepository.setUserContext(userContext);
    }

    private UserEntity saveUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }
}
