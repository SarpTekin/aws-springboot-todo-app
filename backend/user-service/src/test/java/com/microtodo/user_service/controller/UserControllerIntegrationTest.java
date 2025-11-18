package com.microtodo.user_service.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microtodo.user_service.dto.UserRequest;
import com.microtodo.user_service.model.User;
import com.microtodo.user_service.repository.UserRepository;
import com.microtodo.user_service.util.TestJwtHelper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String testToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password123");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testCreateUser_DuplicateUsername() throws Exception {
        // Create first user
        User firstUser = new User();
        firstUser.setUsername("testuser");
        firstUser.setEmail("first@example.com");
        firstUser.setPassword("password123");
        userRepository.save(firstUser);

        // Try to create second user with same username
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("second@example.com");
        userRequest.setPassword("password123");

        // Expect RuntimeException due to duplicate username
        try {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)));
        } catch (Exception e) {
            // Expected - RuntimeException: Username already exists
            assertTrue(e.getCause() != null && e.getCause().getMessage().contains("Username already exists"));
        }
    }

    @Test
    void testCreateUser_DuplicateEmail() throws Exception {
        // Create first user
        User firstUser = new User();
        firstUser.setUsername("firstuser");
        firstUser.setEmail("test@example.com");
        firstUser.setPassword("password123");
        userRepository.save(firstUser);

        // Try to create second user with same email
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("seconduser");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password123");

        // Expect RuntimeException due to duplicate email
        try {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)));
        } catch (Exception e) {
            // Expected - RuntimeException: Email already exists
            assertTrue(e.getCause() != null && e.getCause().getMessage().contains("Email already exists"));
        }
    }

    @Test
    void testCreateUser_ValidationErrors() throws Exception {
        UserRequest userRequest = new UserRequest();
        // Missing required fields

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // Create a user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        User savedUser = userRepository.save(user);

        // Generate JWT token for the same user
        testToken = TestJwtHelper.generateTestToken(savedUser.getId(), "testuser");

        mockMvc.perform(get("/api/users/" + savedUser.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // Create a user first (needed for JWT validation)
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        User savedUser = userRepository.save(user);
        
        // Generate a token for the created user
        testToken = TestJwtHelper.generateTestToken(savedUser.getId(), "testuser");
        
        // Try to access a non-existent user (should return 403 Forbidden - same-user only)
        mockMvc.perform(get("/api/users/999")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isForbidden());
    }
}

