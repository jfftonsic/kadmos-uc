package com.example.controller;

import com.example.business.api.IBalanceService;
import com.example.exception.presentation.HttpFacingBaseException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;

@RestController
@SecurityRequirement(name = "api-security-requirement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BalanceController {

    IBalanceService balanceService;
    Clock clock;

    public record BalanceResponse(BigDecimal amount, ZonedDateTime timestamp) {
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        final var amount = balanceService.fetchAmount();
        return new BalanceResponse(amount, ZonedDateTime.now(clock));
    }

    public record FundsRequest(BigDecimal amount) {
    }

    @PostMapping("/balance/admin/funds")
    public void addFunds(@Valid @RequestBody FundsRequest req) {
        if (req.amount().signum() < 0) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST, "Amount must be positive.");
        }
        balanceService.addFunds(req.amount());
    }



}
