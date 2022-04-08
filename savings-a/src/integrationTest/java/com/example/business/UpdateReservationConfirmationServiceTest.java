package com.example.business;

import com.example.LoggerAspect;
import com.example.business.UpdateReservationInitService.ConfirmUnknownReservationException;
import com.example.business.api.IBalanceService;
import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.exception.service.NotEnoughBalanceException;
import static com.example.util.BoilerplateUtil.getExactlyOne;
import com.example.util.FakeClockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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
    public static final String TEST_IDEM_CODE2 = "test2";
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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void confirmUpdateReservation_successCreditAndDebit()
            throws NotEnoughBalanceException, ConfirmUnknownReservationException {

        addFunds(100);

        final UUID reservationCode;
        final var fakeNow = ZonedDateTime.now(clock);

        reservationCode = initAndReserve(fakeNow, BigDecimal.valueOf(10), TEST_IDEM_CODE);

        var confirmationId = updateReservationInitService.initConfirmation(reservationCode, fakeNow);

        var response = updateReservationConfirmationService.confirmUpdateReservation(UUID.fromString(
                "5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"), confirmationId);
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND, response);

        response = updateReservationConfirmationService.confirmUpdateReservation(reservationCode,
                UUID.fromString("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"));
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.CONFIRMATION_NOT_FOUND, response);

        response = updateReservationConfirmationService.confirmUpdateReservation(reservationCode, confirmationId);
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.DONE, response);

        response = updateReservationConfirmationService.confirmUpdateReservation(reservationCode, confirmationId);
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.NO_CHANGES, response);

        assertEquals(0, balanceService.fetchAmount().compareTo(BigDecimal.valueOf(110L)));

        var burEntity = getExactlyOne(balanceUpdateReservationRepository.findByReservationCode(
                reservationCode), "update reservation");

        assertEquals(BalanceUpdateReservationStatus.CONFIRMED, burEntity.getStatusEnum());

        final var reservationCodeDebit = initAndReserve(fakeNow, BigDecimal.valueOf(-20L), TEST_IDEM_CODE2);
        confirmationId = updateReservationInitService.initConfirmation(reservationCodeDebit, fakeNow);

        response = updateReservationConfirmationService.confirmUpdateReservation(reservationCodeDebit, confirmationId);
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.DONE, response);

        response = updateReservationConfirmationService.confirmUpdateReservation(reservationCodeDebit, confirmationId);
        assertEquals(IBalanceService.ConfirmUpdateReservationResponse.NO_CHANGES, response);

        assertEquals(0, balanceService.fetchAmount().compareTo(BigDecimal.valueOf(90L)));

        burEntity = getExactlyOne(balanceUpdateReservationRepository.findByReservationCode(
                reservationCodeDebit), "update reservation");

        assertEquals(BalanceUpdateReservationStatus.CONFIRMED, burEntity.getStatusEnum());

    }

    @NotNull
    private UUID initAndReserve(ZonedDateTime fakeNow, BigDecimal amount, String idemCode)
            throws NotEnoughBalanceException {
        final UUID reservationCode;
        reservationCode = updateReservationInitService.initUpdateReservation(idemCode,
                TEST_ACTOR,
                fakeNow,
                amount);
        balanceService.reserve(reservationCode.toString());
        return reservationCode;
    }

    public void addFunds(int amount) {
        balanceRepository.addFunds(BigDecimal.valueOf(amount));
    }
}
