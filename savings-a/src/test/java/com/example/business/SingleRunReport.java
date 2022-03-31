package com.example.business;

import com.example.test.TestReport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicLong;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleRunReport extends TestReport {
    AtomicLong negativeBalanceOccurrences;
    AtomicLong unexpectedNewSourceOccurrences;
    AtomicLong unexpectedNewDestinationOccurrences;

    public SingleRunReport() {
        negativeBalanceOccurrences = new AtomicLong(0L);
        unexpectedNewSourceOccurrences = new AtomicLong(0L);
        unexpectedNewDestinationOccurrences = new AtomicLong(0L);
    }
}
