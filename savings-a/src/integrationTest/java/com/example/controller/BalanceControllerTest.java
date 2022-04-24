package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.api.IBalanceService;
import com.example.util.FakeClockConfiguration;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilder;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithAdminRole;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithUserRole;
import static com.example.util.MockMvcUtil.getBalanceRequestBuilderWithValidUserWithoutRoles;
import static com.example.util.MockMvcUtil.getValidUserWithUserRole;
import static com.example.util.MockMvcUtil.performAndExpect;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilder;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidAdminUserWithoutRole;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidUserWithAdminRole;
import static com.example.util.MockMvcUtil.postBalanceAddFundsRequestBuilderWithValidUserWithUserRole;
import static com.example.util.MockMvcUtil.postUpdateReservationRequestBuilder;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// use this when you want a more complete spring context
//@SpringBootTest
//@AutoConfigureMockMvc

@WebMvcTest(controllers = BalanceController.class)
@Import(FakeClockConfiguration.class)
class BalanceControllerTest {

    public static final String FAKE_TIMESTAMP = "1970-01-01T00:00:00Z";
    public static final String FAKE_IDEM_CODE = "client-generated-code";
    public static final double TEN_DOUBLE = 10.0;
    public static final String FAKE_UUID_STR = "278e0f99-78a1-470c-b99e-17b7760e61f7";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    IBalanceService service;
    @MockBean
    UUIDGenerator uuidGenerator;

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

        verify(service).addFunds(argThat(argument -> argument.compareTo(BigDecimal.TEN) == 0));
    }

    void getBalance_success(RequestBuilder requestBuilder) throws Exception {
        when(service.fetchAmount()).thenReturn(BigDecimal.ZERO);

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

    @Test
    void postUpdateReservation_success() throws Exception {
        final var amount = TEN_DOUBLE;
        final var reservationCode = UUID.fromString("ec2aaf17-7106-445c-ae2c-93f72f24a2b4");
        when(
                service.reserve(
                        eq(reservationCode)
                )
        ).thenReturn(reservationCode);
        byte[] json = """
                {
                    "timestamp": "%s",
                    "idempotency": {
                        "code": "%s"
                    },
                    "amount": %.1f
                }
                """.formatted(FAKE_TIMESTAMP, FAKE_IDEM_CODE, amount).getBytes(StandardCharsets.UTF_8);

        performAndExpect(
                mockMvc,
                postUpdateReservationRequestBuilder()
                        .with(getValidUserWithUserRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                status().isOk(),
                jsonPath("$.timestamp").value(FAKE_TIMESTAMP),
                jsonPath("$.updateReservation.code").value(reservationCode));

    }

    @Test
    void postUpdateReservation_success_onlyRequiredFields() throws Exception {
        final var amount = TEN_DOUBLE;
        final var reservationCode = UUID.fromString("ec2aaf17-7106-445c-ae2c-93f72f24a2b4");
        UUID uuid = UUID.fromString(FAKE_UUID_STR);
        when(uuidGenerator.randomUUID()).thenReturn(uuid);
        when(
                service.reserve(
                        eq(reservationCode)
                )
        ).thenReturn(reservationCode);

        byte[] json = """
                {
                    "amount": %.1f
                }
                """.formatted(amount).getBytes(StandardCharsets.UTF_8);

        performAndExpect(
                mockMvc,
                postUpdateReservationRequestBuilder()
                        .with(getValidUserWithUserRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                status().isOk(),
                jsonPath("$.timestamp").value(FAKE_TIMESTAMP),
                jsonPath("$.updateReservation.code").value(reservationCode));

    }

    @Test
    void postUpdateReservation_idemCodeMaxSizeViolated() throws Exception {
        byte[] json = """
                {
                    "timestamp": "%s",
                    "idempotency": {
                        "code": "%s"
                    },
                    "amount": %.1f
                }
                """.formatted(FAKE_TIMESTAMP, "123456789012345678901234567890123", TEN_DOUBLE)
                .getBytes(StandardCharsets.UTF_8);

        testForInvalidRequest(json);
    }

    @Test
    void postUpdateReservation_noAmount() throws Exception {
        byte[] json = """
                {
                    "timestamp": "%s",
                    "idempotency": {
                        "code": "%s"
                    }
                }
                """.formatted(FAKE_TIMESTAMP, FAKE_IDEM_CODE)
                .getBytes(StandardCharsets.UTF_8);

        testForInvalidRequest(json);
    }

    private void testForInvalidRequest(byte[] json) throws Exception {
        performAndExpect(
                mockMvc,
                postUpdateReservationRequestBuilder()
                        .with(getValidUserWithUserRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                status().is4xxClientError());
    }

}