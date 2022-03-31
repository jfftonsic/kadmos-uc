package com.example.test;

import java.util.List;
import java.util.function.Function;

/**
 * Repeats the execution n times for each input object.
 */
public class TestMaster {

    public <T extends TestInput> AllAggregatedTestReport run(
            TestTask testTask,
            List<T> inputs,
            AllAggregatedTestReport aggregatedTestReport,
            Function<T, OneInputAggregatedTestReport> oneInputAggregatedTestReportSupplier
    ) {
        for (T input : inputs) {
            final var oneInputAggregatedTestReport = oneInputAggregatedTestReportSupplier.apply(input);
            for (int i = 0; i < input.getNumberOfRepetitions(); i++) {
                final var report = testTask.run(input);
                oneInputAggregatedTestReport.aggregate(report);
            }
            oneInputAggregatedTestReport.afterAggregations();
            aggregatedTestReport.aggregate(oneInputAggregatedTestReport);
        }
        return aggregatedTestReport;
    }
}
