
package com.taskmanager;

import java.util.ArrayList;

public interface TaskRepository {
    void addTask(int id, String title);
    void completeTask(int id);
    void removeTask(int id);
    Task findTask(int id);
    ArrayList<Task> findCompletedTasks (TaskStatus status);
    ArrayList<Task> getTasks();
}
