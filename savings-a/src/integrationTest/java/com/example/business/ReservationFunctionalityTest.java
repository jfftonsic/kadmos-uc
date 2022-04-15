package com.example.business;

import com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse;
import com.example.business.api.IBalanceService.UndoUpdateReservationResponse;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateConfirmRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.db.relational.repository.BalanceUpdateUndoRepository;
import com.example.exception.service.NotEnoughBalanceException;
import com.example.util.CustomChecking;
import static com.example.util.CustomChecking.check;
import static com.example.util.CustomChecking.checkBalanceUpdateReservationEntity;
import com.example.util.FakeClockConfiguration;
import static java.util.Optional.of;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * It makes no sense to integration test separately the balance service classes because operations depend on one
 * another. E.g. if you want to test the confirmation, you need to perform every step that comes before it... If you say
 * "oh, so let's just prepare the database with what confirmation needs and do just confirmation" but then, or you end
 * up reimplementing functionality similar to the actual implementation, or it will be different and if there are
 * modifications on steps performed before confirmation you'll need to replicate them to the preparation you did
 * (increasing maintenance costs).
 * <p>
 * There are also a (possibly big) quantity of use cases that could be tested here and are not, for example: - 2 debit
 * reservations (without confirming) to check if the on hold amounts get added. - make a $10 credit reservation, don't
 * confirm, try to make a $5 debit reservation. It should fail. - perform different kinds of operations concurrently and
 * see to the consistency.
 */
// when a test fails, and you want more information, use "verboseDatabaseLogs" profile
// or else, use "nonVerboseDatabaseLogs"
@SuppressWarnings("ResultOfMethodCallIgnored") @ActiveProfiles({ "nonVerboseDatabaseLogs", "integrationTest" })
@DataJpaTest(includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BalanceService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UpdateReservationInitService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UpdateReservationConfirmationService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UpdateReservationUndoService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ReservationStateMachineService.class),
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FakeClockConfiguration.class)
@Slf4j
public class ReservationFunctionalityTest {

    public static final String IDEM_CODE_1 = "idemcode1";
    public static final String IDEM_ACTOR_1 = "idemactor1";
    public static final BigDecimal AMOUNT_DEBIT_20 = BigDecimal.valueOf(-20L);

    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    BalanceUpdateReservationRepository reservationRepository;

    @Autowired
    BalanceUpdateConfirmRepository confirmRepository;

    @Autowired
    BalanceUpdateUndoRepository undoRepository;

    @Autowired
    UpdateReservationInitService updateReservationInitService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    UpdateReservationConfirmationService updateReservationConfirmationService;

    @Autowired
    UpdateReservationUndoService updateReservationUndoService;

    @Autowired
    Clock clock;

    ZonedDateTime fakeNow;

    @PostConstruct
    public void post() {
        fakeNow = ZonedDateTime.now(clock);
    }

