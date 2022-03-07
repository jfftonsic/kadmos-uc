package com.example.exception.handler;

import com.example.exception.presentation.HttpFacingBaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    public record ErrorResponse(ZonedDateTime timestamp, String message, String path) {
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGenericRuntimeException(HttpServletRequest req, Exception e) {
        final var message = "Unhandled error.";
        log.error(message, e);

        return new ResponseEntity<>(
                new ErrorResponse(ZonedDateTime.now(), message, req.getServletPath()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(HttpFacingBaseException.class)
    public final ResponseEntity<ErrorResponse> handleHttpFacingBaseException(HttpServletRequest req,
            HttpFacingBaseException e) {
        return new ResponseEntity<>(
                new ErrorResponse(ZonedDateTime.now(), e.getUserFriendlyMessage(), req.getRequestURI()),
                e.getHttpStatus()
        );
    }
//
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Map<String, String> handleValidationExceptions(
//            MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return errors;
//    }
}
