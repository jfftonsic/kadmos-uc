package com.example.test;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class TestReport {

    Map<String, Long> longFeature = new HashMap<>();
    Map<String, String> stringFeature = new HashMap<>();
    StopWatch stopWatch = new StopWatch();

    public void startStopWatch(){
        stopWatch.start();
    }

    public void stopStopWatch(){
        stopWatch.stop();
    }

    public long getTotalTimeNanosStopWatch(){
        return stopWatch.getTotalTimeNanos();
    }

    public void incrementLongFeature(String feature) {
        // need to see if this will not consume too much from the test execution itself.
        final var v = longFeature.computeIfAbsent(feature, x -> 0L);
        longFeature.put(feature, v + 1);
    }

    public void setLongFeature(String feature, long v) {
        longFeature.put(feature, v);
    }

    public void setStringFeature(String feature, String v) {
        stringFeature.put(feature, v);
    }
}
