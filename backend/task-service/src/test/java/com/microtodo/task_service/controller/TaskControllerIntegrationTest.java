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
import com.microtodo.task_service.dto.UserDto;
import com.microtodo.task_service.model.Task;
import com.microtodo.task_service.repository.TaskRepository;
import com.microtodo.task_service.service.UserServiceClient;

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

    @MockBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        // Reset mocks before each test
        reset(userServiceClient);
    }

    @Test
    void testCreateTask_Success() throws Exception {
        // Mock user service response
        UserDto mockUser = new UserDto();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userServiceClient.getUserById(anyLong())).thenReturn(mockUser);

        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setUserId(1L);
        taskRequest.setStatus(Task.TaskStatus.PENDING);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void testCreateTask_ValidationErrors() throws Exception {
        TaskRequest taskRequest = new TaskRequest();
        // Missing required fields

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTaskById_Success() throws Exception {
        // Create a task directly in the database
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        // Expect RuntimeException due to task not found
        try {
            mockMvc.perform(get("/api/tasks/999"));
        } catch (Exception e) {
            // Expected - RuntimeException: Task not found
            assertTrue(e.getCause() != null && e.getCause().getMessage().contains("Task not found"));
        }
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        // Create a task directly in the database
        Task task = new Task();
        task.setTitle("Original Task");
        task.setDescription("Original Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Description");
        updateRequest.setUserId(1L);
        updateRequest.setStatus(Task.TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // Create a task directly in the database
        Task task = new Task();
        task.setTitle("Task to Delete");
        task.setDescription("Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        assertTrue(taskRepository.findById(savedTask.getId()).isEmpty());
    }

    @Test
    void testGetAllTasks_NoFilter() throws Exception {
        // Create tasks
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setUserId(1L);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setUserId(2L);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetAllTasks_FilteredByUserId() throws Exception {
        // Create tasks
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setUserId(1L);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setUserId(2L);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));
    }
}

