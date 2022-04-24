package com.example.business;

import com.example.business.api.IBalanceService;
import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import static com.example.util.BoilerplateUtil.getExactlyOne;
import com.example.util.GeneralConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BalanceService implements IBalanceService {

    final ApplicationContext context;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository balanceUpdateReservationRepository;
    final UpdateReservationInitService updateReservationInitService;

    @Override
    public BigDecimal fetchAmount() {
        return getExactlyOne(balanceRepository.getBalanceAmount(), "balance");
    }

    @Override
    public void addFunds(BigDecimal amount) {
        final var updates = balanceRepository.addFunds(amount);
        if (updates < 1) {
            throw new IllegalStateException("No row was updated while adding funds.");
        } else if (updates > 1) {
            throw new IllegalStateException("More than 1 row were updated while adding funds.");
        }
    }

    @Override
    @Transactional(
            timeout = GeneralConstants.TIMEOUT_S,
            isolation = Isolation.READ_COMMITTED,
            noRollbackFor = NotEnoughBalanceException.class
    )
    public UUID reserve(UUID reservationCode) throws NotEnoughBalanceException {

        log.info("balanceUpdateReservationRepository.findAndLockByReservationCode reservationCode={}...",
                reservationCode);
        final var balanceUpdateReservationEntityOpt = balanceUpdateReservationRepository
                .findAndLockByReservationCode(reservationCode);
        final var balanceUpdateReservationEntity = balanceUpdateReservationEntityOpt.orElseThrow(
                () -> new IllegalArgumentException(
                        "Balance update reservation not found. reservationCode=%s".formatted(reservationCode)
                )
        );

        final var amount = balanceUpdateReservationEntity.getAmount();
        if (amount.signum() < 0) {
            log.info("balanceRepository.getBalanceForUpdate idemCode={}", reservationCode);
            final var balanceAmountForUpdate = balanceRepository.getBalanceForUpdateNative();

            if (validateIfEnoughBalance(amount, balanceAmountForUpdate)) {

                final var onHoldAmount = balanceAmountForUpdate.getOnHoldAmount();
                final var newOnHoldAmount = onHoldAmount.add(amount.abs());

                log.info("idemCode={} replacing on-hold from {} to {} and saving balanceAmountForUpdate...",
                        onHoldAmount, newOnHoldAmount, reservationCode);

                balanceAmountForUpdate.setOnHoldAmount(newOnHoldAmount);
                balanceRepository.save(balanceAmountForUpdate);

            } else {
                log.info("idemCode={} BUSINESS_RULE_VIOLATION on balanceUpdateReservationEntity...", reservationCode);

                balanceUpdateReservationEntity.setStatus(
                        BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION.getDbValue()
                );
                throw new NotEnoughBalanceException();
            }

        } // when it is a credit, we just need to update the reservation to reserved.

        log.info("idemCode={} RESERVED on balanceUpdateReservationEntity...", reservationCode);
        balanceUpdateReservationEntity.setStatus(
                BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED.getDbValue()
        );

        return reservationCode;
    }

    private boolean validateIfEnoughBalance(BigDecimal amount, BalanceEntity balanceAmountForUpdate) {
        return balanceAmountForUpdate.getTotalAmount()
                .subtract(balanceAmountForUpdate.getOnHoldAmount())
                .add(amount)
                .signum() >= 0;
    }

    static class CollidingIdempotencyException extends Exception {
        public CollidingIdempotencyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
