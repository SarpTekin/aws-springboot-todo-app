package com.microtodo.task_service.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microtodo.task_service.dto.TaskRequest;
import com.microtodo.task_service.model.Task;
import com.microtodo.task_service.repository.TaskRepository;
import com.microtodo.task_service.util.TestJwtHelper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    private String testToken;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        // Generate a test JWT token for user ID 1
        testToken = TestJwtHelper.generateTestToken(1L, "testuser");
    }

    @Test
    void testCreateTask_Success() throws Exception {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setStatus(Task.TaskStatus.PENDING);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void testCreateTask_ValidationErrors() throws Exception {
        TaskRequest taskRequest = new TaskRequest();
        // Missing required fields (title)

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTaskById_Success() throws Exception {
        // Create a task directly in the database (owned by user 1)
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        // Create a task directly in the database (owned by user 1)
        Task task = new Task();
        task.setTitle("Original Task");
        task.setDescription("Original Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus(Task.TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // Create a task directly in the database (owned by user 1)
        Task task = new Task();
        task.setTitle("Task to Delete");
        task.setDescription("Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        assertTrue(taskRepository.findById(savedTask.getId()).isEmpty());
    }

    @Test
    void testGetAllTasks_UserIsolation() throws Exception {
        // Create tasks for user 1
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setUserId(1L);
        taskRepository.save(task1);

        // Create task for user 2 (should not appear in results)
        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setUserId(2L);
        taskRepository.save(task2);

        // User 1 should only see their own tasks
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));
    }
}

