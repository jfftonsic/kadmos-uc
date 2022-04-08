package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceUpdateConfirmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceUpdateConfirmRepository extends JpaRepository<BalanceUpdateConfirmEntity, UUID> {
    int TIMEOUT_S = 5;

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_S)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // one reservation can have multiple rows of confirmation
    List<BalanceUpdateConfirmEntity> findByBalanceUpdateReservationEntityId(UUID balanceUpdateReservationEntityId);

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_S)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BalanceUpdateConfirmEntity> findAndLockById(UUID id);

    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_S)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BalanceUpdateConfirmEntity> findAndLockByBalanceUpdateReservationEntityReservationCode(UUID reservationCode);

    @Query(name="existsDoneDoesNotNeedToBeSameNameAsMethod")
    boolean existsDone(UUID reservationCode); //@Param("reservationCode")

    /** This is an example of a named query that is defined at jpa-named-queries.properties */
    @Query(name="randomJPQLQuery", nativeQuery = true)
    Optional<Boolean> couldBeWhicheverNameIWanted();
}
