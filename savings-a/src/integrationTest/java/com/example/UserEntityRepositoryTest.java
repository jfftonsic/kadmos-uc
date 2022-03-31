package com.example;

import com.example.db.relational.entity.BalanceUpdateConfirmEntity;
import com.example.db.relational.entity.BalanceUpdateReservationEntity;
import com.example.db.relational.repository.BalanceRepository;
import com.example.db.relational.repository.BalanceUpdateConfirmRepository;
import com.example.db.relational.repository.BalanceUpdateReservationRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@ActiveProfiles("integrationTest")
@DataJpaTest
class UserEntityRepositoryTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private BalanceUpdateReservationRepository balanceUpdateReservationRepository;
    @Autowired
    BalanceUpdateConfirmRepository balanceUpdateConfirmRepository;

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(entityManager).isNotNull();
        assertThat(balanceRepository).isNotNull();
    }

    @Test
    @Transactional
    void tempTest() {

        final var reservationCode = UUID.randomUUID();
        final var idemCode = UUID.randomUUID().toString();
        final var idemActor = "actor";
        BalanceUpdateReservationEntity updateReservationEntity = BalanceUpdateReservationEntity.builder()
                .reservationCode(reservationCode)
                .amount(BigDecimal.TEN)
                .requestTimestamp(
                        ZonedDateTime.now())
                .idempotencyCode(idemCode)
                .idempotencyActor(idemActor)
                .status(0)
                .build();
        updateReservationEntity = balanceUpdateReservationRepository.save(updateReservationEntity);

        BalanceUpdateConfirmEntity updateConfirm = BalanceUpdateConfirmEntity.builder()
                .balanceUpdateReservationEntity(updateReservationEntity
                )
                .requestTimestamp(ZonedDateTime.now())
                .done(false)
                .build();
        updateConfirm = balanceUpdateConfirmRepository.save(updateConfirm);

        final var namedQuery = entityManager.createQuery(
                        """
                                SELECT b, c
                                FROM balanceUpdateReservation b, balanceUpdateConfirm c
                                WHERE b.reservationCode = :reservationCode AND c.balanceUpdateReservationEntity.id = b.id
                                """)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);

        namedQuery.setParameter("reservationCode", reservationCode);

        final var resultList = namedQuery.getResultList();

        System.out.println();

    }
}
