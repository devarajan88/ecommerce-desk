package com.appsdeveloperblog.users.service;


import com.appsdeveloperblog.users.model.Address;
import com.appsdeveloperblog.users.model.User;
import com.appsdeveloperblog.users.model.Role;
import com.appsdeveloperblog.users.model.Status;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserCreatedEvent {

    private Long version;
    private Long id;
    private String name;
    private int age;
    private String emailId;
    private LocalDate dob;
    private String department;
    private Role role;
    private String baseLocation;
    private Status status;
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User user;

    public UserCreatedEvent() {
    }

    public UserCreatedEvent(Long version, Long id, String name, int age, String emailId, LocalDate dob, String department, Role role, String baseLocation, Status status, Address address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.version = version;
        this.id = id;
        this.name = name;
        this.age = age;
        this.emailId = emailId;
        this.dob = dob;
        this.department = department;
        this.role = role;
        this.baseLocation = baseLocation;
        this.status = status;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserCreatedEvent(User user) {
        this.user = user;
    }
}
