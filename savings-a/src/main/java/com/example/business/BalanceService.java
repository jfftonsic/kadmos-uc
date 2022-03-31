package com.example.business;

import com.example.business.api.IBalanceService;
import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.entity.BalanceUpdateConfirmEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateConfirmRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import com.example.util.GeneralConstants;
import static com.example.util.GeneralConstants.TIMEOUT_MS;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BalanceService implements IBalanceService {

    final ApplicationContext context;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository balanceUpdateReservationRepository;
    final BalanceUpdateConfirmRepository balanceUpdateConfirmRepository;

    //
    @PersistenceContext
    final EntityManager entityManager;

    BalanceService self;

    public <T> T validBalance(List<T> amounts) {
        if (amounts.isEmpty()) {
            throw new IllegalStateException("No balance on database.");
        } else if (amounts.size() > 1) {
            throw new IllegalStateException("Should exist only one balance.");
        } else {
            return amounts.get(0);
        }
    }

    @Override
    public BigDecimal fetchAmount() {
        return validBalance(balanceRepository.getBalanceAmount());
    }

    @Override
    public void addFunds(BigDecimal amount) {
        if (balanceRepository.addFunds(amount) != 1) {
            throw new IllegalStateException("No row was updated while adding funds.");
        }
    }

    @SneakyThrows
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            timeout = TIMEOUT_MS,
            isolation = Isolation.READ_COMMITTED
    )
    public UUID initUpdateReservation(String idemCode, String idemActor, ZonedDateTime requestTimestamp,
            BigDecimal amount) {

        final var reservationCode = UUID.randomUUID();

        BalanceUpdateReservationEntity balanceUpdateReservationEntity = null;

        balanceUpdateReservationEntity = BalanceUpdateReservationEntity.builder()
                .amount(amount)
                .reservationCode(reservationCode)
                .idempotencyCode(idemCode)
                .idempotencyActor(idemActor)
                .requestTimestamp(requestTimestamp)
                .status(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED.getDbValue())
                .build();

        log.info("idemCode={} - Saving BalanceUpdateReservationEntity ...", idemCode);
        balanceUpdateReservationEntity = balanceUpdateReservationRepository.save(balanceUpdateReservationEntity);

        return reservationCode;
    }

    @SneakyThrows
    @Override
    @Transactional(
            timeout = GeneralConstants.TIMEOUT_MS,
            isolation = Isolation.READ_COMMITTED,
            noRollbackFor = NotEnoughBalanceException.class
    )
    public String createUpdateReservation(String idemCode, String idemActor, ZonedDateTime requestTimestamp,
            BigDecimal amount) throws NotEnoughBalanceException {

        final UUID reservationCode;
        try {
            reservationCode = getSelf().initUpdateReservation(idemCode, idemActor, requestTimestamp, amount);
        } catch (DataIntegrityViolationException e) {
            throw new CollidingIdempotencyException(
                    "There already is a balance update reservation with idempotency pair (%s, %s)"
                            .formatted(idemCode, idemActor), e);
        }

        log.info("Sleeping idemCode={}...", idemCode);
        Thread.sleep(5000);

        log.info("balanceUpdateReservationRepository.findAndPessimisticWriteLockByReservationCode idemCode={}...",
                idemCode);
        final var balanceUpdateReservationEntity = balanceUpdateReservationRepository.findAndPessimisticWriteLockByReservationCode(
                reservationCode).get();

        if (amount.signum() < 0) {
            log.info("balanceRepository.getBalanceForUpdate idemCode={}...", idemCode);
            final var balanceAmountForUpdate = validBalance(balanceRepository.getBalanceForUpdate(
                    Pageable.ofSize(1)));

            if (validateIfEnoughBalance(amount, balanceAmountForUpdate)) {

                final var newOnHoldAmount = balanceAmountForUpdate.getOnHoldAmount().add(amount);

                log.info("idemCode={} replacing on-hold from {} to {} and saving balanceAmountForUpdate...",
                        balanceAmountForUpdate.getOnHoldAmount(), newOnHoldAmount, idemCode);

                balanceAmountForUpdate.setOnHoldAmount(newOnHoldAmount);
                balanceRepository.save(balanceAmountForUpdate);

            } else {
                log.info("idemCode={} BUSINESS_RULE_VIOLATION on balanceUpdateReservationEntity...", idemCode);

                balanceUpdateReservationEntity.setStatus(
                        BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION.getDbValue()
                );
                throw new NotEnoughBalanceException();
            }

        } // when it is a credit, we just need to update the reservation to reserved.

        log.info("idemCode={} RESERVED on balanceUpdateReservationEntity...", idemCode);
        balanceUpdateReservationEntity.setStatus(
                BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED.getDbValue()
        );

        return reservationCode.toString();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_MS
    )
    public void initConfirmationOnDb(BalanceUpdateReservationEntity updateReservation, ZonedDateTime requestTimestamp) {
        final BalanceUpdateConfirmEntity confirmation = BalanceUpdateConfirmEntity.builder()
                .balanceUpdateReservationEntity(updateReservation)
                .requestTimestamp(requestTimestamp)
                .done(false)
                .build();
        balanceUpdateConfirmRepository.save(confirmation);
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_MS
    )
    @Override
    public ConfirmUpdateReservationResponse confirmUpdateReservation(UUID updateReservationCode,
            ZonedDateTime requestTimestamp) {

        final var updateReservationOpt = balanceUpdateReservationRepository.findAndPessimisticWriteLockByReservationCode(
                updateReservationCode);
        if (updateReservationOpt.isPresent()) {
            final var updateReservation = updateReservationOpt.get();

            try {
                getSelf().initConfirmationOnDb(updateReservation, requestTimestamp);
            } catch (DataIntegrityViolationException e) {
                // there already is a confirmation record on the database
                // if it is not completed yet, we may try to complete
                // if it is already completed, we can just return a "no change" response
                log.warn("Duplicated init of update reservation confirmation for updateReservationCode={}",
                        updateReservationCode);
            }

            final Optional<BalanceUpdateConfirmEntity> updateConfirmOpt = balanceUpdateConfirmRepository.findByBalanceUpdateReservationEntityId(
                    updateReservation.getId());
            if (updateConfirmOpt.isEmpty()) {
                throw new RuntimeException(
                        "The update reservation confirmation was not found after its supposed creation. updateReservationCode=%s".formatted(
                                updateReservationCode));
            }
            final var updateConfirm = updateConfirmOpt.get();

            if (updateConfirm.getDone())
                return ConfirmUpdateReservationResponse.NO_CHANGES;

            if (updateReservation.getAmount().signum() < 0) {
                final var balanceForUpdate = balanceRepository.getBalanceForUpdate(Pageable.ofSize(1));
                final var balanceEntity = balanceForUpdate.get(0);
                balanceEntity.setOnHoldAmount(balanceEntity.getOnHoldAmount().add(updateReservation.getAmount()));
                balanceEntity.setTotalAmount(balanceEntity.getTotalAmount().add(updateReservation.getAmount()));
            }
            updateReservation.setStatus(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED.getDbValue());
            updateConfirm.setDone(true);
        }
        return ConfirmUpdateReservationResponse.DONE;
    }

    private boolean validateIfEnoughBalance(BigDecimal amount, BalanceEntity balanceAmountForUpdate) {
        return balanceAmountForUpdate.getTotalAmount()
                .subtract(balanceAmountForUpdate.getOnHoldAmount())
                .subtract(amount)
                .signum() < 0;
    }

    public BalanceService getSelf() {
        if (self == null)
            self = context.getBean(BalanceService.class);
        return self;
    }

    static class CollidingIdempotencyException extends Exception {
        public CollidingIdempotencyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
