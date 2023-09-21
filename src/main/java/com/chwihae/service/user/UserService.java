package com.chwihae.service.user;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.dto.user.UserContext;
import com.chwihae.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chwihae.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity createUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }
    
    public UserContext getUserContextOrException(Long userId) {
        return userRepository.findById(userId)
                .map(UserContext::fromEntity)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }
}
