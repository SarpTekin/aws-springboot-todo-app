package com.microtodo.task_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microtodo.task_service.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndStatus(Long userId, Task.TaskStatus status);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
