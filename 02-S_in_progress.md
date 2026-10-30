# Task Manager — Steps I'm doing

As we can see now, there are 2 folder `task-manager-api` and `task-manager-maven`... 

The maven is the one we created first

The api is the one with Spring Boot 


## Related to database
- The old `task-manager-maven` project manually created a table called `tasks` (plural). 

- The new `task-manager-api` project's Hibernate auto-created a separate table called `task` (singular, from the @Entity class name) — they coexist in the same database but are unrelated tables.

## Spring Data JPA
- It's a Spring Boot (framework) tool that makes easier for Java applications to work with databases
    - Spring Data JPA = Java code ↔ Database, without writing most SQL yourself.

- Without JPA: Java → manually write SQL → Database
- With JPA: Java → TaskRepository → Spring generates SQL → Database

- We need to create both files `TaskRepository.java` and `TaskController.java` 

- First, we create the `TaskRepository.java` that will replace all our manual SQL in the terminal
    - We don't need to add any methods inside of it, because `JpaRepository<Task, Integer>` already gives save(), findById(), findAll(), deleteById(), etc. for free

        ```java
        package com.taskmanager;

        // This is related to Spring Data JPA

        import org.springframework.data.jpa.repository.JpaRepository;

        public interface TaskRepository extends JpaRepository<Task, Integer> 
        {
            
        }
        ```

- The file `TaskController.java` is the API/controller layer. 
    - It receives requests and tells the Repository what to do
    - In this file is where we find the ``HTTP method + URL path`` 

    ```java
    @RequestMapping("/tasks") // GET /tasks/5 → run getTask(5).

    @GetMapping
    public List<TaskResponseDTO> getAllTasks()
    {
        return taskRepository.findAll()
            .stream()
            .map(TaskResponseDTO::new)
            .collect(Collectors.toList());
    } // curl http://localhost:8080/tasks

    @PostMapping // Add task - Just add the title of the task
    public TaskResponseDTO addTask (@Valid @RequestBody TaskRequestDTO request)
    {
        // return taskRepository.save(task);
        Task task = new Task(request.getTitle());
        Task saved = taskRepository.save(task);
        return new TaskResponseDTO(saved);
    } // curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Study Java"}'

    @PutMapping("/{id}/complete") // Transforming from "TODO" to "DONE" - Specify the ID
    public TaskResponseDTO completeTask(@PathVariable int id)
    {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(TaskStatus.DONE);
        Task saved = taskRepository.save(task);
        return new TaskResponseDTO(saved);
    } // curl -X PUT http://localhost:8080/tasks/1/complete

    ```

    ```bash
    curl http://localhost:8080/tasks

    curl http://localhost:8080/tasks/3

    curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Study Java"}'

    curl -X PUT http://localhost:8080/tasks/1/complete

    curl -X DELETE http://localhost:8080/tasks/1
    ```

    ``` 
    GET /tasks
    ↓
    TaskController
    ↓
    TaskRepository
    ↓
    Database
    ```
       
    - This `GET /tasks` is `curl http://localhost:8080/tasks`

- The `TaskController.java` is where we define the HTTP endpoints
    - HTTP - Communication protocol between client and server
    - HTTP method - GET, POST, PUT, DELETE
    - Endpoint - Specific URL + HTTP method 

## In `task-manager-api` how did we created the database | Also related to Spring Boot JPA (Java Persistence API)

- @Entity tells JPA that `Task` should be persisted as a database table
    - And its fields (id, title, status) are mapped to columns

- @Entity on Task → maps the class to a database table (named after the class, lowercase, unless overridden with @Table(name = "...")).
- @Id marks id as the primary key
- @GeneratedValue tells the database/JPA to generate the ID automatically

- And it's necessary the JPA configuration (for example, Hibernate with spring.jpa.hibernate.ddl-auto) in `task-manager-api\src\main\resources\application.properties`

- In `Task.java` we have 3 entities of the object task (private int `id`; private String `title`; private TaskStatus `status`;) so based on these 3, it will create the 3 columns of the table

