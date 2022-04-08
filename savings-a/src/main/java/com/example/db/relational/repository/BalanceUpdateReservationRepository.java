package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface BalanceUpdateReservationRepository extends JpaRepository<BalanceUpdateReservationEntity, UUID> {

    int TIMEOUT_S = 5;

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_S)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BalanceUpdateReservationEntity> findAndLockByReservationCode(UUID reservationCode);

    Optional<BalanceUpdateReservationEntity> findByReservationCode(UUID reservationCode);
}
