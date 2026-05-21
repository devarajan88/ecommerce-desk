package com.appsdeveloperblog.users.dto;

public record EmailRequest(
        Long userId,
        String subject,
        String message) {
}
