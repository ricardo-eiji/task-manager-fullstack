// Task Manager

// import java.util.ArrayList; // They aren't used here anymore
// import java.util.Iterator;

package com.taskmanager;

public class Task_manager {

    public static void main (String[] args)
    {

        try (java.sql.Connection conn = Database.connect())
        {
            System.out.println("Connected to database successfully!");
        }
        catch (java.sql.SQLException e)
        {
            System.out.println ("Connection failed: " + e.getMessage());
        }

        TaskRepository manager = new TaskManager(); // Created the new object 'manager' from class 'TaskManager()'

        // Adding values into the database table

        // manager.addTask(0, "Study Java"); // We are writing the object and then calling the method from the class
        // manager.addTask(0, "Practive SQL");
        // manager.addTask(0, "AWS");


        System.out.println("------------- 1: Display all tasks --------------");

        // Display all tasks
        for (Task task : manager.getTasks()) 
        {
            // System.out.println(task.title); // task.title = "Give me the title of the current Task."
            if (task.getStatus() == TaskStatus.DONE)
            {
                System.out.println( task.getId() + " - " +  task.getTitle() + " - ✅");
            }
            else
            {
                System.out.println(task.getId() + " - " + task.getTitle() + " - ❌");
            }
        }

        System.out.println();


        // for(Task t : manager.findCompletedTasks(TaskStatus.DONE)) 
        // {
        //     System.out.println(t.getTitle());
        // }

        System.out.println("----------- 2: Display message --------------");

        // Using these "try and catch" to test the message 'Error: title cannot be null or empty'
        try
        {
            manager.addTask(1, "");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Error: " + e.getMessage()); // Should display this part
        }

        System.out.println();

        System.out.println("----------- 3: Display message --------------");
        
        // Try and catch to display the message (Task with id 12 not found.)
        try
        {
            Task foundTask = manager.findTask(12); // Shouldn't find 12 -> Go to catch
            System.out.println("Found: " + foundTask.getTitle());
        }
        catch (TaskNotFoundException e)
        {
            System.out.println(e.getMessage()); // Should display this part
        }

        System.out.println();

        // System.out.println("-------------------------");

        // Box<String> box = new Box<>(); // Generics (Using string type)
        // box.set("Hello");
        // System.out.println(box.get());

        // Box<Integer> numberBox = new Box<>(); // (Using Integer type)
        // numberBox.set(42);
        // System.out.println(numberBox.get());

        System.out.println("----------- 4: Sort tasks alphabetically --------------");

        manager.getTasks().stream() // So from the object of the class (manager) we are calling the method to get the tasks, and then .stream() takes the arrayList and creates stream | with stream we can use create a Stream so we can process those tasks with operations like .filter(), .map(), .sorted(), etc
        
        .sorted((a, b) -> a.getTitle().compareTo(b.getTitle())) // lambda is comparing 2 tasks by title
        
        .forEach(task -> System.out.println(task.getTitle())); // lambda replacing a for-loop to print each one

            // The output is in alphabetical order by title

        System.out.println();


        System.out.println("----------- 5: Complete Task --------------");

        manager.completeTask(104); // TODO -> DONE

        Task updateTask = manager.findTask(104);
        System.out.println(updateTask);

        System.out.println();

        System.out.println("----------- 6: Remove task method --------------");

        manager.removeTask(103);

        try 
        {
            Task seeTask = manager.findTask(103);
            System.out.println(seeTask);
        }
        catch (TaskNotFoundException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println();

        System.out.println("----------- 7: Find task method --------------");

        System.out.println(manager.findTask(102));
        System.out.println(manager.findTask(102).toString());

        System.out.println();

        System.out.println("----------- 8: Find completed method --------------");

        manager.completeTask(102);
        manager.completeTask(103);

        for (Task t : manager.findCompletedTasks(TaskStatus.DONE))
        {
            System.out.println(t);
        }

        System.out.println();
    }
}
