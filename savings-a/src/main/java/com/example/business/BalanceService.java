package com.example.business;

import com.example.business.api.IBalanceService;
import com.example.db.relational.repository.BalanceRepository;
import com.example.exception.service.NotEnoughBalanceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BalanceService implements IBalanceService {

    BalanceRepository balanceRepository;

    public <T> T validBalance(List<T> amounts) {
        if (amounts.isEmpty()) {
            throw new IllegalStateException("No balance on database.");
        } else if (amounts.size() > 1) {
            throw new IllegalStateException("Should exist only one balance.");
        } else {
            return amounts.get(0);
        }
    }

    @Override
    public BigDecimal fetchAmount() {
        return validBalance(balanceRepository.getBalanceAmount());
    }

    @Override
    @Transactional(timeout = 5000) // timeout in ms
    public BigDecimal updateBalanceBy(BigDecimal amount) {
        final var balanceAmountForUpdate = validBalance(balanceRepository.getBalanceForUpdate(Pageable.ofSize(1)));
        if (amount.signum() == -1 && amount.abs().compareTo(balanceAmountForUpdate.getTotalAmount()) > 0) {
            throw new NotEnoughBalanceException();
        }

        balanceAmountForUpdate.setTotalAmount(balanceAmountForUpdate.getTotalAmount().add(amount));

        return balanceAmountForUpdate.getTotalAmount();
    }

    @Override
    public void addFunds(BigDecimal amount) {
        if (balanceRepository.addFunds(amount) != 1) {
            throw new IllegalStateException("No row was updated while adding funds.");
        }
    }
}
