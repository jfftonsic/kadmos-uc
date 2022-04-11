package com.example.db.relational.entity;

import static com.example.db.relational.entity.BalanceEntity.ENTITY_NAME_BALANCE;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity(name = ENTITY_NAME_BALANCE)
@Table(name = BalanceEntity.TABLE_NAME_BALANCE)
@Getter @Setter @ToString @RequiredArgsConstructor @SuperBuilder
@NamedQueries({
        @NamedQuery(name = BalanceEntity.NAMED_QUERY_RESET_BALANCE, query = "UPDATE "+ENTITY_NAME_BALANCE+" SET "
                + BalanceEntity.COL_TOTAL_AMOUNT + " = 0, "
                + BalanceEntity.COL_ON_HOLD_AMOUNT + " = 0")
})
public class BalanceEntity {

    public static final String TABLE_NAME_BALANCE = "balance";
    public static final String ENTITY_NAME_BALANCE = "balance";
    public static final String COL_ID = "id";
    public static final String COL_TOTAL_AMOUNT = "total_amount";
    public static final String COL_ON_HOLD_AMOUNT = "on_hold_amount";
    public static final String NAMED_QUERY_RESET_BALANCE = "resetBalance";

    @Id
    @Column(name = COL_ID, nullable = false)
    final private UUID id = UUID.randomUUID();

    @Column(name = COL_TOTAL_AMOUNT, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = COL_ON_HOLD_AMOUNT, nullable = false)
    private BigDecimal onHoldAmount;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        BalanceEntity that = (BalanceEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
