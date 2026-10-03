package com.taskmanager;

import jakarta.validation.constraints.NotBlank;

public class TaskRequestDTO {
    @NotBlank(message = "title cannot be null or empty")
    private String title;
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

}
