package com.example.business;

public class AccountFactories {
    public static class Af implements MyExtension.AccountFactory<BalanceObjects.MtTransferenceStrategy01Balance> {

        @Override
        public BalanceObjects.MtTransferenceStrategy01Balance build(int idx, int size, int initialBalance) {
            return new BalanceObjects.MtTransferenceStrategy01Balance(initialBalance);
        }
    }

    public static class Af2 implements MyExtension.AccountFactory<BalanceObjects.MtTransferenceStrategy02Balance> {

        @Override
        public BalanceObjects.MtTransferenceStrategy02Balance build(int idx, int size, int initialBalance) {
            return new BalanceObjects.MtTransferenceStrategy02Balance(initialBalance);
        }
    }

    public static class Af3 implements MyExtension.AccountFactory<BalanceObjects.MtTransferenceStrategy03Balance> {

        @Override
        public BalanceObjects.MtTransferenceStrategy03Balance build(int idx, int size, int initialBalance) {
            return new BalanceObjects.MtTransferenceStrategy03Balance(initialBalance);
        }
    }

    public static class AfLockable implements MyExtension.AccountFactory<BalanceObjects.LockableBalance> {

        @Override
        public BalanceObjects.LockableBalance build(int idx, int size, int initialBalance) {
            return new BalanceObjects.LockableBalance(initialBalance);
        }
    }
}
