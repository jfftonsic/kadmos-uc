package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.Idempotency;
import com.example.controller.dataobject.UpdateReservation;
import com.example.exception.presentation.HttpFacingBaseException;
import com.example.exception.service.NotEnoughBalanceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
class BalanceControllerTest {
    public static final String IDEM_CODE = "idem-code";
    public static final String PRINCIPAL_NAME = "principal-name";
    public static final String UPDATE_RESERVATION_CODE = "update-reservation-code";
    @Mock
    IBalanceService balanceService;

    @Mock
    UUIDGenerator uuidGenerator;

    Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

    BalanceController balanceController ;

    @BeforeEach
    void beforeEach() {
        balanceController = new BalanceController(balanceService, uuidGenerator, clock);
    }

    @Test
    void getBalance() {
        when(balanceService.fetchAmount()).thenReturn(BigDecimal.ONE);

        final var expectedBalance = new BalanceController.BalanceResponse(BigDecimal.ONE, ZonedDateTime.now(clock));

        final var actualBalance = balanceController.getBalance();

        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void addFunds_success() {
        final var input = new BalanceController.FundsRequest(BigDecimal.TEN);
        balanceController.addFunds(input);
        Mockito.verify(balanceService).addFunds(argThat(arg -> arg.compareTo(BigDecimal.TEN) == 0));
    }

    @Test
    void addFunds_negativeAmount() {
        final var input = new BalanceController.FundsRequest(BigDecimal.valueOf(-10.0));
        assertThrows(HttpFacingBaseException.class, () -> balanceController.addFunds(input));
    }

    @Test
    void postUpdateReservation_success() throws NotEnoughBalanceException {
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        final var zonedDateTime = ZonedDateTime.now(clock);
        when(balanceService.createUpdateReservation(
                eq(IDEM_CODE),
                eq(PRINCIPAL_NAME),
                eq(zonedDateTime),
                argThat(arg -> arg.compareTo(BigDecimal.TEN) == 0)
        )).thenReturn(UPDATE_RESERVATION_CODE);

        var input = new BalanceController.UpdateReservationPostRequest(
                zonedDateTime,
                new Idempotency(IDEM_CODE),
                BigDecimal.TEN
        );

        var expected = new BalanceController.UpdateReservationPostResponse(
                zonedDateTime,
                new UpdateReservation(UPDATE_RESERVATION_CODE)
        );

        final var actual = balanceController.postUpdateReservation(input, principal);

        assertEquals(expected, actual);
    }

    @Test
    void postUpdateReservation_notEnoughBalance() throws NotEnoughBalanceException {

    }


}