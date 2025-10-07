package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;   // <-- options(), get(), post()...
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // <-- status(), header()...
import static org.hamcrest.Matchers.*;                                            // <-- containsString()

@AutoConfigureMockMvc(addFilters = true) // bật Security filter chain
@SpringBootTest
@ActiveProfiles("dev") // để load application-dev.properties (nếu bạn dùng profile)
class CorsTest {

    @Autowired MockMvc mvc;

    @Test
    void preflight_options_should_return_cors_headers() throws Exception {
        mvc.perform(options("/api/request")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")));
    }

    @Test
    void simple_get_should_echo_allow_origin() throws Exception {
        mvc.perform(get("/api/cors")
                .header("Origin", "http://localhost:3000"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
