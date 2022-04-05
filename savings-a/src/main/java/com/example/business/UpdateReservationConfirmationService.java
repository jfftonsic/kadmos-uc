package com.example.business;

import com.example.business.api.IBalanceService.ConfirmUpdateReservationResponse;
import com.example.db.relational.entity.BalanceUpdateConfirmEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import static com.example.db.relational.entity.BalanceUpdateReservationEntity.BalanceUpdateReservationStatus.CONFIRMED;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateConfirmRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import com.example.util.GeneralConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateReservationConfirmationService {//implements SelfReferential<UpdateReservationConfirmationService> {

    //    public UpdateReservationConfirmationService getSelf() {
    //        return internalGetSelf(context, self);
    //    }
    //    @Setter
    UpdateReservationConfirmationService self;

    // TODO: check 3 things
    // 1 - inside this same class, calling a public method B from another public method A bypasses transactional configuration of method B
    // 2 - making the same call using a reference to itself (by using application context's get bean) does NOT bypass transactional configuration of method B
    // 3 - making the same call using a reference to itself (by using PostConstruct self=this) does NOT bypass transactional configuration of method B
    @PostConstruct
    public void postConstruct() {
        self = this;
    }

    final ApplicationContext context;

    final BalanceRepository balanceRepository;
    final BalanceUpdateReservationRepository balanceUpdateReservationRepository;
    final BalanceUpdateConfirmRepository balanceUpdateConfirmRepository;

    /**
     * Creates and commits an initial record of the entity. Block calls incoming from methods that have open
     * transactions so as to not give the idea that the insert will be reverted if the transaction gets rolled back.
     */
    @Transactional(
            propagation = Propagation.NEVER
    )
    public UUID initConfirmation(BalanceUpdateReservationEntity updateReservation, ZonedDateTime requestTimestamp) {
        final BalanceUpdateConfirmEntity confirmation = BalanceUpdateConfirmEntity.builder()
                .balanceUpdateReservationEntity(updateReservation)
                .requestTimestamp(requestTimestamp)
                .done(false)
                .build();
        balanceUpdateConfirmRepository.save(confirmation);
        return confirmation.getId();
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = GeneralConstants.TIMEOUT_S
    )
    public ConfirmUpdateReservationResponse confirmUpdateReservation(
            UUID updateReservationCode,
            UUID confirmationId
    ) {

        final var b = balanceUpdateConfirmRepository.existsDone(updateReservationCode);
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@ {}", b);

        final var aBoolean = balanceUpdateConfirmRepository.couldBeWhicheverNameIWanted();
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@ {}", aBoolean);

        final var list = balanceUpdateConfirmRepository
                .findPessimisticByBalanceUpdateReservationEntityReservationCode(updateReservationCode);



        // if none of the potentially multiple records is marked as done, then proceed.
        if (list.stream().noneMatch(BalanceUpdateConfirmEntity::getDone)) {

            final var confirmOpt = list.stream().filter(en -> en.getId().equals(confirmationId)).findFirst();
            if (confirmOpt.isEmpty()) {
                return ConfirmUpdateReservationResponse.CONFIRMATION_NOT_FOUND;
            }

            final var confirm = confirmOpt.get();
            confirm.setDone(true);

            final var amount = confirmUpdateReservationAndReturnTheAmount(updateReservationCode);

        } else {
            return ConfirmUpdateReservationResponse.NO_CHANGES;
        }

        //        final var updateReservationOpt = balanceUpdateReservationRepository.findAndPessimisticWriteLockByReservationCode(
        //                updateReservationCode);
//
//        if (updateReservationOpt.isPresent()) {
//            final var updateReservation = updateReservationOpt.get();
//
//            updateReservation.setStatus(CONFIRMED.getDbValue());
//            updateConfirm.setDone(true);
//
//            log.info("################# {} getBalanceForUpdate pessimistically", updateReservationCode);
//            final var balanceForUpdate = balanceRepository.getBalanceForUpdate(Pageable.ofSize(1));
//            final var balanceEntity = balanceForUpdate.get(0);
//            if (updateReservation.getAmount().signum() < 0) {
//                balanceEntity.setOnHoldAmount(balanceEntity.getOnHoldAmount().add(updateReservation.getAmount()));
//
//            }
//            balanceEntity.setTotalAmount(balanceEntity.getTotalAmount().add(updateReservation.getAmount()));
//        }
//        log.info("################# {} returning", updateReservationCode);
        return ConfirmUpdateReservationResponse.DONE;
    }

    private BigDecimal confirmUpdateReservationAndReturnTheAmount(UUID updateReservationCode) {
        final var updateReservationOpt = balanceUpdateReservationRepository.findByReservationCode(
                updateReservationCode);
        final var updateReservation = updateReservationOpt.orElseThrow(
                () -> new IllegalArgumentException(
                        "Database inconsistency, missing an expected mandatory %s record. updateReservationCode=%s".formatted(
                                BalanceUpdateReservationEntity.class.getSimpleName(),
                                updateReservationCode))
        );
        updateReservation.setStatusEnum(CONFIRMED);
        return updateReservation.getAmount();
    }

}
