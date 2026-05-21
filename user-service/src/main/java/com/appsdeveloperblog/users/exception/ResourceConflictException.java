package com.appsdeveloperblog.users.exception;

/**
 * Java 17 sealed class subtype: represents resource conflict errors (e.g., duplicate email).
 * The 'final' keyword satisfies the sealed contract.
 */
public final class ResourceConflictException extends ApplicationException {

    private final String resourceName;
    private final String conflictField;

    public ResourceConflictException(String resourceName, String conflictField, String message) {
        super("RESOURCE_CONFLICT", message);
        this.resourceName = resourceName;
        this.conflictField = conflictField;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getConflictField() {
        return conflictField;
    }
}
