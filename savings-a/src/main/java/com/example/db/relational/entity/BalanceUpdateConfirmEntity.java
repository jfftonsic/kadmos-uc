package com.example.db.relational.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = BalanceUpdateConfirmEntity.ENTITY_NAME_BALANCE)
@Table(name = BalanceUpdateConfirmEntity.TABLE_NAME_BALANCE)
@Getter @Setter @RequiredArgsConstructor @SuperBuilder
@NamedNativeQueries(
        @NamedNativeQuery(
                // This query is defined as a NamedQuery just as an example of using named queries
                name = "existsDoneDoesNotNeedToBeSameNameAsMethod",
                query = """
                        SELECT CASE WHEN EXISTS (
                            SELECT 1
                            FROM balance_update_confirm buc
                            JOIN balance_update_reservation bur ON bur.id = buc.balance_update_reservation_id
                            WHERE 1=1
                                AND bur.reservation_code = :reservationCode
                                AND buc.done = true
                        ) THEN TRUE ELSE FALSE END
                        """
                        //"IF EXISTS (SELECT true FROM balanceUpdateConfirm c JOIN c.balanceUpdateReservationEntity b WHERE b.reservationCode = :reservationCode AND c.done IS TRUE LIMIT 1) SELECT 1 ELSE SELECT 0"
        )
)
public class BalanceUpdateConfirmEntity {

    public static final String TABLE_NAME_BALANCE = "balance_update_confirm";
    public static final String ENTITY_NAME_BALANCE = "balanceUpdateConfirm";
    private static final String COL_ID = "id";
    private static final String COL_REQUEST_TIMESTAMP = "request_timestamp";
    private static final String COL_BALANCE_UPDATE_RESERVATION_ID = "balance_update_reservation_id";
    private static final String COL_DONE = "done";

    @Id
    @Column(name = COL_ID, nullable = false)
    final private UUID id = UUID.randomUUID();

    @Column(name = COL_REQUEST_TIMESTAMP, nullable = false)
    private ZonedDateTime requestTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = COL_BALANCE_UPDATE_RESERVATION_ID, nullable = false)
    private BalanceUpdateReservationEntity balanceUpdateReservationEntity;

    @Column(name = COL_DONE, nullable = false)
    private Boolean done;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        BalanceUpdateConfirmEntity that = (BalanceUpdateConfirmEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "requestTimestamp = " + requestTimestamp + ", " +
                "done = " + done + ")";
    }

}
