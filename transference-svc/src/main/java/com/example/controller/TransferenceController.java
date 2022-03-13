package com.example.controller;

import com.example.TransferenceConfigurationProperties;
import com.example.controller.dataobject.TransferenceRequest;
import com.example.controller.dataobject.TransferenceResponse;
import com.example.db.dataobject.TransferenceCreationDO;
import com.example.db.relational.repository.CustomRepository;
import com.example.feign.BalanceAServiceClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransferenceController {

    // Don't call repository on controller
    CustomRepository customRepository;

    TransferenceConfigurationProperties properties;

    BalanceAServiceClient balanceAServiceClient;

    @GetMapping("/test-get-balance")
    public void testGetBalance() {
        log.info("Got balance {}", balanceAServiceClient.getBalance());
    }

    @PostMapping("/transference")
    public TransferenceResponse updateBalanceBy(@Valid @RequestBody TransferenceRequest req) {
        final var uuidTransference = UUID.randomUUID();
        final var uuidDebitRequest = UUID.randomUUID();
        final var uuidCreditRequest = UUID.randomUUID();
        final var uuidDebitRequestIdemCode = UUID.randomUUID().toString();
        final var uuidCreditRequestIdemCode = UUID.randomUUID().toString();
        customRepository.transferenceCreation(
                new TransferenceCreationDO(
                        uuidTransference,
                        req.idempotency().code(),
                        req.idempotency().actor(),
                        req.sourceBalance(),
                        req.destinationBalance(),
                        req.amount(),
                        uuidDebitRequest,
                        uuidDebitRequestIdemCode,
                        properties.idemActor(),
                        uuidCreditRequest,
                        uuidCreditRequestIdemCode,
                        properties.idemActor()
                )
        );
        return new TransferenceResponse();
    }
}
