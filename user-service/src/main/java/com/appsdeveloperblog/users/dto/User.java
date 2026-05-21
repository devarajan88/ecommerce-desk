package com.appsdeveloperblog.users.dto;

import java.time.LocalDate;

public record User (

     Long id,
     String name,
     int age,
     String emailId,
     LocalDate dob
) {}
