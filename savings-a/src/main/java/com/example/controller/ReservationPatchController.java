package com.example.controller;

import com.example.business.UpdateReservationConfirmationService;
import com.example.business.UpdateReservationInitService;
import com.example.exception.presentation.HttpFacingBaseException;
import com.example.util.SafeUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "api-security-requirement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReservationPatchController {
    UpdateReservationInitService initService;

    UpdateReservationConfirmationService updateReservationConfirmationService;

    Clock clock;


    public record UpdateReservationPatchRequest(ZonedDateTime timestamp) {
    }

    public record UpdateReservationPatchResponse(ZonedDateTime timestamp) {
    }

    @PatchMapping("/balance/update-reservation/{updateReservationCode}")
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public UpdateReservationPatchResponse patchUpdateReservation(
            @PathVariable String updateReservationCode,
            @Valid @RequestBody UpdateReservationPatchRequest req,
            Principal principal
    ) {
        log.info("principal={}", principal);

        final var timestamp = SafeUtil.safeTimestamp(clock, req.timestamp());

        if (updateReservationCode == null) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST, "Update reservation code required.");
        }

        final UUID reservationCode;
        try {
            reservationCode = UUID.fromString(updateReservationCode);
        } catch (IllegalArgumentException e) {
            throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST,
                    "Update reservation code must be an UUID, for example: ec2aaf17-7106-445c-ae2c-93f72f24a2b4.");
        }

        final UUID confirmationId;
        try {
            confirmationId = initService.initConfirmation(reservationCode, timestamp);
        } catch (UpdateReservationInitService.ConfirmUnknownReservationException e) {
            throw new HttpFacingBaseException(HttpStatus.NOT_FOUND,
                    "Unknown update reservation for code %s.".formatted(reservationCode));
        }

        final var confirmUpdateReservationResponse = updateReservationConfirmationService.confirmUpdateReservation(
                reservationCode,
                confirmationId);

        return switch (confirmUpdateReservationResponse) {
            case DONE -> new UpdateReservationPatchResponse(ZonedDateTime.now(clock));
            case NO_CHANGES -> {
                log.info("m=patchUpdateReservation info=reConfirmation reservationCode={} confirmationId={}",
                        reservationCode,
                        confirmationId);
                yield new UpdateReservationPatchResponse(ZonedDateTime.now(clock));
            }
            case UPDATE_RESERVATION_NOT_FOUND -> throw new HttpFacingBaseException(HttpStatus.NOT_FOUND,
                    "Unknown update reservation for code %s.".formatted(reservationCode));
            case CONFIRMATION_NOT_FOUND -> {
                log.error("m=patchUpdateReservation r=recentlyInitializedConfirmationNotFound id={}", confirmationId);
                throw new HttpFacingBaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unknown update reservation for code %s.".formatted(reservationCode));
            }
            case RECEIVED_BUT_NOT_RESERVED -> {
                log.info(
                        "m=patchUpdateReservation r=tryingToConfirmUnpreparedReservation reservationCode={} confirmationId={}",
                        reservationCode,
                        confirmationId);
                throw new HttpFacingBaseException(HttpStatus.CONFLICT,
                        "Trying to confirm a reservation that is not yet properly initialized, try again later.");
            }
            case WAS_INVALID -> throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST,
                    "Trying to confirm a reservation that resulted in a validation error previously.");
            case WAS_ALREADY_CANCELED -> throw new HttpFacingBaseException(HttpStatus.BAD_REQUEST,
                    "Trying to confirm a reservation that was already cancelled.");
        };

    }
}
