package com.example.business;

import com.example.test.FastRand;
import com.example.test.TestTask;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BusinessTestTask extends TestTask<ExecutionInput, SingleRunReport> {

    @Override
    public SingleRunReport run(ExecutionInput input) {

        final var executionState = new ExecutionState(input);

        final var singleRunReport = executionState.getReport();

        final var fastRand = new FastRand(FastRand.GenerationMethod.APPROACH_3,
                0,
                input.getMultiThreadedBalancePoolSize(),
                input.getTransferenceAmountBetweenStart(),
                input.getTransferenceAmountBetweenEnd());

        IntStream.range(0, input.getCorePoolSize()).forEach(i -> {
            executionState.getThreadPool().schedule(
                    () -> {
                        // I've seen through profiler that the random number generation was taking most of the time of processing
                        // So I'm gonna generate everything before starting the actual thing that I want to measure.
                        //                System.out.printf("Thread %d generating random numbers %n", Thread.currentThread().getId());
                        //                System.out.printf("Thread %d generated random numbers. Now waiting at the barrier. %n", Thread.currentThread().getId());

                        final var singleThreadReport = new SingleThreadReport();

                        try {
                            // sync every thread
                            executionState.getBarrier().await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }

                        final MultiThreadedBalanceService balanceService = new MultiThreadedBalanceService();

                        for (int transferIdx = 0; transferIdx < input.getNumberOfTransfersByTask(); transferIdx++) {

                            final var sourceIdx = fastRand.nextInt();
                            final var source = executionState.getMultiThreadedBalancePool()[sourceIdx];

                            int destinationIdx;
                            do {
                                destinationIdx = fastRand.nextInt();
                            } while (destinationIdx == sourceIdx);

                            final var destination = executionState.getMultiThreadedBalancePool()[destinationIdx];

                            final var amount = fastRand.nextLong();

                            //                            System.out.printf("START Thread %d is on task %d, sourceIdx=%d destinationIdx=%d %n",
                            //                                    Thread.currentThread().getId(),
                            //                                    value,
                            //                                    sourceIdx,
                            //                                    destinationIdx);

                            var transferResult = balanceService
                                    .transferBlockingNegativeBalances(
                                            source,
                                            destination,
                                            amount
                                    );

                            if (MultiThreadedBalanceService.TransferBlockingNegativeBalancesResult.class.isAssignableFrom(
                                    transferResult.getClass())) {

                                MultiThreadedBalanceService.TransferBlockingNegativeBalancesResult castResult = (MultiThreadedBalanceService.TransferBlockingNegativeBalancesResult) transferResult;
                                if (castResult.negativeNewSource())
                                    singleThreadReport.incrementNegativeBalanceOccurrences();
                                if (castResult.newSourceBalanceDifferentFromExpected())
                                    singleThreadReport.incrementUnexpectedNewSourceOccurrences();
                                if (castResult.newDestinationBalanceDifferentFromExpected())
                                    singleThreadReport.incrementUnexpectedNewDestinationOccurrences();

                            } else if (MultiThreadedBalanceService.TransferAllowingEverythingResult.class.isAssignableFrom(
                                    transferResult.getClass())) {
                                MultiThreadedBalanceService.TransferAllowingEverythingResult castResult = (MultiThreadedBalanceService.TransferAllowingEverythingResult) transferResult;
                                if (castResult.newSourceBalanceDifferentFromExpected())
                                    singleThreadReport.incrementUnexpectedNewSourceOccurrences();
                                if (castResult.newDestinationBalanceDifferentFromExpected())
                                    singleThreadReport.incrementUnexpectedNewDestinationOccurrences();
                            }

                            //                            System.out.printf("END   Thread %d is on task %d, sourceIdx=%d destinationIdx=%d %n",
                            //                                    Thread.currentThread().getId(),
                            //                                    value,
                            //                                    sourceIdx,
                            //                                    destinationIdx);
                        }

                        singleRunReport.setLongFeature("NegativeBalanceOccurrences",
                                singleThreadReport.getNegativeBalanceOccurrences());
                        singleRunReport.setLongFeature("UnexpectedNewSourceOccurrences",
                                singleThreadReport.getUnexpectedNewSourceOccurrences());
                        singleRunReport.setLongFeature("UnexpectedNewDestinationOccurrences",
                                singleThreadReport.getUnexpectedNewDestinationOccurrences());



                    }, 0L, TimeUnit.SECONDS);
        });

        try {
            executionState.getBarrier().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        //        System.out.printf("Barrier breached. %n");

        singleRunReport.startStopWatch();

        executionState.getThreadPool().shutdown();
        try {
            executionState.getThreadPool().awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        singleRunReport.stopStopWatch();

        final var newWorldBalance = (Long) Arrays.stream(executionState.getMultiThreadedBalancePool())
                .mapToLong(MultiThreadedBalance::get)
                .sum();

        if (newWorldBalance.equals(executionState.getWorldBalance())) {
            singleRunReport.setStringFeature("WorldBalance", "New == Starting");
        } else {
            singleRunReport.setStringFeature("WorldBalance",
                    "Inconsistent. New=%d Starting=%d".formatted(newWorldBalance, executionState.getWorldBalance()));
        }

        final var collect = Arrays.stream(executionState.getMultiThreadedBalancePool())
                .filter(x -> x.get() < 0).toList();

        singleRunReport.setLongFeature("NegativeBalances", collect.size());

        return singleRunReport;
    }
}
