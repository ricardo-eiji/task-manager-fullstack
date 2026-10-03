
// This is the class "Task" that is responsible for the object's info (Variables, getter, setter)
// Each Task object manages itself 
// 'TaskManager' manages the collection of tasks. But doesn't define them

package com.taskmanager;

public class Task
{
    private int id;
    private String title;
    // private boolean completed;
    private TaskStatus status;

    public Task (int id, String title)
    {
        if (id < 0)
        {
            throw new IllegalArgumentException("Id cannot be negative");
        }

        if (title == null || title.isEmpty())
        {
            throw new IllegalArgumentException("title cannot be null or empty");
        }
        
        this.id = id;
        this.title = title;
        // this.completed = false;
        this.status = TaskStatus.TODO;
    }

    public int getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    // public boolean isCompleted()
    // {
    //     return completed;
    // }

    // public void setCompleted(boolean completed)
    // {
    //     this.completed = completed;
    // }

    public TaskStatus getStatus()
    {
        return status;
    }

    public void setStatus(TaskStatus status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return id + " - " +  title + " - " + status;
    }
}