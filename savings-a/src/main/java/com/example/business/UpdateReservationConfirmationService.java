package com.example.business;

import com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.CONFIRMATION_NOT_FOUND;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.NO_CHANGES;
import static com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateReservationConfirmationService {//implements SelfReferential<UpdateReservationConfirmationService> {

    final ApplicationContext context;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository balanceUpdateReservationRepository;
    final BalanceUpdateConfirmRepository balanceUpdateConfirmRepository;

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_S
    )
    public ConfirmUpdateReservationResponse confirmUpdateReservation(
            UUID updateReservationCode,
            UUID confirmationId
    ) {
        // Depending on the situation, you might want to make validations and extra things before entering any region
        // where you have locked a row for update. Because it minimizes contention.
        // In the case here, let's assume there are lots of times when this method is called and some confirmation
        // for that reservation code has already been done.
        final var anyDoneConfirmation = balanceUpdateConfirmRepository.existsDone(updateReservationCode);
        if (anyDoneConfirmation) {
            log.info("a=AlreadyConfirmed updateReservationCode={}", updateReservationCode);
            return NO_CHANGES;
        }

        final var reservationOpt = balanceUpdateReservationRepository.findAndLockByReservationCode(
                updateReservationCode);
        if (reservationOpt.isEmpty()) {
            log.info("a=ReservationNotFound updateReservationCode={}", updateReservationCode);
            return UPDATE_RESERVATION_NOT_FOUND;
        }
        final var reservation = reservationOpt.get();

        // now that we are in the locked region, we need to re-validate if no confirmations were performed concurrently
        // since we last checked.
        final var anyDoneConfirmationRevalidation = balanceUpdateConfirmRepository.existsDone(updateReservationCode);
        if (anyDoneConfirmationRevalidation) {
            log.info("a=SecondValidationAlreadyConfirmed updateReservationCode={}", updateReservationCode);
            return NO_CHANGES;
        }

        final var confirmOpt = balanceUpdateConfirmRepository.findById(confirmationId);
        if (confirmOpt.isEmpty()) {
            log.info("a=ConfirmationNotFound updateReservationCode={} confirmationId={}",
                    updateReservationCode,
                    confirmationId);
            return CONFIRMATION_NOT_FOUND;
        }
        final var confirm = confirmOpt.get();

        confirm.setDone(true);
        reservation.setStatusEnum(CONFIRMED);

        final var balance = balanceRepository.getBalanceForUpdateNative();
        balance.setTotalAmount(balance.getTotalAmount().add(reservation.getAmount()));
        if (reservation.getAmount().signum() < 0) {
            balance.setOnHoldAmount(balance.getOnHoldAmount().add(reservation.getAmount()));
        }

        return ConfirmUpdateReservationResponse.DONE;
    }
}
