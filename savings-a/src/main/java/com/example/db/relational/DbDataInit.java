package com.example.db.relational;

import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.repository.BalanceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DbDataInit {

    BalanceRepository balanceRepository;

    @PostConstruct
    @Transactional
    public void run() {
//        log.info("Running database contents initialization");
//
//        final var balanceEntities = balanceRepository.lockToWriteFindAny(PageRequest.of(0, 1));
//        if (balanceEntities.isEmpty()) {
//            final BalanceEntity balanceEntity = BalanceEntity.builder().amount(BigDecimal.ZERO).build();
//            balanceRepository.save(balanceEntity);
//        }
    }
}
