package com.example.business;

import com.example.business.api.IBalanceService;
import static com.example.business.api.IBalanceService.UndoUpdateReservationResponse.DONE;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CANCELED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.db.relational.repository.BalanceUpdateUndoRepository;
import com.example.test.TestEntityBuilder;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateReservationUndoServiceTest {

    public static final UUID UUID1 = UUID.fromString("ec2aaf17-7106-445c-ae2c-93f72f24a2b4");
    public static final UUID UUID2 = UUID.fromString("ef70e34b-4f13-4357-8296-66a3e7cea80c");
    public static final UUID UUID3 = UUID.fromString("c2b46e3e-6f0e-4f90-ab1b-6472103323a9");

    public static final Clock FAKE_CLOCK = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

    @Mock
    ReservationStateMachineService stateMachineService;
    @Mock
    BalanceRepository balanceRepository;
    @Mock
    BalanceUpdateReservationRepository updateReservationRepository;
    @Mock
    BalanceUpdateUndoRepository undoRepository;

    @InjectMocks
    UpdateReservationUndoService service;

    @Test
    void undoUpdateReservation_undoNotFound() {
        when(undoRepository.findById(eq(UUID2))).thenReturn(empty());

        final var actual = service.undoUpdateReservation(UUID1, UUID2);

        assertEquals(IBalanceService.UndoUpdateReservationResponse.UNDO_NOT_FOUND, actual);
    }

    @Test
    void undoUpdateReservation_undoFoundButAlreadyDone() {
        when(undoRepository.findById(eq(UUID2))).thenReturn(of(TestEntityBuilder.buildDoneUndoEntity()));

        final var actual = service.undoUpdateReservation(UUID1, UUID2);

        assertEquals(IBalanceService.UndoUpdateReservationResponse.NO_CHANGES, actual);
    }

    @Test
    void undoUpdateReservation_undoFoundButReservationNot() {
        when(undoRepository.findById(eq(UUID2))).thenReturn(of(TestEntityBuilder.buildNotDoneUndoEntity()));
        when(updateReservationRepository.findByReservationCode(UUID1)).thenReturn(empty());

        final var actual = service.undoUpdateReservation(UUID1, UUID2);

        assertEquals(IBalanceService.UndoUpdateReservationResponse.UPDATE_RESERVATION_NOT_FOUND, actual);
    }

    @Test
    void undoUpdateReservation_fullSuccessFlow_debitReservation() {
        final var amount = -10L;
        final var fakeNow = ZonedDateTime.now(FAKE_CLOCK);
        final var undo = TestEntityBuilder.buildUndoEntity(
                UUID2,
                false,
                fakeNow
        );
        final var undoOpt = of(undo);
        when(undoRepository.findById(eq(UUID2))).thenReturn(undoOpt, undoOpt);
        when(updateReservationRepository.findByReservationCode(eq(UUID1)))
                .thenReturn(of(TestEntityBuilder.buildReservationEntity(
                        UUID3,
                        UUID1,
                        TestEntityBuilder.IDEM_ACTOR1,
                        TestEntityBuilder.IDEM_CODE1,
                        amount,
                        fakeNow,
                        RESERVED
                )));
        final var reservation = TestEntityBuilder.buildReservationEntity(
                UUID3,
                UUID1,
                TestEntityBuilder.IDEM_ACTOR1,
                TestEntityBuilder.IDEM_CODE1,
                amount,
                fakeNow,
                RESERVED
        );
        when(updateReservationRepository.findAndLockByReservationCode(eq(UUID1)))
                .thenReturn(of(reservation));
        when(stateMachineService.isStatusChangeValid(eq(RESERVED), eq(CANCELED))).thenReturn(true);

        final var balanceEntity = TestEntityBuilder.buildBalanceEntity(0L, 30L);
        when(balanceRepository.getBalanceForUpdateNative()).thenReturn(balanceEntity);

        final var actual = service.undoUpdateReservation(UUID1, UUID2);

        assertEquals(true, undo.getDone());
        assertEquals(CANCELED, reservation.getStatusEnum());
        assertEquals(0, BigDecimal.valueOf(20L).compareTo(balanceEntity.getOnHoldAmount()));
        assertEquals(0, BigDecimal.valueOf(0L).compareTo(balanceEntity.getTotalAmount()));
        assertEquals(DONE, actual);

    }

    // ATTENTION:
    // I'm going to list here some scenarios to test but won't implement them for now.
    // The purpose is to invest more time in things that I think are more probable to teach me something.
    @Test
    void undoUpdateReservation_invalidStateChange_StatusReceived() {
    }

    @Test
    void undoUpdateReservation_invalidStateChange_StatusCanceled() {
    }

    @Test
    void undoUpdateReservation_invalidStateChange_StatusBusinessRuleViolation() {
    }

    @Test
    void undoUpdateReservation_invalidStateChange_StatusConfirmed() {
    }

    @Test
    void undoUpdateReservation_missingReservationWhenEnteringLockedContext() {
    }

    @Test
    void undoUpdateReservation_reservationStatusAfterLockBecameInvalid() {
    }

    @Test
    void undoUpdateReservation_missingUndoWhenInsideLockedContext() {
    }

    @Test
    void undoUpdateReservation_fullSuccessFlow_creditReservation() {

    }

}