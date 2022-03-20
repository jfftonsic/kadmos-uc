package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.api.IBalanceService;
import static com.example.util.MockMvcUtil.getValidUserWithUserRole;
import static com.example.util.MockMvcUtil.performAndExpect;
import static com.example.util.MockMvcUtil.postUpdateReservationRequestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

/**
 * This is an example of a junit with the following characteristics:
 *  - brings all the spring context up with integrationTest profile, which initiates a docker container for the database.
 *  - has a MockMvc instance to make calls
 *  - opens no port, only MockMvc can access the controllers
 *  - supports mocking beans with @MockBean
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@AutoConfigureMockMvc
public class FullSpringBootContextTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    IBalanceService service;

    @MockBean
    UUIDGenerator uuidGenerator;

    public static final String FAKE_TIMESTAMP = "1970-01-01T00:00:00Z";
    public static final String FAKE_IDEM_CODE = "client-generated-code";
    public static final double TEN_DOUBLE = 10.0;

    @Test
    public void a() throws Exception {
        byte[] json = """
                {
                    "timestamp": "%s",
                    "idempotency": {
                        "code": "%s"
                    },
                    "amount": %.1f
                }
                """.formatted(FAKE_TIMESTAMP, "123456789012345678901234567890123", TEN_DOUBLE).getBytes(StandardCharsets.UTF_8);

        performAndExpect(
                mockMvc,
                postUpdateReservationRequestBuilder()
                        .with(getValidUserWithUserRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                status().is4xxClientError());
    }
}
