package com.appsdeveloperblog.users.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;  // ← CRITICAL: jakarta.persistence (Spring Boot 3+)
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "addresses")
public class Address implements Serializable {

    // All getters and setters
    @Id  // ← PRIMARY KEY - REQUIRED
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String no;
    private String street;
    private String area;
    private String city;
    private String zip;
    private String country;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private User user;

    // DEFAULT CONSTRUCTOR - REQUIRED BY JPA
    public Address() {}

}
