package com.example.business;

import com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.BUSINESS_RULE_VIOLATION;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CANCELED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RESERVED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.UNMAPPED;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.values;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service is to control reservation state changes.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ReservationStateMachineService {

    @Getter(AccessLevel.PACKAGE)
    Map<BalanceUpdateReservationStatus, Set<BalanceUpdateReservationStatus>> validChanges = Map.of(
            RECEIVED, Set.of(
                    RESERVED
            ),
            RESERVED, Set.of(
                    CONFIRMED,
                    CANCELED,
                    BUSINESS_RULE_VIOLATION
            ),
            UNMAPPED, Arrays.stream(values()).collect(Collectors.toUnmodifiableSet())
    );

    public boolean isStatusChangeValid(BalanceUpdateReservationStatus from, BalanceUpdateReservationStatus to) {
        if (from == null || to == null) {
            log.debug("a=stateChange r=NullFromOrTo from={} to={}", from, to);
            return false;
        }
        if (UNMAPPED.equals(to)) {
            // unmapped are for situations where you get a value from the database that doesn't exist on the enum
            log.debug("a=stateChange r=ToUnmapped from={} to={}", from, to);
            return false;
        }
        final var validTo = validChanges.get(from);
        if (validTo == null) {
            log.debug("a=stateChange r=FromHasNoValidDestinations from={} to={}", from, to);
            return false;
        }
        if (!validTo.contains(to)) {
            log.debug("a=stateChange r=FromProhibitsTo from={} to={}", from, to);
            return false;
        }

        log.debug("a=stateChange r=valid from={} to={}", from, to);
        return true;
    }
}
