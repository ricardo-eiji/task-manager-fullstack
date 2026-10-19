package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-test-secret-test-secret-test-secret-test-secret-1234"
})
@AutoConfigureMockMvc
public class TaskControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    private String bearer(String username)
    {
        return "Bearer " + jwtUtil.generateToken(username);
    }

    @Test
    public void shouldReturnOnlyTheCurrentUsersTasks() throws Exception
    {
        Task task = new Task("Study Java");
        Mockito.when(taskRepository.findByOwnerUsernameOrderByIdAsc("alice"))
            .thenReturn(List.of(task));

        mockMvc.perform(get("/tasks").header("Authorization", bearer("alice")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Study Java"));

        // bob has no tasks, and alice's are not returned to him
        mockMvc.perform(get("/tasks").header("Authorization", bearer("bob")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldCreateTaskOwnedByTheCurrentUser() throws Exception
    {
        User alice = new User("alice", "hash");
        Mockito.when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        Mockito.when(taskRepository.save(Mockito.any(Task.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/tasks")
                .header("Authorization", bearer("alice"))
                .contentType("application/json")
                .content("{\"title\": \"New Task\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New Task"));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(taskRepository).save(saved.capture());
        assertSame(alice, saved.getValue().getOwner());
    }

    @Test
    public void shouldReturn404WhenTaskNotFound() throws Exception
    {
        Mockito.when(taskRepository.findByIdAndOwnerUsername(999, "alice"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/tasks/999").header("Authorization", bearer("alice")))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotLetAUserReadAnotherUsersTask() throws Exception
    {
        Task alicesTask = new Task("Alice private");
        Mockito.when(taskRepository.findByIdAndOwnerUsername(5, "alice"))
            .thenReturn(Optional.of(alicesTask));
        Mockito.when(taskRepository.findByIdAndOwnerUsername(5, "bob"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/tasks/5").header("Authorization", bearer("alice")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Alice private"));

        // 404, not 403: the API doesn't confirm the task exists
        mockMvc.perform(get("/tasks/5").header("Authorization", bearer("bob")))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotLetAUserDeleteAnotherUsersTask() throws Exception
    {
        Mockito.when(taskRepository.findByIdAndOwnerUsername(5, "bob"))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete("/tasks/5").header("Authorization", bearer("bob")))
            .andExpect(status().isNotFound());

        Mockito.verify(taskRepository, Mockito.never()).delete(Mockito.any(Task.class));
    }

    @Test
    public void shouldReturn401WithoutToken() throws Exception
    {
        mockMvc.perform(get("/tasks"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401WithInvalidToken() throws Exception
    {
        mockMvc.perform(get("/tasks").header("Authorization", "Bearer not-a-real-token"))
            .andExpect(status().isUnauthorized());
    }
}