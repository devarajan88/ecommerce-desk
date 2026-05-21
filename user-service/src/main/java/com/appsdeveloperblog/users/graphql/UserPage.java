package com.appsdeveloperblog.users.graphql;

import com.appsdeveloperblog.users.model.User;

import java.util.List;

public record UserPage(List<User> content, int totalElements, int totalPages) {}
