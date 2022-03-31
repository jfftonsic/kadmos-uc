package com.example.business.api;

import com.example.exception.service.NotEnoughBalanceException;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface IBalanceService {

    BigDecimal fetchAmount();

    void addFunds(BigDecimal amount);

    /**
     *
     * @param idemCode Idempotency code
     * @param idemActor Idempotency actor
     * @param requestTimestamp timestamp that came in the API request
     * @param amount the amount to be updated, can be negative or positive
     * @return the reservation code, which uniquely identifies the reservation and is part of communication between
     *  services.
     */
    String createUpdateReservation(String idemCode, String idemActor, ZonedDateTime requestTimestamp, BigDecimal amount)
            throws NotEnoughBalanceException;

    enum ConfirmUpdateReservationResponse { NO_CHANGES, DONE, UPDATE_RESERVATION_NOT_FOUND }
    ConfirmUpdateReservationResponse confirmUpdateReservation(UUID updateReservationCode, ZonedDateTime requestTimestamp);
}