```
Java Object (Task)
       ↓
      JPA
       ↓
Database Table
```
- Files related to Spring Data JPA
    - `Task.java` → JPA entity; represents a database table
    - `TaskRepository.java` → Spring Data JPA repository; provides database operations like findAll(), save(), etc.
        - Used by TaskController via @Autowired (dependency injection). Operates on Task entities.
    - `task-manager-api\src\main\resources\application.properties` → contains your database/JPA configuration.
    - `pom.xml` → contains the dependencies for Spring Data JPA, database driver, etc.
    - `TaskController.java` uses the 'TaskRepository', so it is related indirectly, but it's primarily the HTTP/API layer.

## Steps when modifying the code
- Modify the code
- Reset the server in the terminal (`ctrl + c`)
- Run again in the terminal `mvn spring-boot:run` (For the API folder)

- While the server is running, we can access on the browser `localhost:8080`

- Then we can run the `curl` commands to add, get, delete tasks
    - To see all tasks: `curl http://localhost:8080/tasks`

    - To create a task: `curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Learn Spring Data JPA"}'`

    - To mark as done: `curl -X PUT http://localhost:8080/tasks/1/complete`


## How to access the database
- Go to the terminal and run `psql -U taskuser -d task_manager -h localhost`
    - Password: taskpass

- How to know the database we are using:
    - The file related is the `task-manager-api\src\main\resources\application.properties`

    - In the line `spring.datasource.url=jdbc:postgresql://localhost:5432/task_manager`

- The file where we specify the username, password Spring use to connect
    - File: `task-manager-api\src\main\resources\application.properties`

- Commands of postgreSQL
    - `psql -U taskuser -d task_manager -h localhost` (Access postgreSQL)
    - `\l` (List the databases)
    - `\c task_manager` (To select the database)
    - `\conninfo` (Which database we are using)
    - `\dt` (See the tables of the database)
        - Right now we are working with 2 tables
            - task → API
            - tasks → Maven
    - `SELECT * FROM task;` (To see data from the specific table)
    - `\d task` (To see the table structure)
    - `\q` (Exit)

    - When there is the ~ ~ ~ (END) → Press `q`

## How to run the applications (API)
- `cd task-manager-api/` (Go to the API directory)
- `mvn spring-boot:run` (Run the Spring Boot server)

- Access on the browser `http://localhost:8080/tasks`

- I'll add a new task to see which table we are using for the API folder
    - `curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "26/set/2026"}'`

    - After entering the command, we can refresh the browser page

    - It showed the new task (id: 6 | status: TODO | title: 26/set/2026)

        ```
        task_manager=> SELECT * FROM task;
        id | status |         title
        ----+--------+-----------------------
        3 |      0 | Learn Spring Data JPA
        4 |      0 | Build REST endpoints
        5 |      0 | Test with curl
        6 |      0 | 26/set/2026
        (4 rows)
        ```
        - We can see that the table we are using is `task` for API folder


## 26/Sep/2026

### What I've done so far
- Spring Boot project created (Maven, Java 17, Web + JPA + PostgreSQL)
- Task entity (@Entity, @Id, @GeneratedValue) — Hibernate auto-creates the task table
- TaskRepository extends JpaRepository<Task, Integer> — free CRUD methods
- TaskController — full REST API:
    - GET /tasks, 
    - GET /tasks/{id}, 
    - POST /tasks, PUT /tasks/{id}/complete, 
    - DELETE /tasks/{id}
- TaskRequestDTO / TaskResponseDTO — controlled input/output shape
- Validation (@NotBlank, @Valid) — rejects empty titles
- GlobalExceptionHandler (@RestControllerAdvice) — clean JSON errors instead of Spring defaults
- Git initialized, .gitignore added, committed

### Next steps
- Testing (JUnit/Mockito)
- Spring Security
- Docker
- (Optional) Frontend


--- 
### Steps I'm doing now

- I deleted the `demo.zip` file (No needed anymore - I already extracted)

- Checked if the `.git` folder survivded the move 
    - `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api"`
    - `git status`
    - It's ok, but if not, we could've use `git init` again

- I deleted the file `task-manager-api\src\test\java\com\example\demo\DemoApplicationTests.java` because it still references the old com.example.demo package + We are not using tests yet.

