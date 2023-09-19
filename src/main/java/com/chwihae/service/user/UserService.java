package com.chwihae.service.user;

import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
