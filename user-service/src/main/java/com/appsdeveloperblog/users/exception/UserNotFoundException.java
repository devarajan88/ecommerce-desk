package com.appsdeveloperblog.users.exception;

/**
 * Java 17 sealed class subtype: final class that extends the sealed ApplicationException.
 * The 'final' keyword satisfies the sealed contract (must be final, sealed, or non-sealed).
 */
public final class UserNotFoundException extends ApplicationException {

    private final Long userId;

    public UserNotFoundException(Long id) {
        super("USER_NOT_FOUND", "User not found with id: " + id);
        this.userId = id;
    }

    public Long getUserId() {
        return userId;
    }
}
