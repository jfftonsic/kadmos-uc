package com.example.business;

import com.example.business.MyExtension.MyExtensionOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MyExtension.class)
class MtTransferenceStrategy01Test {

    public static final int TRANSFERENCES_MADE_BY_EACH_ACCOUNT = 10000000;
    final MtTransferenceStrategy01 transferenceService = new MtTransferenceStrategy01();

    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = 1000000,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af.class
    )
    void transfer(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {

        doIt(myExtensionOptions, financialEnvironment, transferenceService);
    }


    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = 1000000,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af2.class
    )
    void transfer2(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {

        doIt(myExtensionOptions, financialEnvironment, transferenceService);
    }


    @Test
    @MyExtensionOptions(
            numberOfThreads = 7,
            numberOfAccounts = 5,
            moneyInEachAccount = 1000000,
            transferencesMadeByEachThread = TRANSFERENCES_MADE_BY_EACH_ACCOUNT,
            amountTransferred = 2,
            accountFactory = AccountFactories.Af3.class
    )
    void transfer3(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment) throws Exception {

        doIt(myExtensionOptions, financialEnvironment, transferenceService);
    }


    private void doIt(MyExtensionOptions myExtensionOptions, FinancialEnvironment financialEnvironment,
            MtTransferenceStrategy01 transferenceService) {
        int accountIdx = 0;
        for (int j = 0; j < myExtensionOptions.transferencesMadeByEachThread(); j++) {
            final var source = financialEnvironment.getAccount(accountIdx);
            accountIdx = (accountIdx + 1) % myExtensionOptions.numberOfAccounts();
            final var destination = financialEnvironment.getAccount(accountIdx);
            accountIdx = (accountIdx + 1) % myExtensionOptions.numberOfAccounts();

            transferenceService.transfer(source, destination, myExtensionOptions.amountTransferred());
        }
    }
}