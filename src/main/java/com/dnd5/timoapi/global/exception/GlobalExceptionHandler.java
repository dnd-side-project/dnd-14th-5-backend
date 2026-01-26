package com.dnd5.timoapi.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle a BusinessException by constructing an ErrorResponse from its error code and returning it with the corresponding HTTP status.
     *
     * @param e the BusinessException that contains an ErrorCode describing the error and its HTTP status
     * @return a ResponseEntity whose body is an ErrorResponse created from the exception's ErrorCode and whose status matches that ErrorCode
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    /**
     * Handles validation failures raised by method argument binding and produces a client-facing error response.
     *
     * <p>Aggregates field errors into a single comma-separated message using the format "field: message".</p>
     *
     * @param e the validation exception containing binding and field error details
     * @return a ResponseEntity with HTTP 400 Bad Request and an ErrorResponse whose code is "VALIDATION_ERROR" and whose message contains the aggregated field error descriptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }
}