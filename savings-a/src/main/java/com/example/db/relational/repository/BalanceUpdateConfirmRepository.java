package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceUpdateConfirmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface BalanceUpdateConfirmRepository extends JpaRepository<BalanceUpdateConfirmEntity, UUID> {
    int TIMEOUT_MS = 5000;

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BalanceUpdateConfirmEntity> findByBalanceUpdateReservationEntityId(UUID balanceUpdateReservationEntityId);

    @Query(name="balanceUpdateConfirm.findWhatever")
    Optional<BalanceUpdateConfirmEntity> findPessimisticBybalanceUpdateReservationEntityReservationCode(UUID reservationCode);
}