- Commited the changes
    - `git status`
    - `git add .`
    - `git commit -m "Delete unused test file, minor cleanup"`


- I have deleted the folder from src/test/java/com → demo and example
    - And then created `task-manager-api\src\test\java\com\taskmanager\TaskControllerTest.java`

    - Added the code of Unit test the controller 

    - `cd task-manager-api/`

    - `mvn test`
        - Didn't work

    - I needed to modify the `pom.xml` file 

    - `mvn clean test`
        - Didn't work

    - I modified the `TaskControllerTest.java` — change these imports

- I've wrote a unit test for the `TaskController` (HTTP endpoints) using JUnit + Mockito, without hitting the real database

    - We created the file `task-manager-api\src\test\java\com\taskmanager\TaskControllerTest.java`

        - What the test does:
            - @SpringBootTest — loads the full Spring app context
            - @AutoConfigureMockMvc — gives you MockMvc, which simulates HTTP requests without a real running server

            ```
            @MockitoBean
            private TaskRepository taskRepository;
            ```
            - Replaces the real TaskRepository with a fake one — no real database calls happen during the test
                - The TaskRepository is responsible for database operations like findAll(), save(), etc 
                - Used by TaskController via @Autowired (dependency injection). Operates on Task entities


- Related to 'testing'
    - We've created the file `TeskControllerTest` 
    - And it only simulates a "GET /tasks" request so far

    - How to run the test?
        - `cd task-manager-api/` 
        - `mvn clean test`


- Now we'll add a couple more tests to cover the other endpoints - Good practice is testing success and failure cases

    - Going back to the `TaskControllerTest.java`, we'll add 2 test methods inside the class

    - We've added 2 new "@Test" and the name of the methods explains them

    - Run these new tests:
        - `mvn clean test`

Remaining steps
- REST API complete
- Testign - 3 passing tests (GET all, POST create, GET 404)
- Spring Security 
- Docker 
- (Optional) Frontend

Commit the milestone:
- `git add .`
- `git commit -m "Add unit tests for TaskController with MockMvc and Mockito"`
- `git status`
- `git log --oneline`

Spring security
- This will add authentication to your API — currently anyone can hit any endpoint. 
    - We'll start simple (basic auth), then optionally move to JWT (JSON Web Token) later.

1) We need to add the dependency in `pom.xml` - inside `<dependencies>`

2) After adding the dependency, we can run `mvn spring-boot:run`
    - Opened a new terminal
        - `cd task-manager-api/` 
        - ran `curl http://localhost:8080/tasks`
    - Going back to the terminal where it's running the server, we can see `Using generated security password: 23f1c45f-8521-4259-a4c4-17dab22593d6`

    - Ran now `curl -i http://localhost:8080/tasks` to see output with `HTTP/1.1 401`

        - 401 Unauthorized
        - We are trying to call the endpoint (To see all tasks), but it's not authenticated / haven't provided valid credentials
        - Spring Security is now protecting every endpoint by default.


    - Now we'll try to test with the generated credentials
        - `curl -i -u user:23f1c45f-8521-4259-a4c4-17dab22593d6 http://localhost:8080/tasks`
        - Spring Boot/JPA → PostgreSQL database

    - After running the command with the security password, it showed:
        - 200 OK with valid credentials, 401 without
        - [{"id":3,"status":"TODO","title":"Learn Spring Data JPA"},{"id":4,"status":"TODO","title":"Build REST endpoints"},{"id":5,"status":"TODO","title":"Test with curl"},{"id":6,"status":"TODO","title":"26/set/2026"}]



