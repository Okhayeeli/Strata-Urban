package com.strataurban.strata.yoco_integration.exceptions;

public class WebhookValidationException extends RuntimeException {
    public WebhookValidationException(String message) {
        super(message);
    }

    public WebhookValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}