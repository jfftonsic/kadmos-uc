package com.example.business.api;

import com.example.exception.service.NotEnoughBalanceException;

import java.math.BigDecimal;
import java.util.UUID;

public interface IBalanceService {

    BigDecimal fetchAmount();

    void addFunds(BigDecimal amount);

    /**
     * @param reservationCode Idempotency code
     * @return the reservation code, which uniquely identifies the reservation and is part of communication between
     * services.
     */
    UUID reserve(UUID reservationCode)
            throws NotEnoughBalanceException;

    enum ConfirmUpdateReservationResponse { NO_CHANGES, DONE, WAS_INVALID, WAS_ALREADY_CANCELED, RECEIVED_BUT_NOT_RESERVED, UPDATE_RESERVATION_NOT_FOUND, CONFIRMATION_NOT_FOUND }
    enum UndoUpdateReservationResponse { NO_CHANGES, DONE, WAS_ALREADY_CONFIRMED, RECEIVED_BUT_NOT_RESERVED, UPDATE_RESERVATION_NOT_FOUND, UNDO_NOT_FOUND }
}