    // The only way I found to reset a dirty context without triggering restart of everything at each method end is this.
    // The problem is "Oh I must delete this row first because that other row from another table points to this row via FK"
    @AfterEach
    public void afterEach() {
        final var updateCount = balanceRepository.resetBalance();
        log.info("afterEach updateCount={}", updateCount);
        confirmRepository.deleteAll();
        undoRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("")
    void reserve_creditSuccess() throws NotEnoughBalanceException {
        checkCleanDatabase();

        initAndReserve(IDEM_CODE_1, IDEM_ACTOR_1, BigDecimal.TEN, fakeNow);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("")
    void reserve_debitSuccess() throws NotEnoughBalanceException {
        checkCleanDatabase();

        addFunds(BigDecimal.valueOf(100L));

        initAndReserve(IDEM_CODE_1, IDEM_ACTOR_1, AMOUNT_DEBIT_20, fakeNow);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("")
    void reserve_multiple() throws NotEnoughBalanceException {
        checkCleanDatabase();

        var reservationCode = initAndReserve(IDEM_CODE_1, IDEM_ACTOR_1, BigDecimal.valueOf(100L), fakeNow);
        var confirmId = initConfirm(reservationCode, null);
        checkAmounts(0L, 0L);

        confirm(UUID.fromString("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"),
                confirmId,
                ConfirmUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND,
                false,
                false,
                true,
                null);
        checkAmounts(0L, 0L);

        confirm(reservationCode,
                UUID.fromString("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"),
                ConfirmUpdateReservationResponse.CONFIRMATION_NOT_FOUND,
                false,
                true,
                false,
                null);
        checkAmounts(0L, 0L);

        confirm(reservationCode, confirmId, ConfirmUpdateReservationResponse.DONE, true,
                true,
                true, null);
        checkAmounts(100L, 0L);

        confirm(reservationCode, confirmId, ConfirmUpdateReservationResponse.NO_CHANGES, true, true,
                true, null);
        checkAmounts(100L, 0L);

        reservationCode = initAndReserve("idemCode2", IDEM_ACTOR_1, BigDecimal.valueOf(-60L), fakeNow);
        checkAmounts(100L, 60L);
        confirmId = initConfirm(reservationCode, null);
        checkAmounts(100L, 60L);
        confirm(reservationCode, confirmId, ConfirmUpdateReservationResponse.DONE, true, true,
                true, null);
        checkAmounts(40L, 0L);
        confirm(reservationCode, confirmId, ConfirmUpdateReservationResponse.NO_CHANGES, true, true,
                true, null);
        checkAmounts(40L, 0L);

        assertThrowsExactly(NotEnoughBalanceException.class,
                () -> initAndReserve("idemCode3", IDEM_ACTOR_1, BigDecimal.valueOf(-60L), fakeNow));

        var undoId = initUndo(reservationCode, null);
        undo(UUID.fromString("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"),
                undoId,
                UndoUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND,
                false,
                false,
                true,
                null);
        checkAmounts(40L, 0L);

        undo(reservationCode,
                UUID.fromString("5d835a9b-0045-43e0-bbd0-fbdaa5d309a9"),
                UndoUpdateReservationResponse.UNDO_NOT_FOUND,
                false,
                true,
                false,
                null);
        checkAmounts(40L, 0L);

        undo(reservationCode,
                undoId,
                UndoUpdateReservationResponse.WAS_ALREADY_CONFIRMED,
                false,
                true,
                true,
                null);
        checkAmounts(40L, 0L);

        // make a credit reservation to undo successfully
        reservationCode = initAndReserve("idemCode4", IDEM_ACTOR_1, BigDecimal.valueOf(10), fakeNow);
        undoId = initUndo(reservationCode, null);
        undo(reservationCode,
                undoId,
                UndoUpdateReservationResponse.DONE,
                true,
                true,
                true,
                null);
        checkAmounts(40L, 0L);

        // make a debit reservation to undo successfully
        reservationCode = initAndReserve("idemCode5", IDEM_ACTOR_1, BigDecimal.valueOf(-10), fakeNow);
        checkAmounts(40L, 10L);
        undoId = initUndo(reservationCode, null);
        undo(reservationCode,
                undoId,
                UndoUpdateReservationResponse.DONE,
                true,
                true,
                true,
                null);
        checkAmounts(40L, 0L);

        // re-send a already done undo
        undo(reservationCode,
                undoId,
                UndoUpdateReservationResponse.NO_CHANGES,
                true,
                true,
                true,
                null);
        checkAmounts(40L, 0L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserve_notEnoughBalance() {
        checkCleanDatabase();

        addFunds(BigDecimal.TEN);

        final var reservationCode = initReservation(IDEM_CODE_1, IDEM_ACTOR_1, AMOUNT_DEBIT_20, fakeNow);

        assertThrowsExactly(NotEnoughBalanceException.class, () -> balanceService.reserve(reservationCode.toString()));

        final var reservationOpt = reservationRepository.findByReservationCode(reservationCode);
        assertTrue(reservationOpt.isPresent());
        final var reservation = reservationOpt.get();

        // cannot use equals because our ID is final, so we can't generate an "expected entity instance", equals() won't work.
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

    /**
     * provide at least a minimum of guarantee that the database is on a state that we expect
     */
    private void checkCleanDatabase() {
        assertEquals(0, reservationRepository.count());
        check(BigDecimal.ZERO, balanceRepository.getBalanceAmount());
        check(BigDecimal.ZERO, balanceRepository.getBalanceOnHoldAmount());
    }

    private void addFunds(BigDecimal amount) {
        balanceService.addFunds(amount);
        check(amount, balanceRepository.getBalanceAmount());
    }

    private UUID initConfirm(UUID reservationCode, Class<? extends Exception> expectedExceptionOrNull) {
        try {
            if (expectedExceptionOrNull != null) {
                Assertions.assertThrowsExactly(expectedExceptionOrNull,
                        () -> updateReservationInitService.initConfirmation(reservationCode, fakeNow));
            } else {
                var confirmationId = updateReservationInitService.initConfirmation(reservationCode, fakeNow);

                validateConfirmation(reservationCode, confirmationId, false, true, true);
                return confirmationId;
            }
        } catch (UpdateReservationInitService.ConfirmUnknownReservationException e) {
            Assertions.fail(e);
        }
        return null;
    }

    private UUID initUndo(UUID reservationCode, Class<? extends Exception> expectedExceptionOrNull) {
        try {
            if (expectedExceptionOrNull != null) {
                Assertions.assertThrowsExactly(expectedExceptionOrNull,
                        () -> updateReservationInitService.initUndo(reservationCode, fakeNow));
            } else {
                var id = updateReservationInitService.initUndo(reservationCode, fakeNow);

                validateUndo(reservationCode, id, false, true, true);
                return id;
            }
        } catch (UpdateReservationInitService.UndoUnknownReservationException e) {
            Assertions.fail(e);
        }
        return null;
    }

    private ConfirmUpdateReservationResponse confirm(
            UUID reservationCode,
            UUID confirmationId,
            ConfirmUpdateReservationResponse expectedResponse,
            boolean expectedDone,
            boolean thisReservationShouldExist,
            boolean thisConfirmationShouldExist,
            Class<? extends Exception> expectedExceptionOrNull
    ) {

        if (expectedExceptionOrNull != null) {
            assertThrowsExactly(expectedExceptionOrNull,
                    () -> updateReservationConfirmationService.confirmUpdateReservation(reservationCode,
                            confirmationId));
        } else {
            final var actualResponse = updateReservationConfirmationService.confirmUpdateReservation(
                    reservationCode,
                    confirmationId);
            assertEquals(expectedResponse, actualResponse);

            validateConfirmation(reservationCode,
                    confirmationId,
                    expectedDone,
                    thisReservationShouldExist,
                    thisConfirmationShouldExist);

            return actualResponse;
        }

        return null;
    }

    private UndoUpdateReservationResponse undo(
            UUID reservationCode,
            UUID undoId,
            UndoUpdateReservationResponse expectedResponse,
            boolean expectedDone,
            boolean thisReservationShouldExist,
            boolean thisUndoShouldExist,
            Class<? extends Exception> expectedExceptionOrNull
    ) {

        if (expectedExceptionOrNull != null) {
            assertThrowsExactly(expectedExceptionOrNull,
                    () -> updateReservationUndoService.undoUpdateReservation(reservationCode,
                            undoId));
        } else {
            final var actualResponse = updateReservationUndoService.undoUpdateReservation(
                    reservationCode,
                    undoId);
            assertEquals(expectedResponse, actualResponse);

            validateUndo(reservationCode,
                    undoId,
                    expectedDone,
                    thisReservationShouldExist,
                    thisUndoShouldExist);

            return actualResponse;
        }

        return null;
    }

    private void validateConfirmation(UUID reservationCode, UUID confirmationId, boolean expectedDone,
            boolean thisReservationShouldExist, boolean thisConfirmationShouldExist) {

        final var reservationOpt = reservationRepository.findByReservationCode(reservationCode);
        assertEquals(thisReservationShouldExist, reservationOpt.isPresent());
        reservationOpt.ifPresent(reservation -> {
            if (expectedDone) {
                assertEquals(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED,
                        reservation.getStatusEnum());
            }
        });

        final var confirmOpt = confirmRepository.findById(confirmationId);
        assertEquals(thisConfirmationShouldExist, confirmOpt.isPresent());
        confirmOpt.ifPresent(confirm -> assertEquals(expectedDone, confirm.getDone()));
    }

    private void validateUndo(UUID reservationCode, UUID undoId, boolean expectedDone,
            boolean thisReservationShouldExist, boolean thisUndoShouldExist) {

        final var reservationOpt = reservationRepository.findByReservationCode(reservationCode);
        assertEquals(thisReservationShouldExist, reservationOpt.isPresent());
        reservationOpt.ifPresent(reservation -> {
            if (expectedDone) {
                assertEquals(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CANCELED,
                        reservation.getStatusEnum());
            }
        });

        final var undoOpt = undoRepository.findById(undoId);
        assertEquals(thisUndoShouldExist, undoOpt.isPresent());
        undoOpt.ifPresent(undo -> assertEquals(expectedDone, undo.getDone()));
    }

    @NotNull
    private UUID initAndReserve(String idemCode, String idemActor, BigDecimal amount, ZonedDateTime fakeNow)
            throws NotEnoughBalanceException {
        final UUID reservationCode;
        reservationCode = initReservation(idemCode, idemActor, amount, fakeNow);
        balanceService.reserve(reservationCode.toString());
        validateReservation(fakeNow, amount, idemCode, idemActor, reservationCode,
                BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED.getDbValue());
        return reservationCode;
    }

    private UUID initReservation(String idemCode, String idemActor, BigDecimal amount, ZonedDateTime fakeNow) {
        final UUID reservationCode;
        reservationCode = updateReservationInitService.initUpdateReservation(
                idemCode,
                idemActor,
                fakeNow,
                amount
        );
        validateReservation(fakeNow,
                amount,
                idemCode,
                idemActor,
                reservationCode,
                BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED.getDbValue());
        return reservationCode;
    }

    private void validateReservation(ZonedDateTime fakeNow, BigDecimal amount, String idemCode, String idemActor,
            UUID reservationCode, Integer status) {
        final var reservationOpt = reservationRepository.findByReservationCode(reservationCode);
        assertTrue(reservationOpt.isPresent());
        final var reservation = reservationOpt.get();
        checkBalanceUpdateReservationEntity(
                reservation,
                of(idemCode),
                of(idemActor),
                of(reservationCode),
                of(status),
                of(fakeNow),
                of(amount)
        );
    }

    private void checkAmounts(long total, long onHold) {
        final var totalAmountOpt = balanceRepository.getBalanceAmount();
        final var onHoldAmountOpt = balanceRepository.getBalanceOnHoldAmount();

        check(BigDecimal.valueOf(total), totalAmountOpt);
        check(BigDecimal.valueOf(onHold), onHoldAmountOpt);
    }

}
