package com.appsdeveloperblog.users.kafka.consumer;

import com.appsdeveloperblog.users.model.EmailEvent;
import com.appsdeveloperblog.users.model.User;
import com.appsdeveloperblog.users.repository.UserRepository;
import com.appsdeveloperblog.users.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public EmailConsumer(UserRepository userRepository, EmailService emailService) {

        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-email-topic", groupId = "email-group")
    public void consume(EmailEvent event) throws MessagingException {
        // Java 17 record accessors: event.userId() instead of event.getUserId()
        User user = userRepository
                .findById(event.userId())
                .orElseThrow();

        emailService.sendEmail(
                user.getEmailId(),
                event.subject(),
                user.getName(),
                event.message(),
                "HR Team"
        );
    }
}
