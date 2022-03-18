package com.example.controller.dataobject;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransferenceRequest(ZonedDateTime zonedDateTime, Idempotency idempotency, String sourceBalance,
                                  String destinationBalance, BigDecimal amount) {
}
