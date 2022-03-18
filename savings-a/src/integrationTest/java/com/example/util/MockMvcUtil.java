package com.example.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class MockMvcUtil {

    public static final String HTTP_SEGMENT_BALANCE = "/balance";
    public static final String HTTP_SEGMENT_BALANCE_ADMIN_FUNDS = "/balance/admin/funds";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    @NotNull
    public static ResultActions performAndExpect(MockMvc mockMvc, RequestBuilder requestBuilder,
            ResultMatcher... matchers) throws Exception {
        final var resultActions = mockMvc.perform(requestBuilder).andDo(print());
        resultActions.andExpectAll(matchers);
        return resultActions;
    }

    @NotNull
    public static MockHttpServletRequestBuilder postBalanceAddFundsRequestBuilder() {
        return post(HTTP_SEGMENT_BALANCE_ADMIN_FUNDS);
    }

    @NotNull
    public static MockHttpServletRequestBuilder getBalanceRequestBuilder() {
        return get(HTTP_SEGMENT_BALANCE);
    }

    @NotNull
    public static MockHttpServletRequestBuilder postBalanceAddFundsRequestBuilderWithValidAdminUserWithoutRole() {
        return postBalanceAddFundsRequestBuilder().with(getValidAdminUser());
    }

    @NotNull
    public static MockHttpServletRequestBuilder postBalanceAddFundsRequestBuilderWithValidUserWithUserRole() {
        return postBalanceAddFundsRequestBuilder().with(getValidUserWithUserRole());
    }

    @NotNull
    public static MockHttpServletRequestBuilder postBalanceAddFundsRequestBuilderWithValidUserWithAdminRole() {
        return postBalanceAddFundsRequestBuilder().with(getValidAdminUserWithAdminRole());
    }

    @NotNull
    public static MockHttpServletRequestBuilder getBalanceRequestBuilderWithValidUserWithUserRole() {
        return getBalanceRequestBuilder().with(getValidUserWithUserRole());
    }

    @NotNull
    public static MockHttpServletRequestBuilder getBalanceRequestBuilderWithValidUserWithAdminRole() {
        return getBalanceRequestBuilder().with(getValidAdminUserWithAdminRole());
    }

    @NotNull
    public static MockHttpServletRequestBuilder getBalanceRequestBuilderWithValidUserWithoutRoles() {
        return getBalanceRequestBuilder().with(getValidUser());
    }

    @NotNull
    public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor getValidUserWithUserRole() {
        return getValidUser()
                .authorities(getUserRole());
    }

    @NotNull
    public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor getValidAdminUserWithAdminRole() {
        return getValidAdminUser()
                .authorities(getAdminRole());
    }

    @NotNull
    public static SimpleGrantedAuthority getUserRole() {
        return new SimpleGrantedAuthority(ROLE_USER);
    }

    @NotNull
    public static SimpleGrantedAuthority getAdminRole() {
        return new SimpleGrantedAuthority(ROLE_ADMIN);
    }

    @NotNull
    public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor getValidUser() {
        return SecurityMockMvcRequestPostProcessors.user("swagger").password("swagger");
    }

    @NotNull
    public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor getValidAdminUser() {
        return SecurityMockMvcRequestPostProcessors.user("admin").password("admin");
    }
}
