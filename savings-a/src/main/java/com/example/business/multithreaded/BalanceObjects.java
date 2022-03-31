package com.example.business.multithreaded;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BalanceObjects {

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static non-sealed class LockableBalance extends FinancialEnvironment.Account implements LockableAccount {
        final ReentrantReadWriteLock reentrantReadWriteLock;
        long balance;

        public LockableBalance(long balance) {
            this.balance = balance;
            reentrantReadWriteLock = new ReentrantReadWriteLock(true);
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

        @Override
        public <LOCK> LOCK getLock(Class<LOCK> castAs) {
            return castAs.cast(reentrantReadWriteLock);
        }
    }

    public sealed interface LockableAccount permits LockableBalance, LockableBalance2{
        <LOCK> LOCK getLock(Class<LOCK> castAs);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static non-sealed class LockableBalance2 extends FinancialEnvironment.Account implements LockableAccount {
        final ReentrantLock lock;
        long balance;

        public LockableBalance2(long balance) {
            this.balance = balance;
            lock = new ReentrantLock(true);
        }

        public <LOCK> LOCK getLock(Class<LOCK> castAs) {
            return castAs.cast(lock);
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
