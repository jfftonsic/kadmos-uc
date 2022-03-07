package com.example.controller.dataobject;

import com.example.serializer.MoneySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public record BalanceResponse(
        /*@JsonSerialize(using = MoneySerializer.class)*/ BigDecimal amount
) {
}
