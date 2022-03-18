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
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity(name = TransferenceCreditRequestEntity.ENTITY_NAME_BALANCE)
@Table(name = TransferenceCreditRequestEntity.TABLE_NAME_BALANCE)
@Getter @Setter @RequiredArgsConstructor @SuperBuilder
public class TransferenceCreditRequestEntity {

    public static final String TABLE_NAME_BALANCE = "transference_credit_request";
    public static final String ENTITY_NAME_BALANCE = "transferenceCreditRequest";

    private static final String COL_ID = "id";
    private static final String COL_IDEM_CODE = "req_idem_code";
    private static final String COL_IDEM_ACTOR = "req_idem_actor";
    private static final String COL_TRANSFERENCE_ID = "transference_id";

    @Id
    @Column(name = COL_ID, nullable = false)
    final private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = COL_TRANSFERENCE_ID, nullable = false)
    private TransferenceEntity transferenceEntity;

    @Column(name = COL_IDEM_CODE, nullable = false)
    private BigDecimal idempotencyCode;

    @Column(name = COL_IDEM_ACTOR, nullable = false)
    private BigDecimal idempotencyActor;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        TransferenceCreditRequestEntity that = (TransferenceCreditRequestEntity) o;
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
                "idempotencyCode = " + idempotencyCode + ", " +
                "idempotencyActor = " + idempotencyActor + ")";
    }
}
