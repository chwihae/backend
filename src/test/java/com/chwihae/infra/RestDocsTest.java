package com.chwihae.infra;

import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(RestDocumentationExtension.class)
@IntegrationTestSupport
public abstract class RestDocsTest {

    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProperties jwtTokenProperties;

    @Autowired
    private JwtTokenHandler jwtTokenHandler;

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .apply(documentationConfiguration(provider))
                .alwaysDo(print())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    protected abstract Object initController();

    protected String token(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtTokenProperties.getSecretKey(), jwtTokenProperties.getTokenExpiredTimeMs());
    }

    protected String body(Object body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }
}
