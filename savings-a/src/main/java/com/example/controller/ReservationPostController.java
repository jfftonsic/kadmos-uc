package com.example.controller;

import com.example.UUIDGenerator;
import com.example.business.UpdateReservationInitService;
import com.example.business.api.IBalanceService;
import com.example.controller.dataobject.Idempotency;
import com.example.controller.dataobject.UpdateReservation;
import com.example.exception.presentation.HttpFacingBaseException;
import com.example.exception.presentation.NotEnoughBalanceHttpException;
import com.example.exception.service.NotEnoughBalanceException;
import static com.example.util.GeneralConstants.LOG_AMOUNT_ROUNDING_MODE;
import static com.example.util.GeneralConstants.LOG_AMOUNT_SCALE;
import com.example.util.SafeUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
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
public class ReservationPostController {
    IBalanceService balanceService;

    UpdateReservationInitService initService;

    UUIDGenerator uuidGenerator;

    Clock clock;


    public record UpdateReservationPostRequest(ZonedDateTime timestamp, @Valid Idempotency idempotency,
                                               @NotNull BigDecimal amount) {
    }

    public record UpdateReservationPostResponse(ZonedDateTime timestamp, UpdateReservation updateReservation) {
    }

    @PostMapping("/balance/update-reservation")
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public UpdateReservationPostResponse postUpdateReservation(
            @Valid @RequestBody UpdateReservationPostRequest req,
            Principal principal
    ) {
        Assert.notNull(principal, "Missing authentication information.");
        final var principalName = principal.getName();
        Assert.hasLength(principalName, "Empty principal name not allowed.");

        log.info("m=postUpdateReservation principalName={} amount={}",
                principalName,
                req.amount().setScale(LOG_AMOUNT_SCALE, LOG_AMOUNT_ROUNDING_MODE));

        // TODO check idempotency

        if (BigDecimal.ZERO.compareTo(req.amount()) == 0) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST, "Amount must be different than 0.");
        }

        final var reservationCode = initService.initUpdateReservation(
                SafeUtil.safeIdempotencyCode(uuidGenerator, req.idempotency()),
                principalName,
                SafeUtil.safeTimestamp(clock, req.timestamp()),
                req.amount()
        );

        try {
            balanceService.reserve(reservationCode);
            return new UpdateReservationPostResponse(
                    ZonedDateTime.now(clock),
                    new UpdateReservation(reservationCode.toString())
            );
        } catch (NotEnoughBalanceException e) {
            throw new NotEnoughBalanceHttpException(e);
        }
    }
}