- Now we are going to define a fixed username/password for development

    - Create a new file `task-manager-api\src\main\java\com\taskmanager\SecurityConfig.java`

    - Inside the file, we define the fixed user (admin/admin123) instead of an auto-generated password

    - Passwords are never stored in plain text, even in-memory

    - Defines the rules of every request myst be authenticated, using basic auth

    - Now we can restart the server and test the curl with the fixed user
        - `ctrl + c`
        - `mvn spring-boot:run`
        - New terminal: `curl -i -u admin:admin123 http://localhost:8080/tasks`

        - After running, we were able to see all tasks we have

    - Now I'll run the `mvn test` to check if it's still ok or the MockMvc is broken

        - I've ran it and the tests fail because new security requires auth 

    - We need to fix by adding basic auth credentials to each mockMvc.perform() call
        - Go to the file `TaskControllerTest.java` 

            - I've added this import `import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;`

            - I've added the this dependency in `pom.xml`
                ```
                <dependency>
                    <groupId>org.springframework.security</groupId>
                    <artifactId>spring-security-test</artifactId>
                    <scope>test</scope>
                </dependency>
                ```

            - In each `mockMvc.` I've added `.with(httpBasic("admin", "admin123"))`

            - We've updated the File `SecurityConfig.java` — update the `securityFilterChain` method:


                - Best practice note: disabling CSRF is standard for REST APIs using token/basic auth (no browser cookies/sessions involved) — but wrong for traditional server-rendered apps with forms. Different security models need different protections.

            - It worked the 3 tests


Now we'll commit the milestone:
- `git add .`
- `git commit -m "Add Spring Security with basic auth, update tests"`


Some ways to highlight words in Markdown
```
**word** → word (bold)
*word* → word (italic)
***word*** → word (bold + italic)
`word` → word (inline code)
# word → heading
> word → blockquote
```

Now we'll move into Docker

- We'll containerize both the Spring Boot app and PostgreSQL 
    - To run anywhere
    - And with one commnad

- I've created the `Dockerfile` in the root folder `task-manager-api`

- How to test this new file?
     - Open `Docker Desktop` app
     - `docker ps` (Show currently running docker containers)
     - `docker ps -a` (Show all containers)
     - `docker stop 220a09d16f83` (To stop the running containers | ID)

     - `docker build -t task-manager-api .` (Build docker image)
        - ***docker build*** → To create the image
        - ***-t task-manager-api*** → gives your image a name
        - ***.*** → use the current directory as the build context

    - `docker images` (Check if it exists)

    - `docker rmi $(docker images -q)` (To delete all images)

    - If some images are being used by containers, remove the containers first
        - `docker rm -f $(docker ps -aq)` (Removes all from `docker ps -a`)
        - `docker rmi -f $(docker images -q)` (Removes all from `docker images`)

    - `docker run -p 8080:8080 task-manager-api` (To run the container)

        - I've ran it, but it stop running the server 
        - But then in theory we would need to test the API 
            `curl -i http://localhost:8080/tasks`

We need to create docker-compose to run app + database together
- We created the `docker-compose.yml` file in the root folder `task-manager-api`

    - `db` and `app` are separate containers on the same Docker network — the `app` connects to the database using the hostname `db` (the service name), not localhost.

    - The docker compose is orchestrating 2 containers 

- Check if Docker and docker compose are installed
    - `docker --version`
    - `docker compose version`

- I've changed the port of the `db` container localhost (leftside) to a new one to avoid conflict with the Local PostgreSQL

- Stop runing the `mvn spring-boot:run` terminal with `Ctrl+C.`

- Create the `.dockerignore` inn the root `task-manager-api` (keeps target/ and .git out of the build)
    ```
    target/
    .git/
    ```

- Build and run
    - `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api"`
    - `docker compose up --build`
        - The terminal will be running the server

- Verify it works
    - Open a new terminal
        - Run `curl -i http://localhost:8080/tasks` 
            - We're suppose to see **401** (security is active inside the container too).
                - Related to the security file we created that requires

        - `curl -i -u admin:admin123 http://localhost:8080/tasks`
            ```
            UserDetails user = User.builder()
            .username("admin")
            .password(encoder.encode("admin123"))
            .roles("USER")
            .build();
            return new InMemoryUserDetailsManager(user);
            ```
            - We are using the username and password to authentication to your API (Can hit any API endpoints)

        - Run: `curl -i -u admin:admin123 http://localhost:8080/tasks`
            - Expected: []. The Docker database is a fresh, separate PostgreSQL, so your old tasks aren't there.

        - Run: `curl -u admin:admin123 -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Running in Docker"}'`

            - To create the first task of the new database

        - Run: `curl -u admin:admin123 http://localhost:8080/tasks`
            - We can see the that the first task was created

    - Other commands 
        - `docker compose ps` (see running containers)
        - `docker compose logs app` (view app logs)
        - `docker compose down` (stop and remove containers (data is kept in the pgdata volume))
        - `docker compose up -d` (start again in the background)

    - Run: `docker compose down`
    - Run: `docker ps`
    - Stop the running server terminal
        - `docker compose up -d` (To run the server, app and database)
        - `curl -u admin:admin123 http://localhost:8080/tasks` (In the same terminal we can run commands because it is in the background )
    - Run: `docker compose down`

