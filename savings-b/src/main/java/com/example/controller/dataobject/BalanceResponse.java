package com.example.controller.dataobject;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal amount
) {
}
