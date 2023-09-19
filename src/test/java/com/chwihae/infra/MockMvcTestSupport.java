package com.chwihae.infra;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.domain.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@IntegrationTestSupport
public abstract class MockMvcTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenHandler jwtTokenHandler;

    @Autowired
    private JwtTokenProperties jwtTokenProperties;

    @Autowired
    protected UserRepository userRepository;

    protected String token(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs());
    }

    protected String body(Object body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    protected UserEntity createUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }
}
