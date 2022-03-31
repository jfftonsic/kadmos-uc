package com.example.business;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter @FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleThreadReport {
    long negativeBalanceOccurrences;
    long unexpectedNewSourceOccurrences;
    long unexpectedNewDestinationOccurrences;

    public void incrementNegativeBalanceOccurrences() {
        negativeBalanceOccurrences++;
    }

    public void incrementUnexpectedNewSourceOccurrences() {
        unexpectedNewSourceOccurrences++;
    }

    public void incrementUnexpectedNewDestinationOccurrences() {
        unexpectedNewDestinationOccurrences++;
    }
}
