
package com.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import jakarta.validation.Valid;

import java.util.List;


@RestController
@RequestMapping("/tasks") // GET /tasks/5 → run getTask(5).

// Responsible for the HTTP endpoints (Methods + URL)

public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @GetMapping // Get all tasks 
    public List<TaskResponseDTO> getAllTasks()
    {
        return taskRepository.findAll()
            .stream()
            .map(TaskResponseDTO::new)
            .collect(Collectors.toList());
    }
    // curl http://localhost:8080/tasks

    @PostMapping // Add task - Just add the title of the task
    public TaskResponseDTO addTask (@Valid @RequestBody TaskRequestDTO request)
    {
        // return taskRepository.save(task);
        Task task = new Task(request.getTitle());
        Task saved = taskRepository.save(task);
        return new TaskResponseDTO(saved);
    }
    // curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Study Spring Boot"}'

    @GetMapping("/{id}") // try to find a specific task via ID
    public TaskResponseDTO getTask(@PathVariable int id)    
    {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        return new TaskResponseDTO(task);
        // return taskRepository.findById(id)
        //     .orElseThrow(() -> new RuntimeException("Task not found"));
    }
    // curl http://localhost:8080/tasks/2

    @PutMapping("/{id}/complete") // Transforming from "TODO" to "DONE" - Specify the ID
    public TaskResponseDTO completeTask(@PathVariable int id)
    {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(TaskStatus.DONE);
        Task saved = taskRepository.save(task);
        return new TaskResponseDTO(saved);
    }
    // curl -X PUT http://localhost:8080/tasks/1/complete

    @PutMapping("/{id}/undo")
    public TaskResponseDTO undoTask(@PathVariable int id)
    {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(TaskStatus.TODO);
        Task saved = taskRepository.save(task);
        return new TaskResponseDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id)
    {
        taskRepository.deleteById(id);
    }
    // curl -X DELETE http://localhost:8080/tasks/1
}
