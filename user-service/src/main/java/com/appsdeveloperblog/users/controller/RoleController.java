package com.appsdeveloperblog.users.controller;

import com.appsdeveloperblog.users.dto.RoleResponse;
import com.appsdeveloperblog.users.model.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(role -> new RoleResponse(role.name(), role.getDisplayName()))
                .toList();
    }
}
