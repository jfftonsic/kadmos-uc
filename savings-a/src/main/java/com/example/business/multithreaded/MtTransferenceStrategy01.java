package com.example.business.multithreaded;

import com.example.business.multithreaded.FinancialEnvironment.Account;

/**
 * This strategy doesn't try to avoid negative balances
 */
public class MtTransferenceStrategy01 {

    public void transfer(Account source,
            Account destination, long amount) {
        source.applyDelta(-amount);
        destination.applyDelta(amount);
    }

}
