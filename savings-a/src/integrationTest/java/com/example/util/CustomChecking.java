package com.example.util;

import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class CustomChecking {

    /**
     * The optional holds the expected instance, it will validate it with equals.
     *
     * @param opt    if opt == null, the method will check that the actual is also null. If opt.isPresent it will check
     *               that equals the actual. If opt.isEmpty it will assume you don't want any validation for this
     *               'actual'.
     * @param actual the instance you want to check
     */
    public static <T> void check(@Nullable Optional<T> opt, T actual) {
        //noinspection OptionalAssignedToNull
        if (opt == null) {
            assertNull(actual);
        } else {
            opt.ifPresent(o -> {
                testEquals(o, actual);
            });
        }
    }

    private static <T> void testEquals(T o, T actual) {
        if (BigDecimal.class.isAssignableFrom(o.getClass())) {
            assertEquals(0,
                    ((BigDecimal) o).compareTo((BigDecimal) actual),
                    () -> "Expected %s but was %s".formatted(o.toString(), actual.toString()));
        } else if (BigInteger.class.isAssignableFrom(o.getClass())) {
            assertEquals(0,
                    ((BigInteger) o).compareTo((BigInteger) actual),
                    () -> "Expected %s but was %s".formatted(o.toString(), actual.toString()));
        } else if (ZonedDateTime.class.isAssignableFrom(o.getClass())) {
            final var castO = (ZonedDateTime) o;
            assertEquals(castO.toEpochSecond(), ((ZonedDateTime) actual).toEpochSecond());
        } else {
            assertEquals(o, actual);
        }
    }

    public static void checkBalanceUpdateReservationEntity(
            BalanceUpdateReservationEntity actual,
            Optional<String> idempotencyCode,
            Optional<String> idempotencyActor,
            Optional<UUID> reservationCode,
            Optional<Integer> status,
            Optional<ZonedDateTime> requestTimestamp,
            Optional<BigDecimal> amount
    ) {
        check(idempotencyCode, actual.getIdempotencyCode());
        check(idempotencyActor, actual.getIdempotencyActor());
        check(reservationCode, actual.getReservationCode());
        check(status, actual.getStatus());
        check(requestTimestamp, actual.getRequestTimestamp());
        check(amount, actual.getAmount());
    }

    @SuppressWarnings({ "OptionalAssignedToNull", "ConstantConditions" })
    public static <T> void check(T expected, Optional<T> optional) {
        assertFalse(expected == null && (optional != null || optional.isPresent()));
        assertFalse(expected != null && optional.isEmpty());
        if (expected != null && optional.isPresent()) {
            testEquals(expected, optional.get());
        }
    }
}
