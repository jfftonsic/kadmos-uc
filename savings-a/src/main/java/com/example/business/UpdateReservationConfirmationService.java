package com.example.business;

import com.example.business.api.IBalanceService;
import com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.CONFIRMATION_NOT_FOUND;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.NO_CHANGES;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.RECEIVED_BUT_NOT_RESERVED;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.WAS_ALREADY_CANCELED;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.WAS_INVALID;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateConfirmRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
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
public class UpdateReservationConfirmationService {
    final ReservationStateMachineService stateMachineService;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository reservationRepository;
    final BalanceUpdateConfirmRepository confirmRepository;

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_S
    )
    public ConfirmUpdateReservationResponse confirmUpdateReservation(
            UUID reservationCode,
            UUID confirmationId
    ) {
        // Depending on the situation, you might want to make validations and extra things before entering any region
        // where you have locked a row for update. Because it minimizes contention.
        // In the case here, let's assume there are lots of times when this method is called and some confirmation
        // for that reservation code has already been done.

        var confirmOpt = confirmRepository.findById(confirmationId);
        if (confirmOpt.isEmpty()) {
            log.info("a=ConfirmationNotFound reservationCode={} confirmationId={}",
                    reservationCode,
                    confirmationId);
            return CONFIRMATION_NOT_FOUND;
        }
        var confirm = confirmOpt.get();
        if (confirm.getDone()) {
            log.info("a=ConfirmationWasAlreadyMarkedAsDone confirmationId={}", confirmationId);
            return NO_CHANGES;
        }

        var reservationOpt = reservationRepository.findByReservationCode(reservationCode);
        if (reservationOpt.isEmpty()) {
            log.info("a=ReservationNotFound reservationCode={}", reservationCode);
            return UPDATE_RESERVATION_NOT_FOUND;
        }
        var reservation = reservationOpt.get();

        var validationIssueOrNull = validateStatusChange(reservation);
        if (validationIssueOrNull != null)
            return validationIssueOrNull;

        reservationOpt = reservationRepository.findAndLockByReservationCode(
                reservationCode);
        if (reservationOpt.isEmpty()) {
            throw new IllegalStateException("A previously found reservation (%s) disappeared from the database!".formatted(
                    reservationCode));
        }
        reservation = reservationOpt.get();

        validationIssueOrNull = validateStatusChange(reservation);
        if (validationIssueOrNull != null) {
            log.info("a=ConfirmationStateChangeBecameInvalidConcurrently reservation={}", reservationCode);
            return validationIssueOrNull;
        }

        confirmOpt = confirmRepository.findById(confirmationId);
        if (confirmOpt.isEmpty()) {
            throw new IllegalStateException("A previously found confirmation (%s) disappeared from the database!".formatted(
                    confirmationId));
        }
        confirm = confirmOpt.get();
        if (confirm.getDone()) {
            log.info("a=ConfirmationWasConcurrentlyMarkedAsDone undoId={}", confirmationId);
            return NO_CHANGES;
        }

        confirm.setDone(true);
        reservation.setStatusEnum(CONFIRMED);

        final var balance = balanceRepository.getBalanceForUpdateNative();
        balance.setTotalAmount(balance.getTotalAmount().add(reservation.getAmount()));
        if (reservation.getAmount().signum() < 0) {
            balance.setOnHoldAmount(balance.getOnHoldAmount().add(reservation.getAmount()));
        }

        return ConfirmUpdateReservationResponse.DONE;
    }

    private IBalanceService.ConfirmUpdateReservationResponse validateStatusChange(
            BalanceUpdateReservationEntity reservation) {
        if (!stateMachineService.isStatusChangeValid(reservation.getStatusEnum(), CONFIRMED)) {
            return switch (reservation.getStatusEnum()) {
                // it could fall here in case of concurrency
                case RECEIVED -> RECEIVED_BUT_NOT_RESERVED;
                case CONFIRMED -> NO_CHANGES;
                case BUSINESS_RULE_VIOLATION -> WAS_INVALID;
                // caller may be interested in the fact that the reservation ended up being canceled before his confirmation request.
                case CANCELED -> WAS_ALREADY_CANCELED;
                default -> throw new IllegalStateException(
                        "There is no mapping for current state %s when trying to undo".formatted(reservation.getStatusEnum()));
            };
        }
        return null;
    }
}
