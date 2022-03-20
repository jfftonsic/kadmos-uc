package com.example.util;

import com.example.UUIDGenerator;
import com.example.controller.dataobject.Idempotency;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.ZonedDateTime;

public class SafeUtil {
    public static String safeIdempotencyCode(UUIDGenerator uuidGenerator, Idempotency idempotency) {
        return idempotency != null ?
                (StringUtils.isNotBlank(idempotency.code()) ?
                        idempotency.code() :
                        uuidGenerator.randomUUID().toString()) :
                uuidGenerator.randomUUID().toString();
    }

    public static ZonedDateTime safeTimestamp(Clock clock, ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? ZonedDateTime.now(clock) : zonedDateTime;
    }
}
