package com.appsdeveloperblog.users.service;

import com.appsdeveloperblog.users.exception.UserNotFoundException;
import com.appsdeveloperblog.users.model.User;
import com.appsdeveloperblog.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserService(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void saveOtp(String key, String otp) {
        redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
    }

    @CachePut(value = "users", key = "#result.id")
    @Transactional
    public User createUser(User user) {
        // 1. Save user to DB
        User saved = userRepository.save(user);

        // 2. Publish Kafka event
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(saved);
        kafkaTemplate.send("users-topic", String.valueOf(saved.getId()), userCreatedEvent);

        // 3. Register user in Keycloak so they can login
        String password = user.getPassword();
        if (password == null || password.isBlank()) {
            password = "Welcome@123"; // fallback default
        }
        String[] nameParts = (saved.getName() != null ? saved.getName() : "User").split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        // Use full email address as username so users can login with their email
        String username = (saved.getEmailId() != null && !saved.getEmailId().isBlank())
                ? saved.getEmailId()
                : saved.getName().toLowerCase().replace(" ", ".");
        try {
            keycloakAdminService.createKeycloakUser(username, saved.getEmailId(), firstName, lastName, password);
        } catch (Exception e) {
            // Don't rollback DB save — log the Keycloak error
            System.err.println("Warning: Keycloak registration failed for user " + username + ": " + e.getMessage());
        }

        return saved;
    }

    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public Page<User> getAllUsers(int page, int size) {
        // Java 10+ var: inferred type is Pageable
        var pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    @CachePut(value = "users", key = "#user.id")
    public User updateUser(Long id, User user) {
        // Java 10+ var: inferred type is User
        var existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        existing.setName(user.getName());
        existing.setAge(user.getAge());
        existing.setEmailId(user.getEmailId());
        existing.setDepartment(user.getDepartment());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());
        existing.setBaseLocation(user.getBaseLocation());
        existing.setDob(user.getDob());

        // Update address safely
        if (existing.getAddress() != null && user.getAddress() != null) {
            var existingAddr = existing.getAddress();
            var newAddr = user.getAddress();
            existingAddr.setStreet(newAddr.getStreet());
            existingAddr.setCity(newAddr.getCity());
            existingAddr.setZip(newAddr.getZip());
            existingAddr.setArea(newAddr.getArea());
            existingAddr.setCountry(newAddr.getCountry());
        }

        return userRepository.save(existing);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // Java 10+ var: inferred type
        var user = getUser(id);
        userRepository.delete(user);
    }
}
