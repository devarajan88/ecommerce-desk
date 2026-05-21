package com.appsdeveloperblog.users.kafka.producer;

import com.appsdeveloperblog.users.dto.EmailRequest;
import com.appsdeveloperblog.users.model.EmailEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailProducer {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    public EmailProducer(KafkaTemplate<String, EmailEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEmailEvent(EmailRequest request) {
        // Java 17 record — all fields via constructor (immutable)
        var event = new EmailEvent(
                request.userId(),
                null, // 'to' is resolved by the consumer from user lookup
                request.subject(),
                request.message(),
                LocalDateTime.now()
        );

        kafkaTemplate.send("user-email-topic", event);
    }
}
