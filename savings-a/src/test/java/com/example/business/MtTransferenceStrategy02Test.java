package com.example.business;

import com.example.business.MyExtension.MyExtensionOptions;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MyExtension.class)
class MtTransferenceStrategy02Test {

    public static final int TRANSFERENCES_MADE_BY_EACH_ACCOUNT = 10000000;
    public static final int MONEY_IN_EACH_ACCOUNT = 100;

    final MtTransferenceStrategy02 transferenceService = new MtTransferenceStrategy02();

    XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom(System.currentTimeMillis());

    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = MONEY_IN_EACH_ACCOUNT,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af.class
    )
    void transfer_primitive(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {
        doIt(myExtensionOptions, financialEnvironment, transferenceService, "transfer_primitive");
    }

    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = MONEY_IN_EACH_ACCOUNT,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af2.class
    )
    void transfer_volatile(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {

        doIt(myExtensionOptions, financialEnvironment, transferenceService, "transfer_volatile");
    }


    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = MONEY_IN_EACH_ACCOUNT,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af3.class
    )
    void transfer_atomic(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {

        doIt(myExtensionOptions, financialEnvironment, transferenceService, "transfer_atomic");
    }


    private void doIt(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment,
            MtTransferenceStrategy02 transferenceService, String msg) {
        final long amount = myExtensionOptions.amountTransferred();
        final int numberOfAccounts = myExtensionOptions.numberOfAccounts();

        long stats01 = 0L;
        for (int j = 0; j < myExtensionOptions.transferencesMadeByEachThread(); j++) {
            final int i1 = random.nextInt(0, numberOfAccounts);
            int i2 = random.nextInt(0, numberOfAccounts);
            i2 = (i2 + 1) % numberOfAccounts;

            final var source = financialEnvironment.getAccount(i1);
            final var destination = financialEnvironment.getAccount(i2);

            stats01 += transferenceService.transfer(source, destination, amount) < 0 ? 1: 0;
        }
        System.out.printf("%s negative occasions=%d %n", msg, stats01);
    }
}