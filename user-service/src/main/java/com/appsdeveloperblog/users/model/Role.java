package com.appsdeveloperblog.users.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;

/**
 * Java 17: Uses Stream + Optional pattern instead of for-loop in fromDisplayName().
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Role {

    SENIOR_SOFTWARE_ENGINEER("Senior Software Engineer"),
    ARCHITECT("Architect"),
    JUNIOR_DEVELOPER("Junior Developer"),
    BUSINESS_ANALYST("Business Analyst"),
    SCRUM_MASTER("Scrum Master"),
    DB_ADMINISTRATOR("DB Administrator"),
    MANAGER("Manager");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Java 16+: Stream.toList() + Optional pattern — more idiomatic than for-loop
    public static Role fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(role -> role.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + displayName));
    }
}
