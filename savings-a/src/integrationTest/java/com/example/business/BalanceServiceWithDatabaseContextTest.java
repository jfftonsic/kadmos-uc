package com.example.business;

import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import com.example.util.FakeClockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@ActiveProfiles("integrationTest")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FakeClockConfiguration.class)
@Slf4j
@ComponentScan(basePackageClasses = BalanceService.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BalanceService.class))
@TestPropertySource(properties = {
        "spring.datasource.hikari.auto-commit=false",
        "logging.level.org.hibernate.resource.jdbc.internal=INFO",
        "logging.level.org.hibernate.SQL=INFO",
        "logging.level.org.hibernate.type.descriptor.sql=INFO",
})
class BalanceServiceWithDatabaseContextTest {

    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    BalanceUpdateReservationRepository balanceUpdateReservationRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    BalanceService balanceService;

    @Autowired
    Clock clock;

    @BeforeEach
    public void beforeEach() {
        //        balanceService = new BalanceService(
        //                balanceRepository,
        //                balanceUpdateReservationRepository,
        //                entityManager
        //        );
    }

    @Test
    void a() throws NotEnoughBalanceException, ExecutionException, InterruptedException {

        final var threadPool = Executors.newScheduledThreadPool(2);
        final Supplier<String> stringCallable = () -> {
            try {
                return balanceService.reserve(
                        "test"
                );
            } catch (NotEnoughBalanceException e) {
                log.error("Thread {}", Thread.currentThread().getId(), e);
                throw new RuntimeException(e);
            }
        };

        var completableFuture1 = CompletableFuture.supplyAsync(stringCallable, threadPool)
                .exceptionally(throwable -> {
                    log.error("Exception thrown for completableFuture1", throwable);
                    return "";
                });
        var completableFuture2 = CompletableFuture.supplyAsync(stringCallable, threadPool)
                .exceptionally(throwable -> {
                    log.error("Exception thrown for completableFuture2", throwable);
                    return "";
                });

        final var unused = CompletableFuture.allOf(completableFuture1, completableFuture2).get();

        System.out.println();
    }
    //    @DynamicPropertySource
    //    static void postgresqlProperties(DynamicPropertyRegistry registry) {
    //        registry.add("spring.datasource.hikari.auto-commit", ()->false);
    //        registry.add("logging.level.org.hibernate.resource.jdbc.internal", ()->"INFO");
    //    }
}