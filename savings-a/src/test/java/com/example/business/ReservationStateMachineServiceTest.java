package com.example.business;

import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CANCELED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.UNMAPPED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationStateMachineServiceTest {

    ReservationStateMachineService service = new ReservationStateMachineService();

    @Test
    public void immutability() {
        final var validChanges = service.getValidChanges();
        assertThrows(UnsupportedOperationException.class, () -> validChanges.put(UNMAPPED, Set.of(RECEIVED)));
        final var fromReceivedSet = validChanges.get(RECEIVED);
        assertNotNull(fromReceivedSet);
        assertThrows(UnsupportedOperationException.class, () -> fromReceivedSet.add(UNMAPPED));
    }

    static Stream<Arguments> inputsDatasource() {
        return Stream.of(
                arguments(null, null, false, "Any null parameter should result in false."),
                arguments(null, RECEIVED, false, "Any null parameter should result in false."),
                arguments(RECEIVED, null, false, "Any null parameter should result in false."),
                arguments(BalanceUpdateReservationStatus.values(),
                        UNMAPPED,
                        false,
                        "Any verification with unmapped being the destination should result in false."),
                arguments(UNMAPPED,
                        Arrays.stream(BalanceUpdateReservationStatus.values())
                                .filter(x -> !x.equals(UNMAPPED))
                                .toArray(BalanceUpdateReservationStatus[]::new),
                        true,
                        "Trying to move an UNMAPPED to any other status should be allowed."),
                arguments(CONFIRMED, RESERVED, false, CONFIRMED.name() + " is a final status."),
                arguments(CANCELED, RESERVED, false, CANCELED.name() + " is a final status."),
                arguments(BUSINESS_RULE_VIOLATION,
                        RESERVED,
                        false,
                        BUSINESS_RULE_VIOLATION.name() + " is a final status.")
                ,
                arguments(RECEIVED, initialExcept(values(), RESERVED), false, "Invalid destination")
                ,
                arguments(RESERVED,
                        initialExcept(values(), CONFIRMED, CANCELED, BUSINESS_RULE_VIOLATION),
                        false,
                        "Invalid destination")
                ,
                arguments(RECEIVED, RESERVED, true, "Should be validated.")
                ,
                arguments(RESERVED, toArray(CONFIRMED, CANCELED, BUSINESS_RULE_VIOLATION), true, "Should be validated.")
        );
    }

    static BalanceUpdateReservationStatus[] initialExcept(BalanceUpdateReservationStatus[] initial,
            BalanceUpdateReservationStatus... except) {
        final var collect = Arrays.stream(initial).collect(Collectors.toSet());
        Arrays.stream(except).toList().forEach(collect::remove);
        return collect.toArray(BalanceUpdateReservationStatus[]::new);
    }

    static BalanceUpdateReservationStatus[] toArray(BalanceUpdateReservationStatus... elements) {
        return Arrays.stream(elements).toArray(BalanceUpdateReservationStatus[]::new);
    }

    Set<BalanceUpdateReservationStatus> treatArgument(Object arg) {
        Set<BalanceUpdateReservationStatus> set = new HashSet<>();
        if (arg == null) {
            set.add(null);
        } else if (BalanceUpdateReservationStatus.class.isAssignableFrom(arg.getClass())) {
            set.add((BalanceUpdateReservationStatus) arg);
        } else if (BalanceUpdateReservationStatus[].class.isAssignableFrom(arg.getClass())) {
            final var castFrom = (BalanceUpdateReservationStatus[]) arg;
            set.addAll(Arrays.stream(castFrom).toList());
        }
        return set;
    }

    @ParameterizedTest
    @MethodSource("inputsDatasource")
    void testWithMultiArgMethodSource(
            Object from,
            Object to,
            boolean expected,
            String assertMsg
    ) {
        Set<BalanceUpdateReservationStatus> fromSet = treatArgument(from);
        Set<BalanceUpdateReservationStatus> toSet = treatArgument(to);

        fromSet.forEach(fe -> toSet.forEach(te -> {
            final var actual = service.isStatusChangeValid(fe, te);
            assertEquals(expected, actual, assertMsg);
        }));

    }
}
