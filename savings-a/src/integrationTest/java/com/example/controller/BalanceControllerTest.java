package com.example.controller;

import com.example.business.api.IBalanceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

// use this when you want a more complete spring context
//@SpringBootTest
//@AutoConfigureMockMvc

@Configuration
class ExtraConf {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));
    }
}

@WebMvcTest(controllers = BalanceController.class)
@Import(ExtraConf.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    IBalanceService service;

    @Test
    void getBalance() throws Exception {
        Mockito.when(service.fetchAmount()).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(
                        get("/balance")
                                .with(csrf())
                                .with(
                                        SecurityMockMvcRequestPostProcessors
                                                .user("swagger")
                                                .authorities(new SimpleGrantedAuthority("USER"))
                                )
                )
                .andDo(print())
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value("0")
                );
    }
    //
    //    @Test
    //    void updateBalanceBy_successCase() throws Exception {
    //        this.mockMvc.perform(
    //                        post("/balance")
    //                                .contentType(MediaType.APPLICATION_JSON)
    //                                .content("{ \"amount\": 100.0 }".getBytes(StandardCharsets.UTF_8))
    //                )
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$.amount").value("100"));
    //    }
    //
    //    @Test
    //    void getBalance2() throws Exception {
    //        this.mockMvc.perform(get("/balance")).andDo(print()).andExpect(status().isOk())
    //                .andExpect(jsonPath("$.amount").value("0"));
    //    }
    //
    //    @Test
    //    void updateBalanceBy_negativeBalance() throws Exception {
    //        this.mockMvc.perform(
    //                        post("/balance")
    //                                .contentType(MediaType.APPLICATION_JSON)
    //                                .content("{ \"amount\": -100.0 }".getBytes(StandardCharsets.UTF_8))
    //                )
    //                .andDo(print())
    //                .andExpect(status().is4xxClientError())
    //                .andExpect(jsonPath("$.timestamp").isNotEmpty())
    //                .andExpect(jsonPath("$.message").isNotEmpty())
    //                .andExpect(jsonPath("$.path").value("/balance"));
    //    }
}