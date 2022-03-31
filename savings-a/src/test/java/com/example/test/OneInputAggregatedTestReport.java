package com.example.test;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OneInputAggregatedTestReport<TR extends TestReport, TI extends TestInput> {

    TI input;

    public OneInputAggregatedTestReport(TI input) {
        this.input = input;
    }

    /**
     * After each repetition, this will be called.
     * @param testReport report for one repetition
     */
    public void aggregate(TR testReport) {
    }

    /**
     * Just a callback that you can overwrite if you want to do something after the repeated executions for one
     * input object (e.g. print something)
     */
    public void afterAggregations() {

    }
}