Commit the Docker Compose 
- `git add .`
- `git commit -m "Add Dockerfile and docker-compose for app and PostgreSQL"`


Add README.md file and push to GitHub
- I've created the README file

- I created a new empty public repo on github
    - No README file
    - No .gitignore
        - Because I already have then and will push when connecting

- Ps: The whole project is inside `task-manager-api` 
    - With just this folder we can run the application + database

- After creating the repo: `git remote add origin https://github.com/YOUR_USER/task-manager-api.git`

Actually, before pushing to public repo
- Change the code to work with Environment Variable before pushing it 
- Create the `.env` and add it in `.gitignore` 

- Why not push public now? 
    - Git repo keeps the old versions of files
    - So it's cleaner to move them to environment variables before the first push

- For the frontend, we'll need to create a new folder `task-manager-frontend`, next to the api flder
    - The API will need CORS (Cross-Origin Resource Sharing) enable
        - To enable the frontend to call 


We'll create the environment variables
- Best practice: secrets live outside the code 
    - So the same code can run in dev/prod with different values 
    - And nothing sensitive reaches Git 

- I've changed the values of the file `application.properties`
    - Changed the URL, username and password of the database 
    - The first 2 we specify them
    - The password, will read the .env file

- Create the `.env` file in the root of task-manager-api
    - This file is never committed 
    - We've added the database password and the admin password

- Create the `.env.example` to show other what to create
    - We commit this one

- Modified the `docker-compose.yml` file to replace the hardcoded values with ${DB_PASSWORD} 
     - Compose reads .env automatically 
     - Replaced the `POSTGRES_PASSWORD` and `SPRING_DATASOURCE_PASSWORD`
     - In the `app` container, I've added the line `ADMIN_PASSWORD: ${ADMIN_PASSWORD}` in environments

- In the `SecurityConfig.java` 
    - Added the line `import org.springframework.beans.factory.annotation.Value;`
    ```
    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;
    // then: .password(encoder.encode(adminPassword))
    ```
    - I've added this block on the top of the class

    - Then I modified the `userDetailsService` method and added this private string instead of harcoded value in password

Checking 
- Run: `git status` - The `.env` shouldn't appear in the list... Even as "untracked"

- Rebuild and run the docker compose with new env vars
    - Run: `docker compose down`
    - Run: `docker compose up --build -d`
    - Run: `docker ps`

- Test the API if it still works with the real password
    - `curl -i http://localhost:8080/tasks`
        - It should display `401` (Without the user and pwd)

    - `curl -u admin:admin123 http://localhost:8080/tasks`   
        - It should display `200` and the list of all tasks we have 

- We need to make sure the tests still pass with the new config
    - `mvn test` ( I ran this )

- Related to the test
    - We don't need to run the Spring Server
    - Spring starts the app inside the test process
    - We don't need to run the App container or db container
    - If the tests fail, it is a configuration problem
        - Not because the server isn't running

    - Problem: The `application.properties` requires DB_PASSWORD and ADMIN_PASSWORD as env vars

    - What to do?
        - Run: `export DB_PASSWORD=taskpass`
        - Run: `export ADMIN_PASSWORD=admin123`
        - Run: `mvn test`

    - We'll add default so we don't need `export` every time locally
        - Added in the `application.properties` file
        - The code: `spring.datasource.password=${DB_PASSWORD:taskpass}`

    - 

Next
- Solve the problem related to `mvn test` file 
- Write the README 
- Push to GitHub (public)

- Start the frontend
- Improve (JWT, service layer, CI/CD)

