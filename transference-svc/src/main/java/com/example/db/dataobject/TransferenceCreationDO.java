package com.example.db.dataobject;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenceCreationDO(
        UUID transferenceId,
        String transferenceIdemCode,
        String transferenceIdemActor,
        String transferenceSourceBalance,
        String transferenceDestinationBalance,
        BigDecimal transferenceAmount,
        UUID transferenceDebitRequestId,
        String transferenceDebitRequestReqIdemCode,
        String transferenceDebitRequestReqIdemActor,
        UUID transferenceCreditRequestId,
        String transferenceCreditRequestReqIdemCode,
        String transferenceCreditRequestReqIdemActor
        ) {
    
}
