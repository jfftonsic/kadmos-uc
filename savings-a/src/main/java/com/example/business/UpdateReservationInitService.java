package com.example.business;

import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateReservationInitService {

    final BalanceUpdateReservationRepository balanceUpdateReservationRepository;

    /**
     * Creates and commits an initial record of the update reservation.
     * Block calls incoming from methods that have open transactions so as to not give the idea that the insert will
     * be reverted if the transaction gets rolled back.
     */
    @SneakyThrows
    @Transactional(
            propagation = Propagation.NEVER
    )
    public UUID initUpdateReservation(String idemCode, String idemActor, ZonedDateTime requestTimestamp,
            BigDecimal amount) {
        log.info("");

        final var reservationCode = UUID.randomUUID();

        BalanceUpdateReservationEntity balanceUpdateReservationEntity = BalanceUpdateReservationEntity.builder()
                .amount(amount)
                .reservationCode(reservationCode)
                .idempotencyCode(idemCode)
                .idempotencyActor(idemActor)
                .requestTimestamp(requestTimestamp)
                .status(BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.RECEIVED.getDbValue())
                .build();

        log.info("idemCode={} - Saving BalanceUpdateReservationEntity ...", idemCode);
        balanceUpdateReservationRepository.save(balanceUpdateReservationEntity);

        return reservationCode;
    }
}
