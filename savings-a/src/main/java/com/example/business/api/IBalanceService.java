package com.example.business.api;

import com.example.exception.service.NotEnoughBalanceException;

import java.math.BigDecimal;

public interface IBalanceService {

    BigDecimal fetchAmount();

    void addFunds(BigDecimal amount);

    /**
     *
     * @param reservationCode Idempotency code
     * @return the reservation code, which uniquely identifies the reservation and is part of communication between
     *  services.
     */
    String reserve(String reservationCode)
            throws NotEnoughBalanceException;

    enum ConfirmUpdateReservationResponse { NO_CHANGES, DONE, UPDATE_RESERVATION_NOT_FOUND, CONFIRMATION_NOT_FOUND }

//    ConfirmUpdateReservationResponse confirmUpdateReservation(UUID updateReservationCode, ZonedDateTime requestTimestamp);
}
