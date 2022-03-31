package com.example.business;

import com.example.business.FinancialEnvironment.Account;

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
