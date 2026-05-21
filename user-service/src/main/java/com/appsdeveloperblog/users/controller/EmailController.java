package com.appsdeveloperblog.users.controller;

import com.appsdeveloperblog.users.dto.EmailRequest;
import com.appsdeveloperblog.users.kafka.producer.EmailProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailProducer emailProducer;

    @PostMapping
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        emailProducer.publishEmailEvent(request);
        return ResponseEntity.ok("Email request sent successfully");
    }
}
