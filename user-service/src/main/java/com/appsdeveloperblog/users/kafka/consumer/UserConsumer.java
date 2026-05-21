package com.appsdeveloperblog.users.kafka.consumer;

import com.appsdeveloperblog.users.model.User;
import com.appsdeveloperblog.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class UserConsumer implements Serializable {

    @Autowired
    private UserService userService;


    @KafkaListener(topics = "users-topic", groupId = "user-group")
    public void consume(User user) {
        User saved = userService.createUser(user);
        System.out.println("Received the user from kafka topics: " + saved.getName());
    }

}
