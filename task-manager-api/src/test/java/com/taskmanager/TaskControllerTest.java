package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

// Goal: Write a unit test for TaskController (With the HTTP endpoints) 
// using JUnit + Mockito, without hitting the real database

@SpringBootTest // Loads the full Spring app context
@AutoConfigureMockMvc // Gives MockMvc, which simulates HTTP requests without a real running server
public class TaskControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskRepository taskRepository;
    // Replaces the real TaskRepository with a fake one 
    // No real database calls happen during the test

    @Test
    public void shouldReturnAllTasks() throws Exception
    {
        Task task = new Task("Study Java");
        Mockito.when(taskRepository.findAll()).thenReturn(List.of(task)); // Tells the fake repository: "When findAll() is called, pretend it returned this one task"

        mockMvc.perform(get("/tasks").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Study Java"));
        // Simulates a GET /tasks request
        // Asserts: response status is 200 OK, and the first item's title in the JSON response is "Study Java"
    }

    @Test
    public void shouldCreateTask() throws Exception
    {
        Task task = new Task("New Task");
        // Create the Task we expect to be returned

        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);
        // The Mockito.any(Task.class) — matches any Task object passed to save(), 
            // since we don't care about the exact instance
        // When save() is called with any Task, return our task

        mockMvc.perform(post("/tasks") // Simulates POST /tasks with a JSON body
                .with(httpBasic("admin", "admin123"))
                .contentType("application/json") // Tell the server we're sending JSON
                .content("{\"title\": \"New Task\"}")) // Tell JSON data we're sending
            .andExpect(status().isOk()) // Expect HTTP 200 OK
            .andExpect(jsonPath("$.title").value("New Task")); // Expect the response JSON to contain title "New Task"

        // Simulates the HTTP request we'd make from a client/terminal: POST /tasks with JSON body
    }

    @Test
    public void shouldReturn404WhenTaskNotFound() throws Exception
    {
        Mockito.when(taskRepository.findById(999)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/tasks/999").with(httpBasic("admin", "admin123")))
            .andExpect(status().isNotFound());

        // Second test verifies your GlobalExceptionHandler 
            // correctly returns 404 for a missing task
    }
}