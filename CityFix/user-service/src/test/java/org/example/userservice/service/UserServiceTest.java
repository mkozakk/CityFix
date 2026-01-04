package org.example.userservice.service;

import org.example.userservice.dto.*;
import org.example.userservice.entity.User;
import org.example.userservice.messaging.AuditEventPublisher;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    private UserService userService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, jwtTokenProvider, auditEventPublisher);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePass123")
                .firstName("Test")
                .lastName("User")
                .phone("+48123456789")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("+48123456789")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(auditEventPublisher).publishAudit(eq("register"), any());
    }

    @Test
    void testRegisterUsernameTaken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("SecurePass123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterEmailTaken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("SecurePass123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("SecurePass123")
                .build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("SecurePass123"))
                .firstName("Test")
                .lastName("User")
                .phone("+48123456789")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).findByUsername("testuser");
        verify(jwtTokenProvider).generateToken("testuser", 1L);
        verify(auditEventPublisher).publishAudit(eq("login"), any());
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("password")
                .build();

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        verify(userRepository).findByUsername("nonexistent");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("WrongPassword")
                .build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("CorrectPassword"))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        verify(userRepository).findByUsername("testuser");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    @Test
    void testGetUserByIdSuccess() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("+48123456789")
                .reportsCount(5)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(5, response.getReportsCount());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(999L));
        verify(userRepository).findById(999L);
    }

    @Test
    void testUpdateUserSuccess() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("+48123456789")
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("newemail@example.com")
                .phone("+48987654321")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("newemail@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("+48987654321")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("Updated", response.getFirstName());
        assertEquals("Name", response.getLastName());
        assertEquals("newemail@example.com", response.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserWithDuplicateEmail() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("otheremail@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("otheremail@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUserByUsernameSuccess() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUsername("testuser");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }
}
