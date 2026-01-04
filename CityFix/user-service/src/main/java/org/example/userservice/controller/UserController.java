package org.example.userservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.*;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Value("${jwt.cookie.name:JWT_TOKEN}")
    private String jwtCookieName;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("User registration request for username: {}", request.getUsername());
        try {
            UserResponse response = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        log.info("User login request for username: {}", request.getUsername());
        try {
            LoginResponse loginResponse = userService.login(request);

            // Ustawienie JWT w HTTP Cookie
            setJwtCookie(response, loginResponse.getToken());

            // Zwrocenie response bez tokenu w body
            LoginResponse responseWithoutToken = LoginResponse.builder()
                    .id(loginResponse.getId())
                    .username(loginResponse.getUsername())
                    .email(loginResponse.getEmail())
                    .firstName(loginResponse.getFirstName())
                    .lastName(loginResponse.getLastName())
                    .phone(loginResponse.getPhone())
                    .build();

            return ResponseEntity.ok(responseWithoutToken);
        } catch (IllegalArgumentException e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        log.info("User logout request");

        // Usunięcie JWT cookie
        removeCookie(response);

        // Wyczyszczenie security context
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Getting current user profile");

        // Security check: User must be authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized get attempt for current user");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get authenticated username
        String authenticatedUsername = authentication.getName();
        UserResponse currentUser = userService.getUserByUsername(authenticatedUsername);

        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating current user profile");

        // Security check: User must be authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized update attempt for current user");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get authenticated username
        String authenticatedUsername = authentication.getName();
        UserResponse currentUser = userService.getUserByUsername(authenticatedUsername);

        try {
            UserResponse response = userService.updateUser(currentUser.getId(), request);
            log.info("User {} successfully updated profile", authenticatedUsername);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Update failed: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }

    /**
     * Ustawia JWT token w HTTP Cookie z bezpiecznymi flagami
     */
    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(jwtCookieName, token);
        cookie.setHttpOnly(true);              // Niedostępne z JavaScript
        cookie.setSecure(false);               // FALSE dla testów lokalnych (HTTP)
        cookie.setPath("/");                   // Dostępne na całej ścieżce
        cookie.setMaxAge((int) (jwtExpiration / 1000)); // Konwersja z ms na sekundy
        cookie.setAttribute("SameSite", "Strict"); // Ochrona przed CSRF

        response.addCookie(cookie);
        log.debug("JWT cookie set: {}", jwtCookieName);
    }

    /**
     * Usuwa JWT cookie
     */
    private void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);               // FALSE dla testów lokalnych
        cookie.setPath("/");
        cookie.setMaxAge(0); // Natychmiastowe usunięcie
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);
        log.debug("JWT cookie removed");
    }
}
