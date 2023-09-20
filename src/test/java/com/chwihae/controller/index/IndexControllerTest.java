package com.chwihae.controller.index;

import com.chwihae.infra.MockMvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IndexControllerTest extends MockMvcTestSupport {

    @Test
    @DisplayName("GET / - 성공 (토큰 없이)")
    void index() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/")
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /docs/index.html - 성공 (토큰 없이)")
    void docs() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/docs/index.html")
                )
                .andExpect(status().isOk());
    }
}