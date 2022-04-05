package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.Idempotency;
import com.example.controller.dataobject.UpdateReservation;
import com.example.exception.presentation.HttpFacingBaseException;
import com.example.exception.presentation.NotEnoughBalanceHttpException;
import com.example.exception.service.NotEnoughBalanceException;
import com.example.util.SafeUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    UUIDGenerator uuidGenerator;
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

    public record UpdateReservationPostRequest(ZonedDateTime timestamp, @Valid Idempotency idempotency,
                                               @NotNull BigDecimal amount) {
    }

    public record UpdateReservationPostResponse(ZonedDateTime timestamp, UpdateReservation updateReservation) {
    }

    @PostMapping("/balance/update-reservation")
    public UpdateReservationPostResponse postUpdateReservation(
            @Valid @RequestBody UpdateReservationPostRequest req,
            Principal principal
    ) {
        Assert.notNull(principal, "Missing authentication information.");
        Assert.hasLength(principal.getName(), "Empty principal name not allowed.");

        try {
            return new UpdateReservationPostResponse(
                    ZonedDateTime.now(clock),
                    new UpdateReservation(balanceService.reserve(
                            SafeUtil.safeIdempotencyCode(uuidGenerator, req.idempotency())
                    ))
            );
        } catch (NotEnoughBalanceException e) {
            throw new NotEnoughBalanceHttpException(e);
        }
    }

    public record UpdateReservationPatchRequest(ZonedDateTime timestamp) {
    }

    public record UpdateReservationPatchResponse(ZonedDateTime timestamp) {
    }

    @PatchMapping("/balance/update-reservation/{updateReservationCode}")
    public UpdateReservationPatchResponse patchUpdateReservation(
            @PathVariable String updateReservationCode,
            @Valid @RequestBody UpdateReservationPatchRequest req,
            Principal principal
    ) {
        log.info("principal={}", principal);
        return new UpdateReservationPatchResponse(ZonedDateTime.now(clock));
    }

}
