package com.appsdeveloperblog.users.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Setter
@Getter
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "emailId"),
                @Index(name = "idx_department", columnList = "department")
        })
public class User implements Serializable {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    private int age;

    private String emailId;
    private LocalDate dob;
    private String department;
    private Role role;
    private String baseLocation;
    private Status status;

    /** Not stored in DB — only used during user creation to register in Keycloak */
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @CreatedBy
    private LocalDateTime createdAt;

    @LastModifiedBy
    private LocalDateTime updatedAt;

    // Constructors, getters, setters
    public User() {
    }

}
