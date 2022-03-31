package com.example.business.multithreaded;

import com.example.business.multithreaded.FinancialEnvironment.Account;

/**
 * This strategy naively try to avoid negative balances.
 *
 * Volatile primitive longs for balances almost were able to make consistent the "don't let balances become negative".
 * But that may be because so much new money is created from thin air in the tests that it is rare the occasions where
 * one balance gets too low.
 * Apart from that, the final amount of money was wildly different from the initial.
 *
 * Primitive
 *
 * AtomicLong makes the final amount be equal to initial (no money created or disappeared).
 */
public class MtTransferenceStrategy02 {

    /**
     *
     *
     * @return 1 line later source balance get.
     */
    public long transfer(Account source,
            Account destination, long amount) {
        long sourceBalance = source.get();
        if (source.get() >= amount){
            source.applyDelta(-amount);
            sourceBalance = source.get();
            destination.applyDelta(amount);
        }
        return sourceBalance;
    }

}
