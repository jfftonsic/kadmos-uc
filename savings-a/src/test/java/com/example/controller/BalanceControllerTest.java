package com.example.controller;

import com.example.exception.handler.GlobalControllerExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

//@SpringBootTest
//@AutoConfigureMockMvc
class BalanceControllerTest {

//    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new BalanceController())
                .setControllerAdvice(new GlobalControllerExceptionHandler())
                .build();
    }

    @Test
    void getBalance() throws Exception {
        this.mockMvc.perform(get("/balance")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("0"));
    }

    @Test
    void updateBalanceBy_successCase() throws Exception {
        this.mockMvc.perform(
                        post("/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ \"amount\": 100.0 }".getBytes(StandardCharsets.UTF_8))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("100"));
    }

    @Test
    void getBalance2() throws Exception {
        this.mockMvc.perform(get("/balance")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("0"));
    }

    @Test
    void updateBalanceBy_negativeBalance() throws Exception {
        this.mockMvc.perform(
                        post("/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ \"amount\": -100.0 }".getBytes(StandardCharsets.UTF_8))
                )
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/balance"));
    }
}