package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.UpdateReservationInitService;
import com.example.business.api.IBalanceService;
import com.example.controller.ReservationPostController.UpdateReservationPostRequest;
import com.example.controller.ReservationPostController.UpdateReservationPostResponse;
import com.example.controller.dataobject.Idempotency;
import com.example.controller.dataobject.UpdateReservation;
import com.example.exception.service.NotEnoughBalanceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ReservationPostControllerTest {
    public static final String IDEM_CODE = "idem-code";

    public static final String PRINCIPAL_NAME = "principal-name";
    public static final String UPDATE_RESERVATION_CODE = "update-reservation-code";
    @Mock
    IBalanceService balanceService;

    @Mock
    UpdateReservationInitService updateReservationInitService;

    @Mock
    UUIDGenerator uuidGenerator;

    Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

    ReservationPostController controller;

    @BeforeEach
    void beforeEach() {
        controller = new ReservationPostController(balanceService, updateReservationInitService, uuidGenerator, clock);
    }

    @Test
    void postUpdateReservation_success() throws NotEnoughBalanceException {
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        final var reservationCode = UUID.fromString("ec2aaf17-7106-445c-ae2c-93f72f24a2b4");

        final var zonedDateTime = ZonedDateTime.now(clock);
        when(updateReservationInitService.initUpdateReservation(
                eq(IDEM_CODE),
                eq(PRINCIPAL_NAME),
                eq(zonedDateTime),
                eq(BigDecimal.TEN)
        )).thenReturn(reservationCode);
        when(balanceService.reserve(
                eq(reservationCode)
        )).thenReturn(reservationCode);

        var input = new UpdateReservationPostRequest(
                zonedDateTime,
                new Idempotency(IDEM_CODE),
                BigDecimal.TEN
        );

        var expected = new UpdateReservationPostResponse(
                zonedDateTime,
                new UpdateReservation(reservationCode.toString())
        );

        final var actual = controller.postUpdateReservation(input, principal);

        assertEquals(expected, actual);
    }

    @Test
    void postUpdateReservation_notEnoughBalance() throws NotEnoughBalanceException {

    }


}