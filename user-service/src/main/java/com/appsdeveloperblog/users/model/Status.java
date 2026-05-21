package com.appsdeveloperblog.users.model;

/**
 * Java 17 switch expression used in fromCode() method.
 */
public enum Status {
    INACTIVE(0),
    ACTIVE(1);

    private final int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // Java 17: Switch expression with arrow syntax (cleaner than for-loop)
    public static Status fromCode(int code) {
        return switch (code) {
            case 0 -> INACTIVE;
            case 1 -> ACTIVE;
            default -> throw new IllegalArgumentException("Invalid Status code: " + code);
        };
    }
}
