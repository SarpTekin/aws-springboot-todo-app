package com.microtodo.task_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microtodo.task_service.dto.TaskRequest;
import com.microtodo.task_service.dto.TaskResponse;
import com.microtodo.task_service.security.CurrentUser;
import com.microtodo.task_service.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        // Get authenticated user ID from JWT token
        Long userId = CurrentUser.getUserId();
        
        // Override userId from request with authenticated user's ID for security
        taskRequest.setUserId(userId);
        
        return ResponseEntity.ok(taskService.createTask(taskRequest));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        // Get authenticated user ID from JWT token
        Long userId = CurrentUser.getUserId();
        
        // Only return tasks for authenticated user
        return ResponseEntity.ok(taskService.getAllTasks(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        TaskResponse task = taskService.getTaskById(id);
        
        // Authorization check: users can only access their own tasks
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Forbidden: Cannot access other user's tasks");
        }
        
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, 
            @RequestBody TaskRequest taskRequest) {
        Long userId = CurrentUser.getUserId();
        
        // Get existing task to check ownership
        TaskResponse existing = taskService.getTaskById(id);
        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("Forbidden: Cannot modify other user's tasks");
        }
        
        // Ensure userId in request matches authenticated user
        taskRequest.setUserId(userId);
        
        return ResponseEntity.ok(taskService.updateTask(id, taskRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        
        // Get task to check ownership
        TaskResponse task = taskService.getTaskById(id);
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Forbidden: Cannot delete other user's tasks");
        }
        
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
