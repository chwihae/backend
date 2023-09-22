package com.chwihae.infra.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class AbstractMockMvcTest extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected String body(Object body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }
}
