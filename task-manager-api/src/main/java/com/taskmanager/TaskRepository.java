

package com.taskmanager;

// This is related to Spring Data JPA

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> 
{
    // Don't need to write the methods because it already have by default
}

// We don't need to add any methods inside of it, because JpaRepository<Task, Integer> already gives save(), findById(), findAll(), deleteById(), etc. for free

// free CRUD methods