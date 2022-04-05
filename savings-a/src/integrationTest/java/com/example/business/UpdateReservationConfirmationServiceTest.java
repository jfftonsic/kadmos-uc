package com.example.business;

import com.example.LoggerAspect;
import com.example.business.api.IBalanceService;
import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import static com.example.util.BoilerplateUtil.getExactlyOne;
import com.example.util.FakeClockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

@ActiveProfiles("integrationTest")
@DataJpaTest(
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = LoggerAspect.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = BalanceService.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = UpdateReservationConfirmationService.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = UpdateReservationInitService.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com.example.db.relational.repository.*"
                )
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FakeClockConfiguration.class)
@EnableAspectJAutoProxy
@Slf4j
@TestPropertySource(properties = {
        "spring.datasource.hikari.auto-commit=false",
        "logging.level.org.hibernate.resource.jdbc.internal=INFO",
        "logging.level.org.hibernate.SQL=TRACE",
        "logging.level.org.hibernate.type.descriptor.sql=INFO",
})
public class UpdateReservationConfirmationServiceTest {
    public static final String TEST_ACTOR = "testActor";
    public static final String TEST_IDEM_CODE = "test";
    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    BalanceUpdateReservationRepository balanceUpdateReservationRepository;

    @Autowired
    Clock clock;

    @Autowired
    BalanceService balanceService;

    @Autowired
    UpdateReservationConfirmationService updateReservationConfirmationService;

    @Autowired
    UpdateReservationInitService updateReservationInitService;

    UpdateReservationConfirmationServiceTest self;

    @PostConstruct
    public void postConstruct() {
        self = this;
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void a() throws NotEnoughBalanceException {
        Assertions.assertNotNull(self);
        b();
        c();

        final UUID reservationCode;
        //                try {
        reservationCode = updateReservationInitService.initUpdateReservation(TEST_IDEM_CODE,
                TEST_ACTOR,
                ZonedDateTime.now(clock),
                BigDecimal.valueOf(100));
        //                } catch (DataIntegrityViolationException e) {
        //                    throw new BalanceService.CollidingIdempotencyException(
        //                            "There already is a balance update reservation with idempotency pair (%s, %s)"
        //                                    .formatted(idemCode, idemActor), e);
        //                }

        balanceService.reserve(reservationCode.toString());

        //        final String updateReservationCode = self.createReservation();

        final var response = self.getConfirmUpdateReservationResponse(reservationCode.toString());

        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.DONE, response);

        final var response2 = self.getConfirmUpdateReservationResponse(reservationCode.toString());

        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.NO_CHANGES, response2);

        final var response3 = self.getConfirmUpdateReservationResponse("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"); // this uuid has no special meaning

        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND, response3);

        assertEquals(0, balanceService.fetchAmount().compareTo(BigDecimal.valueOf(110L)));
        final var burEntity = getExactlyOne(balanceUpdateReservationRepository.findByReservationCode(
                reservationCode), "update reservation");

        assertEquals(BalanceUpdateReservationStatus.CONFIRMED, burEntity.getStatusEnum());
        assertEquals(0, burEntity.getAmount().compareTo(BigDecimal.valueOf(10L)));
        assertEquals(TEST_ACTOR, burEntity.getIdempotencyActor());
        assertEquals(TEST_IDEM_CODE, burEntity.getIdempotencyCode());
        assertEquals(ZonedDateTime.now(clock), burEntity.getRequestTimestamp());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private IBalanceService.ConfirmUpdateReservationResponse getConfirmUpdateReservationResponse(
            String updateReservationCode) {
        return updateReservationConfirmationService.confirmUpdateReservation(UUID.fromString(
                updateReservationCode), UUID.fromString(
                updateReservationCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String createReservation() throws NotEnoughBalanceException {
        final var updateReservationCode = balanceService.reserve(
                TEST_IDEM_CODE
        );
        return updateReservationCode;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void b() {
        balanceRepository.addFunds(BigDecimal.valueOf(100));
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void c() {
        final var balanceAmount = balanceRepository.getBalanceForUpdateNative();
        log.info("method c number of balances={}", balanceAmount);
    }
}
