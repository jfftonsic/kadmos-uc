package com.example.business.api;

import java.math.BigDecimal;

public interface IBalanceService {

    BigDecimal fetchAmount();

    BigDecimal updateBalanceBy(BigDecimal amount);
}
