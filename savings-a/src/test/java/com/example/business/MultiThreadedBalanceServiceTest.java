package com.example.business;

import com.example.test.AllAggregatedTestReport;
import com.example.test.OneInputAggregatedTestReport;
import com.example.test.TestInput;
import com.example.test.TestMaster;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MultiThreadedBalanceServiceTest {

    @Test
    void transferMulti() throws InterruptedException {

        final var businessTestTask = new BusinessTestTask();

        final List<TestInput> inputsList = Stream.of(
//                MultiThreadedPrimitiveBalance.class
//                , MultiThreadedAtomicBalance.class
//                , MultiThreadedPrimitiveSynchronizedApplyDeltaBalance.class
//                , MultiThreadedVolatilePrimitiveBalance.class
                 MultiThreadedPrimitiveSelectForUpdateBalance.class
        ).map(clazz -> new ExecutionInput(
                clazz.getSimpleName(),
                5,
                1000,
                2000,
                50,
                100,
                5,
                1_000_000,
                8,
                balanceAmount -> {
                    final MultiThreadedBalance obj;
                    try {
                        obj = clazz.getDeclaredConstructor().newInstance();
                        obj.set(balanceAmount);
                        return obj;
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
        ).collect(Collectors.toList());

        final var allAggregatedTestReport = new Y();

        final var testMaster = new TestMaster();
        final var allAggregatedTestReport1 = testMaster.run(
                businessTestTask,
                inputsList,
                allAggregatedTestReport,
                input -> new X((ExecutionInput) input)
        );

        allAggregatedTestReport.stdoutDisplayReport();

        System.out.println();
    }

    public static class Y extends AllAggregatedTestReport<X> {
        Map<String, X> m = new HashMap<>();

        @Override
        public void aggregate(X testReport) {
            m.put(testReport.getInput().getDescription(), testReport);
        }

        @Override
        public void stdoutDisplayReport() {
            System.out.println("Final veredict:");
            m.forEach((key, value) -> System.out.println(
                    key + ": " + (long) (value.longSummaryStatistics.getAverage() / 1000000.0)));
        }
    }

    @Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class X extends OneInputAggregatedTestReport<SingleRunReport, ExecutionInput> {
        List<Long> executionTimesNano = new LinkedList<>();
        LongSummaryStatistics longSummaryStatistics;
        long neg;
        long un1;
        long un2;


        public X(ExecutionInput input) {
            super(input);
        }

        @Override
        public void aggregate(SingleRunReport testReport) {
            System.out.printf("%s millis execution times: %s%n",
                    getInput().getDescription(),
            testReport.getTotalTimeNanosStopWatch()/1000000);
            executionTimesNano.add(testReport.getTotalTimeNanosStopWatch());

            neg += testReport.getNegativeBalanceOccurrences().get();
            un1 += testReport.getUnexpectedNewSourceOccurrences().get();
            un2 += testReport.getUnexpectedNewDestinationOccurrences().get();
        }

        @Override
        public void afterAggregations() {
            longSummaryStatistics = executionTimesNano.stream().mapToLong(x -> x).summaryStatistics();
            System.out.printf("%s millis execution times: %s%nStatistics: %s%nneg=%d un1=%d un2=%d%n",
                    getInput().getDescription(),
                    executionTimesNano.stream().map(x -> x / 1000000).toList().toString(),
                    longSummaryStatistics.toString(),
                    neg, un1, un2);
        }
    }
}