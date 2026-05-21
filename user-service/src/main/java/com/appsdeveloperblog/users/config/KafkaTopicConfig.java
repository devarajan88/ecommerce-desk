package com.appsdeveloperblog.users.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

public class KafkaTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("users-topic")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }
}
