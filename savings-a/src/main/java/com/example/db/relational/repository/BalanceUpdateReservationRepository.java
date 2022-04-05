package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface BalanceUpdateReservationRepository extends JpaRepository<BalanceUpdateReservationEntity, UUID> {

    int TIMEOUT_MS = 5000;

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(name="balanceUpdateReservation.findAndPessimisticWriteLockByReservationCode")
    Optional<BalanceUpdateReservationEntity> findAndPessimisticWriteLockByReservationCode(UUID reservationCode);

    Optional<BalanceUpdateReservationEntity> findByReservationCode(UUID reservationCode);
}
