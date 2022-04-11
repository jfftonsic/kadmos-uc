package com.example.db.relational;

import com.example.ExceptionalSupplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.function.Supplier;

/**
 * This is one example of a more low-level, manual way to control transactions.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionHandler {

//    JdbcTemplate jdbcTemplate;
    EntityManager entityManager;
//    DataSourceTransactionManager transactionManager;
    TransactionTemplate transactionTemplate;
    PlatformTransactionManager transactionManager;

    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T runInTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T runInNewTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    public <T> T runInTransaction(
            Isolation isolation,
            Propagation propagation,
            String transactionName,
            int timeoutSec,
            boolean readOnly,
            ExceptionalSupplier<T> supplier
    ) throws Exception {

        final var defaultTransactionDefinition = new DefaultTransactionDefinition(propagation.value());
        defaultTransactionDefinition.setIsolationLevel(isolation.value());
        defaultTransactionDefinition.setName(transactionName);
        defaultTransactionDefinition.setTimeout(timeoutSec);
        defaultTransactionDefinition.setReadOnly(readOnly);
        final var transactionStatus = transactionManager.getTransaction(defaultTransactionDefinition);
        if (transactionStatus.isNewTransaction()) {
            log.info("New transaction.");
        }
        try {
            final var t = supplier.get();

            transactionManager.commit(transactionStatus);
            return t;
        } catch (RuntimeException | Error throwable) {
            transactionManager.rollback(transactionStatus);
            throw throwable;
        }
    }
}
