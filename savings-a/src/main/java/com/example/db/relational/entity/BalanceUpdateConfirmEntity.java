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
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = BalanceUpdateConfirmEntity.ENTITY_NAME_BALANCE)
@Table(name = BalanceUpdateConfirmEntity.TABLE_NAME_BALANCE)
@Getter @Setter @RequiredArgsConstructor @SuperBuilder
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
