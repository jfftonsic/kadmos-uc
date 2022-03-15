package com.example.controller.dataobject;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record UpdateReservationPostRequest(ZonedDateTime timestamp, Idempotency idempotency, BigDecimal amount) {
}
