package com.appsdeveloperblog.users.model;

import java.time.LocalDateTime;

/**
 * Immutable email event using Java 17 record.
 * Replaces Lombok @Data — no external dependency needed for this DTO.
 */
public record EmailEvent(
    Long userId,
    String to,
    String subject,
    String message,
    LocalDateTime timestamp
) {}
