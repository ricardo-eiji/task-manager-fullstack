
// The idea: 'main' talks to 'manager' instead of directly manipulating the list 
// ArrayList now lives inside 'TaskManager' - not in 'main'
// All logic (add, remove, find) moved into TaskManager's methods
// 'main' just calls 'manager.xxx()'
package com.taskmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;


public class TaskManager implements TaskRepository {
    
    // private ArrayList<Task> tasks_official_list; // Declares a place for the list 

    // public TaskManager() // Constructor (It automatically run when creating the object of the class)
    // {
    //     this.tasks_official_list = new ArrayList<>(); //Creates the empty ArrayList
    // }

    // ------------------------------------------------

    public void addTask(int id, String title)
    {
        if (title == null || title.isEmpty())
        {
            throw new IllegalArgumentException("title cannot be null or empty");
        }

        String sql = "INSERT INTO tasks (title, status) VALUES (?, ?)";

        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, title);
            stmt.setString(2, "TODO");
            stmt.executeUpdate();
        }
        catch (java.sql.SQLException e)
        {
            throw new RuntimeException("Failed to add task", e);
        }
    }

    public void completeTask(int id)
    {
        String sql = "UPDATE tasks SET status = 'DONE' WHERE id = ?";
        
        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            catch (java.sql.SQLException e)
            {
                throw new RuntimeException("Failed to complete task", e);
            }

        // for (Task task : tasks_official_list)
        // { 
        //     if (task.getId() == id)
        //     {
        //         task.setStatus(TaskStatus.DONE);
        //         break;
        //     }
        // }
    }

    public void removeTask(int id)
    {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        catch (java.sql.SQLException e)
        {
            throw new RuntimeException("Failed to remove task", e);
        }

        // Iterator<Task> iterator = tasks_official_list.iterator();

        // while(iterator.hasNext())
        // {
        //     if (iterator.next().getId() == id)
        //     {
        //         iterator.remove();
        //         break;
        //     }
        // }
    }

    public Task findTask(int id)
    {

        String sql = "SELECT id, title, status FROM tasks WHERE id = ?";

        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, id);

            try (java.sql.ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    Task task = new Task(rs.getInt("id"), rs.getString("title"));

                    task.setStatus(TaskStatus.valueOf(rs.getString("status")));

                    return task;
                }
            }
        }

        catch (java.sql.SQLException e)
        {
            throw new RuntimeException("Failed to find task", e);
        }

        throw new TaskNotFoundException(id);

        // for (Task task : tasks_official_list)
        // {
        //     if (task.getId() == id) 
        //     {
        //             return task;
        //     }
        // }
        // throw new TaskNotFoundException(id);
    }

    public ArrayList<Task> findCompletedTasks(TaskStatus status)
    {

        ArrayList<Task> result = new ArrayList<>();
        String sql = "SELECT id, title, status FROM tasks WHERE status = ?";

        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, status.name());

            try (java.sql.ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    Task task = new Task(rs.getInt("id"), rs.getString("title"));
                    task.setStatus(TaskStatus.valueOf(rs.getString("status")));
                    result.add(task);
                }
            }
        }

        catch (java.sql.SQLException e)
        {
            throw new RuntimeException("Failed to find completed tasks", e);
        }

        return result; // ArrayList

        // return tasks_official_list.stream() // We are using 'streams' to process collections - instead of for loop (common in Spring Code)
        //     .filter(task -> task.getStatus() == status)
        //     .collect(Collectors.toCollection(ArrayList::new));

        // ArrayList<Task> result = new ArrayList<>();
        // for (Task task : tasks_official_list)
        // {
        //     if (task.getStatus() == status)
        //     {
        //         result.add(task);
        //     }
        // }
        // return result;
    }

    public ArrayList<Task> getTasks()
    {
        // return tasks_official_list;
        ArrayList<Task> result = new ArrayList<>();
        String sql = "SELECT id, title, status FROM tasks";

        try (java.sql.Connection conn = Database.connect();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = stmt.executeQuery())
        {
            while (rs.next())
            {
                Task task = new Task(rs.getInt("id"), rs.getString("title"));
                task.setStatus(TaskStatus.valueOf(rs.getString("Status")));
                result.add(task);
            }
        }
        catch (java.sql.SQLException e)
        {
            throw new RuntimeException("Failed to fetch tasks", e);
        }

        return result;
    }


}
