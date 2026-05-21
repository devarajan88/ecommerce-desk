package com.appsdeveloperblog.users.kafka.producer;

import com.appsdeveloperblog.users.model.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserProducer {

    private final KafkaTemplate<Long, User> kafkaTemplate;

    public UserProducer(KafkaTemplate<Long, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUser(User user) {
        kafkaTemplate.send("users-topic", user.getId(), user);
    }

    public void sendRandomUsers(int count) {
        for (int i = 0; i < count; i++) {
            User user = generateRandomUser();
            kafkaTemplate.send("users-topic", user.getId(), user);
        }
    }

    private User generateRandomUser() {
        Random random = new Random();

        User user = new User();
        user.setName("User-" + random.nextInt(1000));
        user.setEmailId("user" + random.nextInt(1000) + "@mail.com");
        user.setBaseLocation("TN-" + random.nextInt(50000));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
}
