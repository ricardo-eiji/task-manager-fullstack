package com.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer>
{
    List<Task> findByOwnerUsernameOrderByIdAsc(String username);
    Optional<Task> findByIdAndOwnerUsername(int id, String username);
}