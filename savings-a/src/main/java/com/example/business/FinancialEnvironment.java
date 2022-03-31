package com.example.business;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a holder of financial accounts.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialEnvironment {

    final List<Account> accounts;

    public FinancialEnvironment(int numberOfAccounts, AccountFactoryFunction accountFactoryFunction) throws Exception {
        accounts = new ArrayList<>(numberOfAccounts);
        for (int i = 0; i < numberOfAccounts; i++) {
            accounts.add(accountFactoryFunction.build(i, numberOfAccounts));
        }
    }

    public Account getAccount(int i) {
        return accounts.get(i);
    }

    public long getTotalSumThreadSafe() {
        synchronized (accounts) {
            return accounts.stream().mapToLong(Account::get).sum();
        }
    }

    public long[] accountBalances() {
        return accounts.stream().mapToLong(Account::get).toArray();
    }

    @FunctionalInterface
    public interface AccountFactoryFunction {
        Account build(int idx, int size) throws Exception;
    }

    public static abstract class Account {
        public abstract long get();

        public abstract void set(long amount);

        public abstract void applyDelta(long amount);
    }
}
