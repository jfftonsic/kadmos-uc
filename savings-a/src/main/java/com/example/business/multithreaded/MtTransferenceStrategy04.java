package com.example.business.multithreaded;

import com.example.business.multithreaded.BalanceObjects.LockableAccount;
import com.example.business.multithreaded.BalanceObjects.LockableBalance;
import com.example.business.multithreaded.BalanceObjects.LockableBalance2;
import com.example.business.multithreaded.FinancialEnvironment.Account;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

        final LockableAccount lockableSource = (LockableAccount) source;
        final LockableAccount lockableDestination = (LockableAccount) destination;

        long sourceBalance;
        boolean debitMade = false;

        final Lock writeLock = getLock(lockableSource);

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
                final var writeLock2 = getLock(lockableDestination);
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

    private Lock getLock(LockableAccount lockableSource) {
        final Lock writeLock;
        if (LockableBalance.class.isAssignableFrom(lockableSource.getClass())) {
            var rrwl = lockableSource.getLock(ReentrantReadWriteLock.class);
            writeLock = rrwl.writeLock();
        } else if (LockableBalance2.class.isAssignableFrom(lockableSource.getClass())) {
            writeLock = lockableSource.getLock(Lock.class);
        } else {
            throw new RuntimeException("Missing implementation");
        }
        return writeLock;
    }

}
