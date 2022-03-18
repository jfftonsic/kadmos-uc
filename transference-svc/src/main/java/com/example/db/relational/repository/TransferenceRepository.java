package com.example.db.relational.repository;

import com.example.db.relational.entity.TransferenceEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface TransferenceRepository extends PagingAndSortingRepository<TransferenceEntity, UUID> {

    int TIMEOUT_MS = 5000;

//    @Query(value = "select b from balance b")
//    @Transactional(propagation = Propagation.REQUIRED, timeout = TIMEOUT_MS)
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    List<TransferenceEntity> getBalanceForUpdate(Pageable pageable);
}
