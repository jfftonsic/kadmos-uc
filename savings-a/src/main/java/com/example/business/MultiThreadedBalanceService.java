package com.example.business;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * There will be multiple instances of this class being called concurrently, and the MultiThreadedBalance's will be
 * shared instances.
 *
 * I'm implementing this 'feature' in multiple ways, including inappropriate ways, to make visible differences in behavior
 * given the approaches.
 *
 * There are some properties that can be measured:
 * - sum of all balances before initiating transferences is equal to after doing all transfers
 */
public class MultiThreadedBalanceService {

    interface TransferResultMarker{}

    /**
     * In this method, there is no attempt to avoid negative balances from appearing.
     *
     * In this case the following were observed:
     * - about the property: "sum of all balances before initiating transferences is equal to after doing all transfers."
     *      - AtomicXXX for balance values were enough to guarantee this property
     *      - Primitive values, volatile or not, were not enough
     *
     */
    public TransferResultMarker transferAllowingEverything(MultiThreadedBalance source, MultiThreadedBalance destination, long amount) {
        var sourceResults = update(source, -amount);
        var destinationResults = update(destination, amount);
        var newSourceBalanceDifferentFromExpected =  sourceResults[0] - amount != sourceResults[1];
        var newDestinationBalanceDifferentFromExpected =  destinationResults[0] + amount != destinationResults[1];
        return new TransferAllowingEverythingResult(newSourceBalanceDifferentFromExpected, newDestinationBalanceDifferentFromExpected);
    }
    record TransferAllowingEverythingResult(
            boolean newSourceBalanceDifferentFromExpected
            , boolean newDestinationBalanceDifferentFromExpected) implements TransferResultMarker {}

    /**
     * PS: This method is intentionally incorrect!
     *
     * In this method, negative resulting balances are attempted to be avoided in a NAIVE way.
     *
     * In this case the following were observed:
     *
     * - about the property: "sum of all balances before initiating transferences is equal to after doing all tranfers."
     *      - AtomicXXX for balance values were enough to guarantee this property
     *      - Primitive values, volatile or not, were not enough
     *
     * - about the property: "at no point in time a balance was negative."
     *      - AtomicXXX or Primitive values, volatile or not, were not enough.
     */
    public TransferResultMarker transferNaiveBlockingNegativeBalances(MultiThreadedBalance source, MultiThreadedBalance destination, long amount) {

        if (source.get() - amount >= 0) {
            var sourceResults = update(source, -amount);
            var destinationResults = update(destination, amount);

            var newSourceBalanceDifferentFromExpected =  sourceResults[0] - amount != sourceResults[1];
            var newDestinationBalanceDifferentFromExpected =  destinationResults[0] + amount != destinationResults[1];

            if (sourceResults[1] < 0) {
                return new TransferBlockingNegativeBalancesResult(true, newSourceBalanceDifferentFromExpected, newDestinationBalanceDifferentFromExpected);
            }
        }

        return new TransferBlockingNegativeBalancesResult(false, false, false);

    }
    record TransferBlockingNegativeBalancesResult(boolean negativeNewSource
            , boolean newSourceBalanceDifferentFromExpected
            , boolean newDestinationBalanceDifferentFromExpected) implements TransferResultMarker {}



    public TransferResultMarker transferBlockingNegativeBalances(final MultiThreadedBalance source, MultiThreadedBalance destination, long amount) {

        if (source.get() - amount >= 0) {

            long[] sourceResults = null;

            synchronized (source) {
                if (source.get() - amount >= 0) {
                    sourceResults = update(source, -amount);
                }
            }

            if (sourceResults != null) {
                var destinationResults = update(destination, amount);

                var newSourceBalanceDifferentFromExpected = sourceResults[0] - amount != sourceResults[1];
                var newDestinationBalanceDifferentFromExpected =
                        destinationResults[0] + amount != destinationResults[1];

                if (sourceResults[1] < 0) {
                    return new TransferBlockingNegativeBalancesResult(
                            true,
                            newSourceBalanceDifferentFromExpected,
                            newDestinationBalanceDifferentFromExpected
                    );
                }
            }
        }

        return new TransferBlockingNegativeBalancesResult(
                false,
                false,
                false
        );

    }





    private long[] update(MultiThreadedBalance source, long amount) {
        final var applyDeltaResult = source.applyDelta(amount);

//        if (applyDeltaResult[1] - applyDeltaResult[0] != amount) {
//            System.out.printf("expected=%d actual=%d %n",
//                    amount,
//                    applyDeltaResult[1] - applyDeltaResult[0]);
//        }
        return applyDeltaResult;
    }
}

interface MultiThreadedBalance {
    long get();
    void set(long amount);
    long[] applyDelta(long amount);
}

class MultiThreadedAtomicBalance implements MultiThreadedBalance{
    AtomicLong balance = new AtomicLong();

    @Override
    public long get() {
        return balance.get();
    }

    @Override
    public void set(long amount) {
        balance.set(amount);
    }

    @Override
    public long[] applyDelta(long amount) {
        final var guaranteedBeforeAdd = balance.getAndAdd(amount);
        final var guaranteedAfterAdd = guaranteedBeforeAdd + amount;
        return new long[] {guaranteedBeforeAdd, guaranteedAfterAdd};
    }
}

class MultiThreadedPrimitiveBalance implements MultiThreadedBalance{
    long balance;

    @Override
    public long get() {
        return balance;
    }

    @Override
    public void set(long amount) {
        balance = amount;
    }

    @Override
    public long[] applyDelta(long amount) {
        var balanceOneLineBefore = balance;
        balance += amount;
        var balanceOneLineLater = balance;

        return new long[]{balanceOneLineBefore, balanceOneLineLater};
    }
}

class MultiThreadedPrimitiveSynchronizedApplyDeltaBalance implements MultiThreadedBalance {
    long balance;

    @Override
    public long get() {
        return balance;
    }

    @Override
    public void set(long amount) {
        balance = amount;
    }

    @Override
    synchronized public long[] applyDelta(long amount) {
        var balanceOneLineBefore = balance;
        balance += amount;
        var balanceOneLineLater = balance;

        return new long[]{balanceOneLineBefore, balanceOneLineLater};
    }
}

class MultiThreadedPrimitiveSelectForUpdateBalance implements MultiThreadedBalance {
    long balance;
    ReentrantLock lock = new ReentrantLock(true);

    @Override
    public long get() {
        lock.lock();
        return balance;
    }

    @Override
    public void set(long amount) {
        balance = amount;
    }

    @Override
    public long[] applyDelta(long amount) {
        try {
            var balanceOneLineBefore = balance;
            balance += amount;
            var balanceOneLineLater = balance;

            return new long[]{balanceOneLineBefore, balanceOneLineLater};
        } finally {
            lock.unlock();
        }
    }
}

class MultiThreadedVolatilePrimitiveBalance implements MultiThreadedBalance{
    volatile long balance;

    @Override
    public long get() {
        return balance;
    }

    @Override
    public void set(long amount) {
        balance = amount;
    }

    @Override
    public long[] applyDelta(long amount) {
        var balanceOneLineBefore = balance;
        balance += amount;
        var balanceOneLineLater = balance;

        return new long[]{balanceOneLineBefore, balanceOneLineLater};
    }
}