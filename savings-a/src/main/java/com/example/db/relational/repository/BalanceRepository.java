package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository extends PagingAndSortingRepository<BalanceEntity, UUID> {

    int TIMEOUT_MS = 5000;
    String QUERY_GET_AMOUNT = "select " + BalanceEntity.COL_TOTAL_AMOUNT + " from " + BalanceEntity.TABLE_NAME_BALANCE
            + " limit 1";

    @Query(value = QUERY_GET_AMOUNT, nativeQuery = true)
    @Transactional(propagation = Propagation.SUPPORTS)
    Optional<BigDecimal> getBalanceAmount();

    @Query(value = "select * from balance b limit 1 for update", nativeQuery = true)
    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    Optional<BalanceEntity> getBalanceForUpdateNative();

    @Query(value = "select b from balance b")
    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<BalanceEntity> getBalanceForUpdate(Pageable pageable);

    @Modifying
    @Query(value = "update balance set total_amount = total_amount + :amount", nativeQuery = true)
    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    int addFunds(BigDecimal amount);
}
