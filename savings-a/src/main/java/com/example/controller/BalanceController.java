package com.example.controller;

import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.BalanceResponse;
import com.example.controller.dataobject.FundsRequest;
import com.example.controller.dataobject.UpdateReservationPostRequest;
import com.example.controller.dataobject.UpdateReservationPostResponse;
import com.example.exception.presentation.HttpFacingBaseException;
import com.example.exception.presentation.NotEnoughBalanceHttpException;
import com.example.exception.service.NotEnoughBalanceException;
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
import java.time.ZonedDateTime;

@RestController
@SecurityRequirement(name = "api-security-requirement")
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

    @PostMapping("/balance/admin/funds")
    public void addFunds(@Valid @RequestBody FundsRequest req) {
        if (req.amount().signum() < 0) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST, "Amount must be positive.");
        }
        balanceService.addFunds(req.amount());
    }

    @PostMapping("/balance/update-reservation")
    public UpdateReservationPostResponse updateBalanceBy(@Valid @RequestBody UpdateReservationPostRequest req) {

        try {
            return new UpdateReservationPostResponse(
                    ZonedDateTime.now(),
                    balanceService.createUpdateReservation(
                            req.idempotency().code(),
                            req.idempotency().actor(),
                            req.timestamp(),
                            req.amount()
                    )
            );
        } catch (NotEnoughBalanceException e) {
            throw new NotEnoughBalanceHttpException(e);
        }
    }
}
