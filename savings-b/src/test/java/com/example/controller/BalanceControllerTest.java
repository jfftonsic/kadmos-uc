package com.example.controller;

import org.springframework.test.web.servlet.MockMvc;

//@SpringBootTest
//@AutoConfigureMockMvc
class BalanceControllerTest {

//    @Autowired
    private MockMvc mockMvc;


//
//    @BeforeEach
//    void setup() {
//        final var iBalanceService = Mockito.mock(IBalanceService.class);
//
//        this.mockMvc = MockMvcBuilders.standaloneSetup(new BalanceController(iBalanceService))
//                .setControllerAdvice(new GlobalControllerExceptionHandler())
//                .build();
//    }
//
//    @Test
//    void getBalance() throws Exception {
//        this.mockMvc.perform(get("/balance")).andDo(print()).andExpect(status().isOk())
//                .andExpect(jsonPath("$.amount").value("0"));
//    }
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