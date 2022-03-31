package com.example.business;

import com.example.business.BalanceObjects.LockableBalance;
import com.example.business.FinancialEnvironment.Account;

/**
 * This strategy try to avoid negative balances strictly.
 * The intention is that this strategy, independently of the data structure of the balance, completely avoids negative
 * balances and world balance inconsistencies.
 *
 * Assumptions:
 * - operations on balance object will never fail
 * - the attempt to lock will hold as long as it takes to lock successfully
 * - the data is kept in the memory of the application, so if the application aborts, there will be no inconsistencies
 * because there will be no data.
 */
public class MtTransferenceStrategy04 {

    /**
     *
     */
    public long transfer(Account source,
            Account destination, long amount) {

        final LockableBalance lockableSource = (LockableBalance) source;
        final LockableBalance lockableDestination = (LockableBalance) destination;

        long sourceBalance;
        boolean debitMade = false;

        final var writeLock = lockableSource.getLock().writeLock();
        writeLock.lock();
        try {
            sourceBalance = source.get();
            if (source.get() >= amount){
                source.applyDelta(-amount);
                sourceBalance = source.get();
                debitMade = true;
            }
        } finally {
            writeLock.unlock();

            if (debitMade) {
                final var writeLock2 = lockableDestination.getLock().writeLock();
                writeLock2.lock();
                try {
                    destination.applyDelta(amount);
                } finally {
                    writeLock2.unlock();
                }
            }
        }

        return sourceBalance;
    }

}
