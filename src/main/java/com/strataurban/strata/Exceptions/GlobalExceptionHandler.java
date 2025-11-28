package com.strataurban.strata.Exceptions;

import com.strataurban.strata.DTOs.ErrorResponse;
import com.strataurban.strata.yoco_integration.exceptions.PaymentException;
import com.strataurban.strata.yoco_integration.exceptions.WebhookValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return ResponseEntity.badRequest().body(errors);
//    }

//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(new ErrorResponse("Invalid credentials", "AUTH_001"));
//    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Access denied", "AUTH_002"));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }


    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<com.strataurban.strata.yoco_integration.dtos.ErrorResponse> handlePaymentException(
            PaymentException ex,
            HttpServletRequest request) {

        log.error("Payment error: {}", ex.getMessage(), ex);

        com.strataurban.strata.yoco_integration.dtos.ErrorResponse error = com.strataurban.strata.yoco_integration.dtos.ErrorResponse.builder()
                .error("Payment Error")
                .message(ex.getMessage())
                .code("PAYMENT_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(WebhookValidationException.class)
    public ResponseEntity<com.strataurban.strata.yoco_integration.dtos.ErrorResponse> handleWebhookValidationException(
            WebhookValidationException ex,
            HttpServletRequest request) {

        log.error("Webhook validation error: {}", ex.getMessage(), ex);

        com.strataurban.strata.yoco_integration.dtos.ErrorResponse error = com.strataurban.strata.yoco_integration.dtos.ErrorResponse.builder()
                .error("Webhook Validation Error")
                .message(ex.getMessage())
                .code("WEBHOOK_VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.strataurban.strata.yoco_integration.dtos.ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", message);

        com.strataurban.strata.yoco_integration.dtos.ErrorResponse error = com.strataurban.strata.yoco_integration.dtos.ErrorResponse.builder()
                .error("Validation Error")
                .message(message)
                .code("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<com.strataurban.strata.yoco_integration.dtos.ErrorResponse> handleHttpClientError(
            HttpClientErrorException ex,
            HttpServletRequest request) {

        log.error("HTTP client error: Status={}, Body={}",
                ex.getStatusCode(), ex.getResponseBodyAsString());

        com.strataurban.strata.yoco_integration.dtos.ErrorResponse error = com.strataurban.strata.yoco_integration.dtos.ErrorResponse.builder()
                .error("External Service Error")
                .message("Payment provider error occurred")
                .code("EXTERNAL_SERVICE_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.strataurban.strata.yoco_integration.dtos.ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error", ex);

        com.strataurban.strata.yoco_integration.dtos.ErrorResponse error = com.strataurban.strata.yoco_integration.dtos.ErrorResponse.builder()
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .code("INTERNAL_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
