package com.example;

import com.example.db.relational.entity.BalanceEntity;
import com.example.db.relational.repository.BalanceRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.LinkedList;

@ActiveProfiles("integrationTest")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryTest {
    @Autowired
    private BalanceRepository repository;

    @Test
    void findAll() {
        var owners = repository.findAll();
        final var balanceEntities = new LinkedList<BalanceEntity>();
        owners.iterator().forEachRemaining(balanceEntities::add);
        assertThat(balanceEntities.size()).isOne();
        assertThat(balanceEntities.get(0).getTotalAmount()).isEqualTo(new BigDecimal("0.0"));
    }
}
