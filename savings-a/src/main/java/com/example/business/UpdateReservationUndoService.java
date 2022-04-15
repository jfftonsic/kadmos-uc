package com.example.business;

import com.example.business.api.IBalanceService.UndoUpdateReservationResponse;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.DONE;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.NO_CHANGES;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.RECEIVED_BUT_NOT_RESERVED;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.UNDO_NOT_FOUND;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.WAS_ALREADY_CONFIRMED;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CANCELED;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.db.relational.repository.BalanceUpdateUndoRepository;
import com.example.util.GeneralConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateReservationUndoService {

    final ReservationStateMachineService stateMachineService;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository updateReservationRepository;
    final BalanceUpdateUndoRepository undoRepository;

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_S
    )
    public UndoUpdateReservationResponse undoUpdateReservation(
            UUID reservationCode,
            UUID undoId
    ) {
        // START - Reservation not locked yet
        // advantages: you make validations before entering a locked state, reducing database contention when you receive
        // lots of calls that would be invalidated.
        // disadvantages: you likely have to redo validations after you enter locked state, generating extra calls
        var undoOpt = undoRepository.findById(undoId);
        if (undoOpt.isEmpty()) {
            log.info("a=UndoNotFound undoId={}", undoId);
            return UNDO_NOT_FOUND;
        }
        var undo = undoOpt.get();
        if (undo.getDone()) {
            log.info("a=UndoWasAlreadyMarkedAsDone undoId={}", undoId);
            return NO_CHANGES;
        }

        var reservationOpt = updateReservationRepository.findByReservationCode(reservationCode);
        if (reservationOpt.isEmpty()) {
            log.info("a=ReservationNotFound reservationCode={}", reservationCode);
            return UPDATE_RESERVATION_NOT_FOUND;
        }
        var reservation = reservationOpt.get();

        UndoUpdateReservationResponse validationIssueOrNull = validateStatusChange(reservation);
        if (validationIssueOrNull != null)
            return validationIssueOrNull;
        // END - Reservation not locked yet

        // START - Reservation locked context
        reservationOpt = updateReservationRepository.findAndLockByReservationCode(
                reservationCode);
        if (reservationOpt.isEmpty()) {
            throw new IllegalStateException("A previously found reservation (%s) disappeared from the database!".formatted(reservationCode));
        }
        reservation = reservationOpt.get();
        validationIssueOrNull = validateStatusChange(reservation);
        if (validationIssueOrNull != null) {
            log.info("a=UndoStateChangeBecameInvalidConcurrently reservation={}", reservationCode);
            return validationIssueOrNull;
        }

        undoOpt = undoRepository.findById(undoId);
        if (undoOpt.isEmpty()) {
            throw new IllegalStateException("A previously found undo (%s) disappeared from the database!".formatted(undoId));
        }
        undo = undoOpt.get();
        if (undo.getDone()) {
            log.info("a=UndoWasConcurrentlyMarkedAsDone undoId={}", undoId);
            return NO_CHANGES;
        }

        undo.setDone(true);
        reservation.setStatusEnum(CANCELED);

        if (reservation.getAmount().signum() < 0) {
            // START - Balance locked context
            final var balance = balanceRepository.getBalanceForUpdateNative();
            balance.setOnHoldAmount(balance.getOnHoldAmount().add(reservation.getAmount()));
        }

        return DONE;
        // END - Balance locked context
        // END - Reservation locked context

    }

    private UndoUpdateReservationResponse validateStatusChange(BalanceUpdateReservationEntity reservation) {
        if (!stateMachineService.isStatusChangeValid(reservation.getStatusEnum(),
                BalanceUpdateReservationStatus.CANCELED)) {
            return switch (reservation.getStatusEnum()) {
                // it could fall here in case of concurrency
                case RECEIVED -> RECEIVED_BUT_NOT_RESERVED;
                // Considering BUSINESS_RULE_VIOLATION as NO_CHANGES so that caller sees that the reservation is already
                // ineffective.
                case CANCELED, BUSINESS_RULE_VIOLATION -> NO_CHANGES;
                // caller may be interested in the fact that the reservation ended up being confirmed before his undo request.
                case CONFIRMED -> WAS_ALREADY_CONFIRMED;
                default -> throw new IllegalStateException(
                        "There is no mapping for current state %s when trying to undo".formatted(reservation.getStatusEnum()));
            };
        }
        return null;
    }
}
