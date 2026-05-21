package com.appsdeveloperblog.users.exception;

/**
 * Java 17 sealed class: defines a closed set of known application exceptions.
 * Only UserNotFoundException and ResourceConflictException can extend this.
 * This enables exhaustive pattern matching in switch expressions.
 */
public sealed abstract class ApplicationException extends RuntimeException
        permits UserNotFoundException, ResourceConflictException {

    private final String errorCode;

    protected ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
