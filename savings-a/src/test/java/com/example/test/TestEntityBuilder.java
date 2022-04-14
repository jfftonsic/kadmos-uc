package com.example.test;

import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import com.example.db.relational.entity.BalanceUpdateUndoEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class TestEntityBuilder {

    public static final String IDEM_CODE1 = "082414aa-3812-47fa-aeed-ea69ddc53b12";
    public static final String IDEM_ACTOR1 = "actor1";

    public static BalanceEntity buildBalanceEntity(long total, long onHold) {
        return BalanceEntity.builder()
                .totalAmount(BigDecimal.valueOf(total))
                .onHoldAmount(BigDecimal.valueOf(onHold))
                .build();
    }

    public static BalanceUpdateUndoEntity buildUndoEntity(UUID uuid, boolean done, ZonedDateTime requestTimestamp) {
        final var entity = BalanceUpdateUndoEntity.builder()
                .done(done)
                .requestTimestamp(requestTimestamp)
                .build();
        ReflectionTestUtils.setField(entity, "id", uuid);
        return entity;
    }

    public static BalanceUpdateUndoEntity buildDoneUndoEntity() {
        return buildUndoEntity(
                null,
                true,
                null
        );
    }

    public static BalanceUpdateUndoEntity buildNotDoneUndoEntity() {
        return buildUndoEntity(
                null,
                false,
                null
        );
    }

    public static BalanceUpdateReservationEntity buildReservationEntity(
            UUID uuid,
            UUID reservationCode,
            String idempotencyActor,
            String idempotencyCode,
            long amount,
            ZonedDateTime requestTimestamp,
            BalanceUpdateReservationStatus status
    ) {
        final var entity = BalanceUpdateReservationEntity.builder()
                .amount(BigDecimal.valueOf(amount))
                .idempotencyActor(idempotencyActor)
                .idempotencyCode(idempotencyCode)
                .requestTimestamp(requestTimestamp)
                .reservationCode(reservationCode)
                .status(status.getDbValue())
                .build();
        ReflectionTestUtils.setField(entity, "id", uuid);
        return entity;
    }
}
