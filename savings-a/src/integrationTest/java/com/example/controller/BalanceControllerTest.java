package com.example.controller;

import com.example.business.api.IBalanceService;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilder;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithAdminRole;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithUserRole;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithoutRoles;
import static com.example.util.MockMvcUtil.performAndExpect;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilder;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidAdminUserWithoutRole;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidUserWithAdminRole;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidUserWithUserRole;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
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
    void getBalance_unauthenticated() throws Exception {
        performAndExpect(mockMvc, getBalanceRequestBuilder(), status().isUnauthorized());
    }

    @Test
    void postBalanceAddFunds_unauthenticated() throws Exception {
        performAndExpect(mockMvc, postBalanceAddFundsRequestBuilder(), status().isUnauthorized());
    }

    @Test
    void getBalance_withoutRole() throws Exception {
        performAndExpect(mockMvc, getBalanceRequestBuilderWithValidUserWithoutRoles(), status().isForbidden());
    }

    @Test
    void postBalanceAddFunds_withoutRole() throws Exception {
        performAndExpect(mockMvc,
                postBalanceAddFundsRequestBuilderWithValidAdminUserWithoutRole(),
                status().isForbidden());
    }

    @Test
    void postBalanceAddFunds_withInsufficientRole() throws Exception {
        performAndExpect(mockMvc,
                postBalanceAddFundsRequestBuilderWithValidUserWithUserRole(),
                status().isForbidden());
    }

    @Test
    void postBalanceAddFunds_success() throws Exception {
        performAndExpect(mockMvc,
                postBalanceAddFundsRequestBuilderWithValidUserWithAdminRole()
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"amount\": 10.0 }"),
                status().isOk());

        Mockito.verify(service).addFunds(Mockito.argThat(argument -> argument.compareTo(BigDecimal.TEN) == 0));
    }

    void getBalance_success(RequestBuilder requestBuilder) throws Exception {
        Mockito.when(service.fetchAmount()).thenReturn(BigDecimal.ZERO);

        performAndExpect(mockMvc, requestBuilder, status().isOk(), jsonPath("$.amount").value("0"));
    }

    @Test
    void getBalance_successUserRole() throws Exception {
        getBalance_success(getBalanceRequestBuilderWithValidUserWithUserRole());
    }

    @Test
    void getBalance_successAdminRole() throws Exception {
        getBalance_success(getBalanceRequestBuilderWithValidUserWithAdminRole());
    }

}