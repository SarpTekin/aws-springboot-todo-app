package com.microtodo.task_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microtodo.task_service.dto.TaskRequest;
import com.microtodo.task_service.dto.TaskResponse;
import com.microtodo.task_service.dto.UserDto;
import com.microtodo.task_service.model.Task;
import com.microtodo.task_service.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TaskService taskService;

    private TaskRequest taskRequest;
    private Task task;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setUserId(1L);
        taskRequest.setStatus(Task.TaskStatus.PENDING);

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setUserId(1L);
        task.setStatus(Task.TaskStatus.PENDING);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
    }

    @Test
    void testCreateTask_Success() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskResponse response = taskService.createTask(taskRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(1L, response.getUserId());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());
        verify(userServiceClient).getUserById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testCreateTask_UserNotFound() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskRequest);
        });
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testCreateTask_DefaultStatus() {
        // Arrange
        taskRequest.setStatus(null);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskResponse response = taskService.createTask(taskRequest);

        // Assert
        assertNotNull(response);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testGetAllTasks_FilteredByUserId() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUserId(1L);

        when(taskRepository.findByUserId(1L)).thenReturn(Arrays.asList(task, task2));

        // Act
        List<TaskResponse> responses = taskService.getAllTasks(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(taskRepository).findByUserId(1L);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void testGetAllTasks_AllTasks() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUserId(2L);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task, task2));

        // Act
        List<TaskResponse> responses = taskService.getAllTasks(null);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(taskRepository).findAll();
        verify(taskRepository, never()).findByUserId(any());
    }

    @Test
    void testGetTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        TaskResponse response = taskService.getTaskById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
    }

    @Test
    void testGetTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.getTaskById(1L);
        });
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void testUpdateTask_Success() {
        // Arrange
        taskRequest.setTitle("Updated Task");
        taskRequest.setStatus(Task.TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskResponse response = taskService.updateTask(1L, taskRequest);

        // Assert
        assertNotNull(response);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testUpdateTask_NotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(1L, taskRequest);
        });
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void testDeleteTask_NotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(1L);
        });
        verify(taskRepository, never()).delete(any());
    }
}

