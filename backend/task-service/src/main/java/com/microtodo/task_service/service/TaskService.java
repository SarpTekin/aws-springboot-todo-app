package com.microtodo.task_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microtodo.task_service.dto.TaskRequest;
import com.microtodo.task_service.dto.TaskResponse;
import com.microtodo.task_service.model.Task;
import com.microtodo.task_service.repository.TaskRepository;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;

    public TaskResponse createTask(TaskRequest taskRequest) {
        // Validate user exists by calling User Service
        userServiceClient.getUserById(taskRequest.getUserId());
        
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setUserId(taskRequest.getUserId());
        task.setStatus(taskRequest.getStatus() != null ? taskRequest.getStatus() : Task.TaskStatus.PENDING);
        
        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks(Long userId) {
        List<Task> tasks = (userId != null) ? 
            taskRepository.findByUserId(userId) : 
            taskRepository.findAll();
        
        return tasks.stream()
            .map(this::mapToTaskResponse)
            .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        return mapToTaskResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest taskRequest) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        if (taskRequest.getStatus() != null) {
            task.setStatus(taskRequest.getStatus());
        }
        
        return mapToTaskResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());
        taskResponse.setDescription(task.getDescription());
        taskResponse.setStatus(task.getStatus());
        taskResponse.setUserId(task.getUserId());
        taskResponse.setCreatedAt(task.getCreatedAt());
        taskResponse.setUpdatedAt(task.getUpdatedAt());
        return taskResponse;
    }
}
