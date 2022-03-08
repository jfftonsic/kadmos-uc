package com.example.controller;

import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.BalanceResponse;
import com.example.controller.dataobject.UpdateBalanceRequest;
import com.example.exception.presentation.NotEnoughBalanceHttpException;
import com.example.exception.service.NotEnoughBalanceException;
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

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BalanceController {

    IBalanceService balanceService;

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        final var amount = balanceService.fetchAmount();
        return new BalanceResponse(amount);
    }

    @PostMapping("/balance")
    public BalanceResponse updateBalanceBy(@Valid @RequestBody UpdateBalanceRequest req) {

        try {
            return new BalanceResponse(balanceService.updateBalanceBy(req.amount()));
        } catch (NotEnoughBalanceException e) {
            throw new NotEnoughBalanceHttpException(e);
        }
    }
}
