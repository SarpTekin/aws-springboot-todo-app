package com.microtodo.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.microtodo.user_service.dto.UserRequest;
import com.microtodo.user_service.model.User;
import com.microtodo.user_service.repository.UserRepository;
import com.microtodo.user_service.dto.UserResponse;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    public void testCreateUser() {
        // Arrange (Given)
        UserRequest request = new UserRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        // Create mock saved user
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("testuser@example.com");
        savedUser.setPassword("$2a$10$hashedpassword");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        // Mock the behavior: username/email don't exist
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
        
        // Mock password encoding
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        
        // Mock repository save
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act (When)
        UserResponse response = userService.createUser(request);

        // Assert (Then)
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("testuser@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        
        // Verify password was hashed
        verify(passwordEncoder, times(1)).encode("password123");
        
        // Verify repository was called
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("testuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testCreateUser_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock: username already exists
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.createUser(request));
        
        assertEquals("Username already exists", exception.getMessage());
        
        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
    }
}
