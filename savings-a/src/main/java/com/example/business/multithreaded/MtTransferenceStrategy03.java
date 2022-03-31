package com.example.business.multithreaded;

import com.example.business.multithreaded.FinancialEnvironment.Account;

/**
 * This strategy try to avoid negative balances strictly.
 * The intention is that this strategy, independently of the data structure of the balance, completely avoids negative
 * balances and world balance inconsistencies.
 */
public class MtTransferenceStrategy03 {

    /**
     *
     */
    public long transfer(Account source,
            Account destination, long amount) {

        long sourceBalance;
        boolean debitMade = false;
        synchronized (source) {
            sourceBalance = source.get();
            if (source.get() >= amount){
                source.applyDelta(-amount);
                sourceBalance = source.get();

                debitMade = true;
            }
        }

        if (debitMade) {
            synchronized (destination) {
                destination.applyDelta(amount);
            }
        }

        return sourceBalance;
    }

}
