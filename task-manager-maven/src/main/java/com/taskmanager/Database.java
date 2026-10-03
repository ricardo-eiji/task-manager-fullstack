
package com.taskmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/task_manager";
    private static final String USER = "taskuser";
    private static final String PASSWORD = "taskpass";

    public static Connection connect() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
