package com.example.controller;

import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.Idempotency;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
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

    public record BalanceResponse(BigDecimal amount, ZonedDateTime timestamp) {}

    @GetMapping("/balance")
    public BalanceResponse getBalance(Principal principal) {
        log.info(String.valueOf(principal));
        final var amount = balanceService.fetchAmount();
        return new BalanceResponse(amount, ZonedDateTime.now(clock));
    }

    public record FundsRequest(BigDecimal amount) {}

    @PostMapping("/balance/admin/funds")
    @RolesAllowed("ADMIN")
    public void addFunds(@Valid @RequestBody FundsRequest req) {
        if (req.amount().signum() < 0) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST, "Amount must be positive.");
        }
        balanceService.addFunds(req.amount());
    }

    public record UpdateReservationPostRequest(ZonedDateTime timestamp, Idempotency idempotency, BigDecimal amount) {}
    public record UpdateReservationPostResponse(ZonedDateTime timestamp, String updateReservationCode) {}

    @PostMapping("/balance/update-reservation")
    public UpdateReservationPostResponse updateBalanceBy(
            @Valid @RequestBody UpdateReservationPostRequest req,
            Principal principal
    ) {

        try {
            return new UpdateReservationPostResponse(
                    ZonedDateTime.now(clock),
                    balanceService.createUpdateReservation(
                            req.idempotency().code(),
                            principal.getName(),
                            req.timestamp(),
                            req.amount()
                    )
            );
        } catch (NotEnoughBalanceException e) {
            throw new NotEnoughBalanceHttpException(e);
        }
    }

    public record UpdateReservationPatchRequest(ZonedDateTime timestamp) {}
    public record UpdateReservationPatchResponse(ZonedDateTime timestamp) {}

    @PatchMapping("/balance/update-reservation/{updateReservationCode}")
    public UpdateReservationPatchResponse updateBalanceBy(
            @PathVariable String updateReservationCode,
            @Valid @RequestBody UpdateReservationPatchRequest req,
            Principal principal
    ) {
        log.info("principal={}", principal);
        return new UpdateReservationPatchResponse(ZonedDateTime.now(clock));
    }


}
