//package com.strataurban.strata.yoco_integration.exceptions;
//
//
//import com.strataurban.strata.yoco_integration.dtos.ErrorResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.client.HttpClientErrorException;
//
//import java.time.LocalDateTime;
//import java.util.stream.Collectors;
//
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(PaymentException.class)
//    public ResponseEntity<ErrorResponse> handlePaymentException(
//            PaymentException ex,
//            HttpServletRequest request) {
//
//        log.error("Payment error: {}", ex.getMessage(), ex);
//
//        ErrorResponse error = ErrorResponse.builder()
//                .error("Payment Error")
//                .message(ex.getMessage())
//                .code("PAYMENT_ERROR")
//                .timestamp(LocalDateTime.now())
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//    }
//
//    @ExceptionHandler(WebhookValidationException.class)
//    public ResponseEntity<ErrorResponse> handleWebhookValidationException(
//            WebhookValidationException ex,
//            HttpServletRequest request) {
//
//        log.error("Webhook validation error: {}", ex.getMessage(), ex);
//
//        ErrorResponse error = ErrorResponse.builder()
//                .error("Webhook Validation Error")
//                .message(ex.getMessage())
//                .code("WEBHOOK_VALIDATION_ERROR")
//                .timestamp(LocalDateTime.now())
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationException(
//            MethodArgumentNotValidException ex,
//            HttpServletRequest request) {
//
//        String message = ex.getBindingResult().getFieldErrors().stream()
//                .map(error -> error.getField() + ": " + error.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//
//        log.error("Validation error: {}", message);
//
//        ErrorResponse error = ErrorResponse.builder()
//                .error("Validation Error")
//                .message(message)
//                .code("VALIDATION_ERROR")
//                .timestamp(LocalDateTime.now())
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//    }
//
//    @ExceptionHandler(HttpClientErrorException.class)
//    public ResponseEntity<ErrorResponse> handleHttpClientError(
//            HttpClientErrorException ex,
//            HttpServletRequest request) {
//
//        log.error("HTTP client error: Status={}, Body={}",
//                ex.getStatusCode(), ex.getResponseBodyAsString());
//
//        ErrorResponse error = ErrorResponse.builder()
//                .error("External Service Error")
//                .message("Payment provider error occurred")
//                .code("EXTERNAL_SERVICE_ERROR")
//                .timestamp(LocalDateTime.now())
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(
//            Exception ex,
//            HttpServletRequest request) {
//
//        log.error("Unexpected error", ex);
//
//        ErrorResponse error = ErrorResponse.builder()
//                .error("Internal Server Error")
//                .message("An unexpected error occurred")
//                .code("INTERNAL_ERROR")
//                .timestamp(LocalDateTime.now())
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//    }
//}