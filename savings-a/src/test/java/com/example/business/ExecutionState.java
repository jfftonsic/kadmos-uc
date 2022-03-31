package com.example.business;

import com.example.test.TestTaskState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecutionState extends TestTaskState<SingleRunReport> {
    long worldBalance;

    MultiThreadedBalance[] multiThreadedBalancePool;

    ScheduledExecutorService threadPool;

    CyclicBarrier barrier;

    public ExecutionState(ExecutionInput input) {
        super(new SingleRunReport());

        multiThreadedBalancePool = new MultiThreadedBalance[input.getMultiThreadedBalancePoolSize()];

        for (int i = 0; i < input.getMultiThreadedBalancePoolSize(); i++) {
            final var balanceAmount = RandomUtils.nextLong(
                    input.getBalancesBetweenStart(),
                    input.getBalancesBetweenEnd());

            multiThreadedBalancePool[i] = input.getBalanceInitializer().apply(balanceAmount);

            worldBalance += balanceAmount;
        }

        threadPool = Executors.newScheduledThreadPool(input.getCorePoolSize(), new MyThreadFactory(input.getDescription()));

        barrier = new CyclicBarrier(input.getCorePoolSize() + 1);
    }

}
