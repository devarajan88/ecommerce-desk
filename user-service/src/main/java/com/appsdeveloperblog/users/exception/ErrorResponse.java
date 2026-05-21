package com.appsdeveloperblog.users.exception;

/**
 * Immutable error response DTO using Java 17 record.
 * Automatically provides constructor, getters (errorCode(), message()),
 * equals(), hashCode(), and toString().
 */
public record ErrorResponse(
    String errorCode,
    String message
) {}
