package com.appsdeveloperblog.orders.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Polyglot persistence configuration for orders-service.
 *
 * <p>Spring Boot auto-configuration cannot distinguish JPA repositories from
 * MongoDB repositories when both starters are on the classpath. Explicitly
 * declaring the base packages for each Spring Data module prevents conflicts:
 *
 * <ul>
 *   <li>{@code dao.jpa.repository} — relational stores backed by MySQL/Hibernate.
 *       Used for the mutable {@code orders} table (current saga state).</li>
 *   <li>{@code dao.mongodb.repository} — document store backed by MongoDB.
 *       Used for the immutable {@code order_history} collection (saga audit trail).</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.appsdeveloperblog.orders.dao.jpa.repository")
@EnableMongoRepositories(basePackages = "com.appsdeveloperblog.orders.dao.mongodb.repository")
public class DataConfig {
}
