package com.example.business;

import com.example.business.MyExtension.MyExtensionOptions;
import com.example.business.multithreaded.FinancialEnvironment;
import com.example.business.multithreaded.MtTransferenceStrategy04;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MyExtension.class)
class MtTransferenceStrategy04Test {

    public static final int TRANSFERENCES_MADE_BY_EACH_ACCOUNT = 500_000;
    public static final int MONEY_IN_EACH_ACCOUNT = 100;

    final MtTransferenceStrategy04 transferenceService = new MtTransferenceStrategy04();

    XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom(System.currentTimeMillis());

    @Test
    @MyExtensionOptions(
            numberOfThreads = 4,
            threadPoolDescription = "transfer_lockable_rw",
            numberOfAccounts = 8,
            moneyInEachAccount = MONEY_IN_EACH_ACCOUNT,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.AfLockable.class
    )
    void transfer_lockable_rw(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) {
        doIt(myExtensionOptions, financialEnvironment, transferenceService, "transfer_lockable_rw");
    }

    @Test
    @MyExtensionOptions(
            numberOfThreads = 4,
            threadPoolDescription = "transfer_lockable",
            numberOfAccounts = 8,
            moneyInEachAccount = MONEY_IN_EACH_ACCOUNT,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.AfLockable2.class
    )
    void transfer_lockable(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) {
        doIt(myExtensionOptions, financialEnvironment, transferenceService, "transfer_lockable");
    }

    private void doIt(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment,
            MtTransferenceStrategy04 transferenceService, String msg) {
        final long amount = myExtensionOptions.amountTransferred();
        final int numberOfAccounts = myExtensionOptions.numberOfAccounts();

        long stats01 = 0L;
        for (int j = 0; j < myExtensionOptions.transferencesMadeByEachThread(); j++) {
            final int i1 = random.nextInt(0, numberOfAccounts);
            int i2 = random.nextInt(0, numberOfAccounts);
            i2 = (i2 + 1) % numberOfAccounts;

            final var source = financialEnvironment.getAccount(i1);
            final var destination = financialEnvironment.getAccount(i2);

            stats01 += transferenceService.transfer(source, destination, amount) < 0 ? 1 : 0;
        }
        System.out.printf("%s negative occasions=%d %n", msg, stats01);
    }

}