package com.appsdeveloperblog.users.graphql.input;

public record UserInput(
        String name,
        int age,
        String emailId,
        String department,
        String role,
        String baseLocation,
        String password
) {}
