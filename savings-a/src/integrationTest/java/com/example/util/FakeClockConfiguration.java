package com.example.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * This configuration replaces the production clock with a fixed test clock.
 * The objective is that date-time generation in the module happens in a predictable way.
 */
@Configuration
public class FakeClockConfiguration {

    public static final Clock FAKE_CLOCK = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

    @Bean
    @Primary
    public Clock testClock() {
        return FAKE_CLOCK;
    }
}
