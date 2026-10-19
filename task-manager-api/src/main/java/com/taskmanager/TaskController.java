package com.taskmanager;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController
{
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // Only finds the task if it belongs to the logged-in user
    private Task findOwnedTask(int id, Authentication auth)
    {
        return taskRepository.findByIdAndOwnerUsername(id, auth.getName())
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @GetMapping
    public List<TaskResponseDTO> getAllTasks(Authentication auth)
    {
        return taskRepository.findByOwnerUsernameOrderByIdAsc(auth.getName())
            .stream()
            .map(TaskResponseDTO::new)
            .collect(Collectors.toList());
    }

    @PostMapping
    public TaskResponseDTO addTask(@Valid @RequestBody TaskRequestDTO request, Authentication auth)
    {
        User owner = userRepository.findByUsername(auth.getName())
            .orElseThrow(InvalidCredentialsException::new);

        Task task = new Task(request.getTitle());
        task.setOwner(owner);
        return new TaskResponseDTO(taskRepository.save(task));
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getTask(@PathVariable int id, Authentication auth)
    {
        return new TaskResponseDTO(findOwnedTask(id, auth));
    }

    @PutMapping("/{id}/complete")
    public TaskResponseDTO completeTask(@PathVariable int id, Authentication auth)
    {
        Task task = findOwnedTask(id, auth);
        task.setStatus(TaskStatus.DONE);
        return new TaskResponseDTO(taskRepository.save(task));
    }

    @PutMapping("/{id}/undo")
    public TaskResponseDTO undoTask(@PathVariable int id, Authentication auth)
    {
        Task task = findOwnedTask(id, auth);
        task.setStatus(TaskStatus.TODO);
        return new TaskResponseDTO(taskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id, Authentication auth)
    {
        taskRepository.delete(findOwnedTask(id, auth));
    }
}