package com.example;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This class exists for the sole purpose of mocking in JUnits. So that you have reproducible data when your flow needs
 * to generate a UUID. Everything that your main code generates that is dependent on environment or randomness you need
 * to make it mock-friendly.
 */
@Component
public class UUIDGenerator {

    public UUID randomUUID() {
        return UUID.randomUUID();
    }
}
