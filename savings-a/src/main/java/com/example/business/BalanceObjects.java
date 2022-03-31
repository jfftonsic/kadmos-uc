package com.example.business;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BalanceObjects {

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LockableBalance extends FinancialEnvironment.Account {
        ReentrantReadWriteLock reentrantReadWriteLock;
        long balance;

        public LockableBalance(long balance) {
            this.balance = balance;
            reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        }

        public ReentrantReadWriteLock getLock() {
            return reentrantReadWriteLock;
        }

        public long get() {
            return balance;
        }

        public void set(long amount) {
            balance = amount;
        }

        public void applyDelta(long amount) {
            balance += amount;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MtTransferenceStrategy02Balance extends FinancialEnvironment.Account {
        volatile long balance;

        public MtTransferenceStrategy02Balance(long balance) {
            this.balance = balance;
        }

        public long get() {
            return balance;
        }

        public void set(long amount) {
            balance = amount;
        }

        public void applyDelta(long amount) {
            balance += amount;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MtTransferenceStrategy01Balance extends FinancialEnvironment.Account {
        long balance;

        public MtTransferenceStrategy01Balance(long balance) {
            this.balance = balance;
        }

        public long get() {
            return balance;
        }

        public void set(long amount) {
            balance = amount;
        }

        public void applyDelta(long amount) {
            balance += amount;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MtTransferenceStrategy03Balance extends FinancialEnvironment.Account {
        volatile AtomicLong balance;

        public MtTransferenceStrategy03Balance(long balance) {
            this.balance = new AtomicLong(balance);
        }

        public long get() {
            return balance.get();
        }

        public void set(long amount) {
            balance.set(amount);
        }

        public void applyDelta(long amount) {
            balance.addAndGet(amount);
        }
    }
}
