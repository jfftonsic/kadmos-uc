package com.example.business;

import com.example.test.TestInput;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecutionInput extends TestInput {

    long balancesBetweenStart;
    long balancesBetweenEnd;

    long transferenceAmountBetweenStart;
    long transferenceAmountBetweenEnd;

    int multiThreadedBalancePoolSize;

    int numberOfTransfersByTask;
    int corePoolSize;

    Function<Long, MultiThreadedBalance> balanceInitializer;

    public ExecutionInput(String description, int numberOfRepetitions, long balancesBetweenStart,
            long balancesBetweenEnd,
            long transferenceAmountBetweenStart, long transferenceAmountBetweenEnd, int multiThreadedBalancePoolSize,
            int numberOfTransfersByTask, int corePoolSize,
            Function<Long, MultiThreadedBalance> balanceInitializer) {
        super(description, numberOfRepetitions);
        this.balancesBetweenStart = balancesBetweenStart;
        this.balancesBetweenEnd = balancesBetweenEnd;
        this.transferenceAmountBetweenStart = transferenceAmountBetweenStart;
        this.transferenceAmountBetweenEnd = transferenceAmountBetweenEnd;
        this.multiThreadedBalancePoolSize = multiThreadedBalancePoolSize;
        this.numberOfTransfersByTask = numberOfTransfersByTask;
        this.corePoolSize = corePoolSize;
        this.balanceInitializer = balanceInitializer;
    }
}