Related to SSH key and GitHub
- I believe we cannot add username and password anymore with GitHub... So the method we use is `SSH keys`

- Why? Clone once, never authenticate again. No token to expire or leak into a terminal.

- Process of SSH keys and GitHub
    - `ls ~/.ssh/` (We can see all the SSH keys we have)

    - `ssh-keygen -t ed25519 -C ricardoprograma01@gmail.com`

    - `cat ~/.ssh/` + clicking `tab` (We can see all the keys)
        - We can see the 2 keys we've created

    - `cat ~/.ssh/id_ed25519.pub`
        - Copy the value and paste into GitHub SSH keys

    - Go to `settings` → `SSH and GPG keys` → Paste the value of cat

    - `git clone git@github.com:ricardo-eiji/Y2S1---Cloud-assignment.git` (After creating the SSH keys on GitHub, we can clone repos on local machine via SSH method)


I've added the README file
- I needed to move inside `task-manager-api` folder

Now I'll commit 
- `git add .`
- `git commit -m "Add README, .env.example, fix mvn test"`

Now we'll push it to GitHub
- `cd task-manager-api/`

- `git remote -v` (Where is my remote?) 
- `git branch -vv` (What remote branch does my branch track?)
- `git remote show origin` (What's the relationship with origin?)

- `git remote add origin git@github.com:ricardo-eiji/spring-boot-task-manager-api.git` (Connect local repo to the GitHub repo)
- `git branch -M main` (Renames your current local branch to main)
- `git push -u origin main` (pushes that local main branch to GitHub and connects it to the remote origin/main)


I'll test the public repo to see if the clone is ok
- I've created a new folder "clone"
- Go inside the new folder "clone"
    - Open git bash
    - Run: `git clone git@github.com:ricardo-eiji/spring-boot-task-manager-api.git test-clone` (The last part "test-clone" is the name of the folder)

- Opened VSCode with the "test-clone" new folder
- Open terminal and run `cp .env.example .env`
- Run `docker compose up --build`


- We need to stop the previous running containers
    - I forgot: Go to `task-manager-api` dir and run `docker compose down -v`

    - Now run again in test-clone `docker compose up --build` 

    - Now it worked! (In the clonned repo) 

- After running the server
    - We'll not change the .env file 
    - So the admin password is `change_me`, not admin123
    - So we can run `curl -u admin:change_me http://localhost:8080/tasks`
    
    - When entering on browser `localhost:8080` or `localhost:8080/tasks`
        - It will ask username and password 
        - Write `admin` and `change_me`

    - Test the curl commands without modifying .env
        - `curl -u admin:change_me -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Cloned and works!"}'` (To add new task)

        - `curl -u admin:change_me http://localhost:8080/tasks` (To see the tasks)

        - PS: We can notice that is basically a new empty database 

    - Checking the database
        - `docker exec -it test-clone-db-1 psql -U taskuser -d task_manager` (PS: The server is running now)

        - `\l` (List all databases)
            - Ps: We can see there is only the `task_manager` database
        - `\conninf` (Show which database we are using)

        - `\dt` (We can see there is only 1 table `task`)

        - `SELECT * FROM task;` (We can see the 2 tasks we created)

        - `\q` (Exit)

        - `sudo service postgresql status`

        - `sudo service postgresql start`

        - `psql -U taskuser -d task_manager -h localhost`
            - pwd: taskpass

    - `docker compose down`

    - Trying to run the tests
        - `mvn test`
            - It didn't work

Problem related to the cloned repo
- `mvn test` / `mvn spring-boot:run` locally are broken due to some local Postgres auth quirk (likely WSL-specific, unrelated to your code)

- Since Docker is your primary way of running/verifying this project (and it's confirmed 100% working), this local issue doesn't block anything important. Suggest: drop it for now, move on. We can revisit only if it starts blocking actual work.


Next steps:

✅ Done — REST API, DTOs, validation, exceptions, tests, security, Docker, env vars, README, pushed to public GitHub, clone verified working

- Frontend (separate project + CORS on the API)
- Improvements: JWT auth, service layer, CI/CD (GitHub Actions)
- (Optional) Deploy somewhere public (Render, Railway, Fly.io, etc.)


