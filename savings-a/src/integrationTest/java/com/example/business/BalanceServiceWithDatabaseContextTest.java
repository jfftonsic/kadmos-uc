package com.example.business;

import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import com.example.util.CustomChecking;
import com.example.util.FakeClockConfiguration;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

@ActiveProfiles("integrationTest")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FakeClockConfiguration.class)
@Slf4j
@ComponentScan(basePackageClasses = BalanceService.class,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BalanceService.class),
                //                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UpdateReservationInitService.class),
        }
)
@TestPropertySource(properties = {
        "spring.datasource.hikari.auto-commit=false",
        "logging.level.org.hibernate.resource.jdbc.internal=INFO",
        "logging.level.org.hibernate.SQL=INFO",
        "logging.level.org.hibernate.type.descriptor.sql=INFO",
})
class BalanceServiceWithDatabaseContextTest {

    public static final String IDEM_CODE_1 = "idemcode1";
    public static final String IDEM_ACTOR_1 = "idemactor1";
    public static final BigDecimal AMOUNT_DEBIT_20 = BigDecimal.valueOf(-20L);
    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    BalanceUpdateReservationRepository balanceUpdateReservationRepository;

    @Autowired
    UpdateReservationInitService updateReservationInitService;

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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserve_notEnoughBalance() {
        balanceRepository.addFunds(BigDecimal.TEN);
        final var fakeNow = ZonedDateTime.now(clock);
        final var reservationCode = updateReservationInitService.initUpdateReservation(IDEM_CODE_1,
                IDEM_ACTOR_1,
                fakeNow,
                AMOUNT_DEBIT_20);

        assertThrowsExactly(NotEnoughBalanceException.class, () -> balanceService.reserve(reservationCode.toString()));

        final var reservationOpt = balanceUpdateReservationRepository.findByReservationCode(reservationCode);
        assertTrue(reservationOpt.isPresent());
        final var reservation = reservationOpt.get();

        // cannot use equals because our ID is final, so we can't generate an "expected entity instance"
        CustomChecking.checkBalanceUpdateReservationEntity(
                reservation
                , Optional.of(IDEM_CODE_1)
                , Optional.of(IDEM_ACTOR_1)
                , Optional.of(reservationCode)
                , Optional.of(BUSINESS_RULE_VIOLATION.getDbValue())
                , Optional.of(fakeNow)
                , Optional.of(AMOUNT_DEBIT_20)
        );
    }

//
//    @Test
//    void a() throws NotEnoughBalanceException, ExecutionException, InterruptedException {
//
//        final var threadPool = Executors.newScheduledThreadPool(2);
//        final Supplier<String> stringCallable = () -> {
//            try {
//                return balanceService.reserve(
//                        "test"
//                );
//            } catch (NotEnoughBalanceException e) {
//                log.error("Thread {}", Thread.currentThread().getId(), e);
//                throw new RuntimeException(e);
//            }
//        };
//
//        var completableFuture1 = CompletableFuture.supplyAsync(stringCallable, threadPool)
//                .exceptionally(throwable -> {
//                    log.error("Exception thrown for completableFuture1", throwable);
//                    return "";
//                });
//        var completableFuture2 = CompletableFuture.supplyAsync(stringCallable, threadPool)
//                .exceptionally(throwable -> {
//                    log.error("Exception thrown for completableFuture2", throwable);
//                    return "";
//                });
//
//        final var unused = CompletableFuture.allOf(completableFuture1, completableFuture2).get();
//
//        System.out.println();
//    }
//    //    @DynamicPropertySource
//    //    static void postgresqlProperties(DynamicPropertyRegistry registry) {
//    //        registry.add("spring.datasource.hikari.auto-commit", ()->false);
//    //        registry.add("logging.level.org.hibernate.resource.jdbc.internal", ()->"INFO");
//    //    }
}