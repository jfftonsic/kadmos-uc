package com.example.business;

import com.example.business.api.IBalanceService;
import com.example.db.relational.TransactionHandler;
import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BalanceService implements IBalanceService {

    public static final int TIMEOUT_MS = 5000;
    BalanceRepository balanceRepository;
    BalanceUpdateReservationRepository balanceUpdateReservationRepository;

    @PersistenceContext
    EntityManager entityManager;

    TransactionHandler transactionHandler;

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
    @Transactional(timeout = TIMEOUT_MS)
    public BigDecimal updateBalanceBy(BigDecimal amount) throws NotEnoughBalanceException {
        final var balanceAmountForUpdate = validBalance(balanceRepository.getBalanceForUpdate(Pageable.ofSize(1)));
        if (amount.signum() == -1 && amount.abs().compareTo(balanceAmountForUpdate.getTotalAmount()) > 0) {
            throw new NotEnoughBalanceException();
        }

        balanceAmountForUpdate.setTotalAmount(balanceAmountForUpdate.getTotalAmount().add(amount));

        return balanceAmountForUpdate.getTotalAmount();
    }

    @Override
    public void addFunds(BigDecimal amount) {
        if (balanceRepository.addFunds(amount) != 1) {
            throw new IllegalStateException("No row was updated while adding funds.");
        }
    }

    @Override
    @Transactional(
            timeout = TIMEOUT_MS,
            isolation = Isolation.READ_COMMITTED,
            noRollbackFor = NotEnoughBalanceException.class
    )
    public String createUpdateReservation(String idemCode, String idemActor, ZonedDateTime requestTimestamp,
            BigDecimal amount) throws NotEnoughBalanceException {

        final var reservationCode = UUID.randomUUID();
        BalanceUpdateReservationEntity balanceUpdateReservationEntity = BalanceUpdateReservationEntity.builder()
                .amount(amount)
                .reservationCode(reservationCode)
                .idempotencyCode(idemCode)
                .idempotencyActor(idemActor)
                .requestTimestamp(requestTimestamp)
                .status(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED.getDbValue())
                .build();
        balanceUpdateReservationEntity = balanceUpdateReservationRepository.save(balanceUpdateReservationEntity);

        balanceUpdateReservationEntity = balanceUpdateReservationRepository.findAndPessimisticWriteLockById(balanceUpdateReservationEntity.getId()).get();

        if (amount.signum() < 0) {
            final var balanceAmountForUpdate = validBalance(balanceRepository.getBalanceForUpdate(
                    Pageable.ofSize(1)));

            if (validateIfEnoughBalance(amount, balanceAmountForUpdate)) {

                balanceAmountForUpdate.setOnHoldAmount(balanceAmountForUpdate.getOnHoldAmount()
                        .add(amount));
                balanceRepository.save(balanceAmountForUpdate);

            } else {
                balanceUpdateReservationEntity.setStatus(
                        BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION.getDbValue()
                );
                throw new NotEnoughBalanceException();
            }

        } // when it is a credit, we just need to update the reservation to reserved.

        balanceUpdateReservationEntity.setStatus(
                BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED.getDbValue()
        );

        return reservationCode.toString();
    }

    private boolean validateIfEnoughBalance(BigDecimal amount, BalanceEntity balanceAmountForUpdate) {
        return balanceAmountForUpdate.getTotalAmount()
                .subtract(balanceAmountForUpdate.getOnHoldAmount())
                .subtract(amount)
                .signum() < 0;
    }

}
