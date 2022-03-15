package com.example.controller.dataobject;

import java.time.ZonedDateTime;

public record UpdateReservationPostResponse(ZonedDateTime timestamp, String updateReservationCode) {
}
