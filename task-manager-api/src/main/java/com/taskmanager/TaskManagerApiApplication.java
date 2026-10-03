package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// The file that `TaskManagerApiApplication.java` is the `main` of Maven

@SpringBootApplication
public class TaskManagerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApiApplication.class, args);
	}
	// When I run "mvn spring-boot:run" Maven causes the main() to execute

}
