package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BalanceRepository extends PagingAndSortingRepository<BalanceEntity, UUID> {

    int TIMEOUT_MS = 5000;
    String QUERY_GET_AMOUNT = "select " + BalanceEntity.COL_AMOUNT + " from " + BalanceEntity.TABLE_NAME_BALANCE
            + " limit 1";

    @Query(value = QUERY_GET_AMOUNT, nativeQuery = true)
    @Transactional(propagation = Propagation.SUPPORTS)
    List<BigDecimal> getBalanceAmount();

    @Query(value = "select b from balance b")
    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<BalanceEntity> getBalanceForUpdate(Pageable pageable);
}
