package com.taskmanager;

public class TaskResponseDTO {
    private int id;
    private String title;
    private TaskStatus status;

    public TaskResponseDTO(Task task)
    {
        this.id = task.getId();
        this.title = task.getTitle();
        this.status = task.getStatus();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
}
