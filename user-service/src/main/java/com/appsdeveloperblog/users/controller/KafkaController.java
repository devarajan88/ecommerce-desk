package com.appsdeveloperblog.users.controller;

import com.appsdeveloperblog.users.kafka.producer.UserProducer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final UserProducer userProducer;

    public KafkaController(UserProducer userProducer) {
        this.userProducer = userProducer;
    }

    @PostMapping("/send/{count}")
    public String send(@PathVariable int count) {
        userProducer.sendRandomUsers(count);
        return count + " users sent to Kafka";
    }
}
