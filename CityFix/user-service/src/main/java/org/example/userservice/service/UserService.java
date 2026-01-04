package org.example.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.*;
import org.example.userservice.entity.User;
import org.example.userservice.event.AuditEvent;
import org.example.userservice.messaging.AuditEventPublisher;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditEventPublisher;

    @Autowired
    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
                      AuditEventPublisher auditEventPublisher) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Publish audit event
        publishAudit("register", savedUser.getId(), savedUser.getUsername(),
                    "User", savedUser.getId(), "User registered");

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", request.getUsername());
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        log.info("Token generated successfully for user: {} (ID: {})", user.getUsername(), user.getId());

        // Publish audit event
        publishAudit("login", user.getId(), user.getUsername(),
                    "User", user.getId(), "User logged in");

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new IllegalArgumentException("User not found");
                });
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new IllegalArgumentException("User not found");
                });

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        // Publish audit event
        publishAudit("update", updatedUser.getId(), updatedUser.getUsername(),
                    "User", updatedUser.getId(), "User profile updated");

        return mapToUserResponse(updatedUser);
    }

    public UserResponse getUserByUsername(String username) {
        log.info("Getting user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .reportsCount(user.getReportsCount() != null ? user.getReportsCount() : 0)
                .build();
    }

    private void publishAudit(String action, Long userId, String username,
                             String entityType, Long entityId, String details) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .eventType("USER")
                    .userId(userId)
                    .username(username)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditEventPublisher.publishAudit(action, event);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", e.getMessage());
        }
    }
}
