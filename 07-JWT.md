## Add dependency in `task-manager-api/pom.xml`  inside <dependencies>:
```
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

## Create a file under task-manager-api/src/.../taskmanager
- It's the JwtUtil
- It generates and validates tokens

```java
package com.taskmanager;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// The file JwtUtil is a class

@Component // Creates an instance of JwtUtil
public class JwtUtil
{
    @Value("${JWT_SECRET}") // It read the value from config/environment
    private String secret; // Store the value in "secret"

    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour
    // How long the token remains valid 

    private SecretKey getKey() // getKey() converts the "secret" string into a cryptographic SecretKey | HMAC algorithm
    {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) // Creates a new JWT(token) for the specified username
    {
        return Jwts.builder()
            .subject(username) // .subject(username) stores the username
            .issuedAt(new Date()) // .issuedAt() records when the token was created
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) //.expiration() sets its expiry time
            .signWith(getKey()) // .signWith(getKey()) signs the token so that its authenticity can later be verified.
            .compact(); // Builds the JWT and returns it as a string (The "String token" above)
    }

    public String extractUsername(String token) // Parses the JWT using the same secret key, verifies its signature, and retrieves the subject—which in this application is the username. If the token is invalid or modified, parsing will fail.
    {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public boolean isTokenValid(String token) // Checks whether the JWT can be successfully parsed and verified. If parsing succeeds, it returns true; if the token is expired, malformed, incorrectly signed, or otherwise invalid, the exception is caught and it returns false.
    {
        try
        {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
```

- getKey() → method
- generateToken() → method
- extractUsername() → method
- isTokenValid() → method

They are methods of the JwtUtil class.

## We need to add in `.env` files (root and task-manager-api folder)
```
JWT_SECRET=change_this_to_a_long_random_string_at_least_32_chars
```


## Add in `application.properties`
```
jwt.secret=${JWT_SECRET}
```

## Modified the code in `JwtUtil.java`
- Instead of `@Value("${JWT_SECRET}")`
- We modified to `@Value("${jwt.secret}")`

## Confirm it compiles 
- `cd task-manager-api/`
- `mvn compile` mvn compile runs from the project with the pom.xml

- Expected "BUILD SUCCESS"

## Plan and next

Done
- Add JWT library to pom.xml
- Create JwtUtil.java — generates and validates tokens

Next:
- Create AuthController.java — /auth/login endpoint, returns a token
- Create JwtFilter.java — checks the token on every request
- Update SecurityConfig.java — use the filter instead of Basic Auth
- Frontend: login form, store token, send it with every 

## Creates AuthController.java - Login endpoint
- Create file in `task-manager-api`

```java
package com.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController // Mark this class as a REST API controller
@RequestMapping("/auth") // All endpoints in this class start with /auth
public class AuthController
{
    @Autowired
    private JwtUtil jwtUtil; // Injects JwtUtil to create JWT tokens 

    @Value("${admin.username:admin}")
    private String adminUsername; // Gets admin username; defaults to "admin"

    @Value("${ADMIN_PASSWORD}") 
    private String adminPassword; // Gets admin password from config/env

    public record LoginRequest(String username, String password) {}
        // Represents the username and password sent during login
    public record LoginResponse(String token) {}
        // Represents the response containing the JWT token

    @PostMapping("/login") // Handles POST request to /auth/login
    public LoginResponse login(@RequestBody LoginRequest request)
    {
        // Checks whether the provided username and password are correct
        if (!request.username().equals(adminUsername) || !request.password().equals(adminPassword))
        {
            throw new RuntimeException("Invalid credentials"); // Reject login if credentials are incorrect
        }

        String token = jwtUtil.generateToken(request.username());
            // Generate a JWT token using the username

        return new LoginResponse(token);
            // Return the JWT token to the client
    }
}
```
- This file creates the login API endpoint for the app
    - User sends their username and password to POST /auth/login

    - The controller (Which is the whole AuthController class) checks them against the configured admin credentials

    - If they are correct, it uses JwtUtil to generate a JWT token and returns that token to the user. If the credentials are incorrect, it returns an error.

- This endpoint must be public (no auth required to log in) — we'll configure that in SecurityConfig next, along with the filter that checks the JWT on every other request.

- The basic flow is: receive login → check credentials → generate JWT → return token.
    - Each successful login creates a new JWT token based on the username
    - The token is newly generated and valid for 1 hour.
    - Token is returned to the client (browser/frontend)
    - The server doesn't need to store the token
    - The client saves it and sends it with future requests, typically in an Authorization: Bearer <token> header. 
        - The server then verifies the token's signature and checks its expiration time. After 1 hour, the expiration time has passed, so the server rejects it as expired.

### Let's run again to confirm it builds
- `cd task-manager-api/`
- `mvn compile`

- It's ok, now we'll focus on filter (Checks the JWT on every other request) + SecurityConfig Update (The more involved part)

## Related to JwtFilter.java

### Before, we need to change the root compose.yml

```yml
app:
    build:
      context: ./task-manager-api
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/task_manager
      SPRING_DATASOURCE_USERNAME: taskuser
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      JWT_SECRET: ${JWT_SECRET} # Will come from .env
```
- I've added the JWT_SECRET in the environment of app service

### Add the same environment in the task-manager-api compose file 
- I've added the `JWT_SECRET: ${JWT_SECRET}`

- Both folders (root and task-manager-api) should have the 32 character in `.env` file
    - The JWT_SECRET = 32 char

### Create the JwtFilter.java file (task-manager-api)

```java
package com.taskmanager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter
{
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException
    {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer "))
        {
            String token = header.substring(7);

            if (jwtUtil.isTokenValid(token))
            {
                String username = jwtUtil.extractUsername(token);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
```
- On every request it looks for [Authorization: Bearer <token>.]
    - If the token is valid, it tells Spring Security "this request is authenticated as this user"
    - If not, it does nothing, and Spring Security rejects the request later

### Replace/Update the file `SecurityConfig.java` (task-manager-api)

```java
// Before
package com.taskmanager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter
{
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException
    {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer "))
        {
            String token = header.substring(7);

            if (jwtUtil.isTokenValid(token))
            {
                String username = jwtUtil.extractUsername(token);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
```

```java
//Now
package com.taskmanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig
{
    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   JwtFilter jwtFilter) throws Exception
    {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- What changed?
    - Removed the in-memory user and PasswordEncoder, because AuthController now checks credentials
    - Removed basic auth
    - It's stateless (The server keeps no session)
        - The token is the only proof of identity 
    - /auth/** is public so you can log in without already being logged in.
    - The HttpStatusEntryPoint returns a clean 401 instead of Spring's default 403 for unauthenticated requests.
    - setAllowCredentials is gone, since a Bearer token in a header doesn't need it.

### Create InvalidCredentialsException.java
- It's a specific exception

```
package com.taskmanager;

public class InvalidCredentialsException extends RuntimeException
{
    public InvalidCredentialsException()
    {
        super("Invalid credentials");
    }
}
```

### In the AuthController.java we need to replace
- I've replaced the `throw new RuntimeException("Invalid credentials");`
    - To `throw new InvalidCredentialsException();`


### In GlobalExceptionHandler.java add this handler inside the class:

```
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler (RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex)
    {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex)
    {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
```
- Added the last `ExceptionHandler(InvalidCredentialsException.class)`
- Spring picks the most specific handler, so this one wins over the generic RuntimeException one.

## Test it (The JwtFilter.java)
- We'll use Docker 

- Open the docker desktop

- At root:
    - `docker compose down`
    - `docker compose up --build -d`

- Problem when running docker compose
    - JwtFilter.java currently contains the SecurityConfig class (probably the SecurityConfig code got pasted into it). 
    - So the class exists twice, and JwtFilter doesn't exist at all.


### Replace JwtFilter.java

- About the new code:
    - Then check that SecurityConfig.java really contains the SecurityConfig code (the version with securityFilterChain and corsConfigurationSource). Each file should hold only the class matching its name.

- `mvn compile` It catches this kind of error in seconds

- Rebuild
    - `cd ..`
    - `docker compose down`
    - `docker compose up --build -d`

- Test the docker:
    - Open new terminal
    - `docker ps`
    - `curl -i http://localhost:8080/tasks` No token -> Expect 401

    - `curl -i -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"wrong"}'` Wrong password → expect 401 with {"error":"Invalid credentials"}

    - `curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'` Correct login → returns {"token":"..."}

    - `curl -i -H "Authorization: Bearer PASTE_TOKEN_HERE" http://localhost:8080/tasks` Use the token → expect 200
        - `curl -i -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc5MTUyNzU2NiwiZXhwIjoxNzkxNTMxMTY2fQ.l0utM9M4OKSU7q8tfnbvfgqy7mdAkxjFBuFeFgRTlwu0cIQYMyDDsjKQiDHF4K4k" http://localhost:8080/tasks` it worked with token value

    - Browser: http://localhost:5173/ - Can't create the tasks (We need to create the Login.jsx)

- Info: 
    - We have the part of the backend, with database
    - But then we need to update the frontend to connect with the backend code

## After the 4 curl tests passed (I haven't seen the outputs), here is the frontend side.

- Create `task-manager-frontend/src/Login.jsx`
    - Replace the `app.jsx`
         - This is running the old backend
         - It sends basic auth (admin:admin123) - And it's hardcoded...
         - But now we are working with JWT

- We've created the `task-manager-frontend/src/Login.jsx`

- We've replaced the code of `task-manager-frontend/src/App.jsx`

- What changed?
    - No credentials in the source anymore
        - The password is typed by the user and sent once, to /auth/login.
        
    - `apiFetch` (In `App.jsx`) is one wrapper that adds the Bearer header to every call
        - Professionals centralize this instead of repeating headers in every handler

    - Automatic logout on 401, so an expired token (1 hour) sends the user back to the login form

    - The token lives in React state (memory), not localStorage
        - The trade-off is that refreshing the page logs you out. The more robust production approach is an httpOnly cookie set by the server, which is a good later improvement.


### Test the `Login.jsx` and the `App.jsx`
- Go to root
- `docker compose down`
- `docker compose up --build -d`

- Browser: http://localhost:5173

    - Testing:
        - Now we can see the login form
        - It shows the red error if wrong password
        - Login: admin and admin123


- How the new login form works?
    - The credentials are defined on the backend

    - The username (admin) is the default in `AuthController` (task-manager-api) in the line `(@Value("${admin.username:admin}")).`

    - And the password (admin123) is whatever we put in ADMIN_PASSWORD in your root `.env`
        - Docker Compose automatically looks for a file named .env in the project directory (normally the same directory as your docker-compose.yml)
        - You don't need to specify the file path explicitly unless you want to use a different environment file.
        - We can change the admin123 to any value in .env

    - When you log in, AuthController compares what you typed against those two values and, if they match, returns a JWT.

- Current situation of the Login
    - We just have 1 hardcoded user 
        - Admin and the password of .env

    - The `admin.username` isn't set anywhere, so `AuthController` falls back to the default `admin`
        - But we can override it with ADMIN_USERNAME variable later

    - A real application stores users in a database table, with passwords hashed using something like BCrypt (never stored in plain text), plus registration and roles. That's a good future improvement, and it fits naturally into the "polish" part of your roadmap.


- How to access the database?
    - At root
    - `docker ps` 3 containers should be running
    - `docker compose exec db psql -U taskuser -d task_manager`
        - `\dt` List tables
        - `SELECT * FROM task;` See tasks
        - `\q`


## Update the backend tests for JWT
- We'll create automated tests in Spring Boot backend to verify if JWT authentication works correctly

- For example, test that logging in with valid credentials returns a token, an incorrect password returns 401 Unauthorized, accessing /tasks without a token returns 401, and accessing it with a valid token succeeds

- These tests help you catch authentication bugs automatically whenever you change the backend, before moving on to database-backed users and AWS deployment.

### Replace the code of  `task-manager-api/src/test/.../TaskControllerTest.java`

- I needed to rewrite the `shouldRejectLoginWithWrongPassword` - Carried an invisible character

### Test the replaced code
- `cd ..` Go to root
- `docker compose down`
- `docker compose up -d db`
- `cd task-manager-api`
- `DB_URL=jdbc:postgresql://localhost:5433/task_manager DB_PASSWORD=taskpass mvn test` It's 5433 because mvn runs in my computer 

    - This command didn't work

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager SPRING_DATASOURCE_USERNAME=taskuser SPRING_DATASOURCE_PASSWORD=YOUR_ACTUAL_DB_PASSWORD mvn test`

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager SPRING_DATASOURCE_USERNAME=taskuser SPRING_DATASOURCE_PASSWORD=taskpass mvn test`
    - This command worked
    - Because you provided Spring Boot with the correct database connection settings directly in the terminal: 
        - SPRING_DATASOURCE_URL pointed to localhost:5433
        - SPRING_DATASOURCE_USERNAME specified taskuser
        - SPRING_DATASOURCE_PASSWORD supplied taskpass

    - Previously, you used DB_URL, which Spring Boot doesn't automatically recognize as its datasource URL unless explicitly configured

    - With the correct settings, Hibernate connected to PostgreSQL, the application context loaded, and all 7 JWT backend tests passed


    Next:
    - Users in database 
    - Current situation: 
        - Hardcoded credentials in a single admin user 


## Adding "User in database" - Not single hardcoded 

### Create the task-manager-api/.../ User.java [User entity]

- In the code we are specifing the name of the table in `@Table(name = "users")`

- `unique = true` makes the database itself refuse duplicate usernames.

### Create the `userRepository.java` [Repository]
- Hibernate creates the users table by itself (ddl-auto=update).
- User entity (@Entity, @table, @Id, @GeneratedValue, @Column) — Hibernate auto-creates the task table
- You don't write any SQL. Spring Data builds the queries from the method names.

### Add imports and bean in `SecurityConfig.java` [PasswordEncoder bean]
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
```
```java
@Bean
public PasswordEncoder passwordEncoder()
{
    return new BCryptPasswordEncoder();
}
```
- BCrypt is a deliberately slow, salted hash. 
- Even if the database leaks, passwords can't be read back and each one is expensive to brute-force
- Never store plain text, and don't use fast hashes like MD5 or SHA-256 for passwords.

### Create `UsernameAlreadyExistsException.java` [Register and login]

### Add handler in `GlobalExceptionHandler.java`

### Update the whole file of `AuthController.java`

What changed?
- The ADMIN_PASSWORD and admin.username values are gone from the code. 
    - The users table is now the source of truth.

- `@Size(max = 72)` on the password, because BCrypt only reads the first 72 bytes.

- An unknown username and a wrong password throw the same exception with the same message. If they differed, an attacker could use the login form to discover which usernames exist.

- @Valid reuses your existing MethodArgumentNotValidException handler, so validation errors already come back as clean 400 JSON.

### Let's test 
- `cd ..`
- `docker ps`
- `docker compose down`

- `cd task-manager-api/`
- `mvn compile`
    - Problem:
        - I needed to change the name `userRepository.java` to `UserRepository.java`

        - `ls src/main/java/com/taskmanager/`
        - We should see all files starting with uperletter    

- Rerun: `mvn compile`

- `cd ..`
- `docker compose down`
- `docker compose up --build -d`

Testing curls
- `curl -i -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{"username":"ricardo","password":"dummy-pass-123"}'`
    - Should see 201, {"username":"ricardo"}

- `curl -i -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{"username":"ricardo","password":"dummy-pass-123"}'`
    - We are adding the same username (Shouldn't work)
    - Should see 409, At bottom {"error":"Username already taken"}

- `curl -i -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{"username":"bob","password":"123"}'`
    - Adding a Password too short
    - Should see 400 - Bottom: {"password":"size must be between 8 and 72"}

- `curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"ricardo","password":"dummy-pass-123"}'`
    - Login → {"token":"..."} (Should see the token)

- `curl -i -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"ricardo","password":"nope"}'`
    - Adding a Wrong password
    - Should see 401 - Bottom: {"error":"Invalid credentials"}

- `docker compose exec db psql -U taskuser -d task_manager -c "SELECT id, username, password_hash FROM users;"`
    - We shouldn't see no trace of dummy-pass-123, but a hashed pwd

- Browser: http://localhost:5173/
    - Login: 
        - ricardo
        - dummy-pass-123

Current situation
- The old admin login stops working 
    - We are using a new user instead

- We should see that the password for users are now hashed
    - password_hash started with $2a$.

- Tasks aren't linked to users yet, so any registered user would see and edit everyone's tasks
    - We should fix it before the public AWS deployment

## Related to Register and Login form

### Replace the code of `task-manager-frontend/src/Login.jsx`

- App.jsx doesn't change.

### Update backend tests

- Change `TaskControllerTest.java`
    - Remove "ADMIN_PASSWORD=test-password" from the @SpringBootTest properties, leaving: 

    - Delete the last two test methods (shouldLoginWithCorrectCredentials and the wrong-password one). They move to a new file.


### Create new file `task-manager-api/src/test/java/com/taskmanager/AuthControllerTest.java`

- The new file is inside the test/ folder 

- Backend test with Docker database up:
    - `docker ps` all 3 are running
    - `docker compose up -d db`
    - `cd task-manager-api/`
        - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager`
        - `SPRING_DATASOURCE_USERNAME=taskuser`
        - `SPRING_DATASOURCE_PASSWORD=taskpass` It's in .env root 
        - `mvn test`


- Problem when running the tests
    - Commands that worked:
        - `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api"`
        - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager \`
        - `SPRING_DATASOURCE_USERNAME=taskuser \`
        - `SPRING_DATASOURCE_USERNAME=taskuser \`
        - `mvn test`

```
cd "/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api"

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager \
SPRING_DATASOURCE_USERNAME=taskuser \
SPRING_DATASOURCE_PASSWORD=taskpass \
mvn test
```
- They worked (This code block above is correct)

### Write the script for the long command
- Create `task-manager-api\test.sh`

- Run the commands:
    - `cd task-manager-api/`
    - `chmod +x test.sh`
    - `DB_PASSWORD=taskpass ./test.sh` - Use this to run the tests in task-manager-api 
        - We should have `docker compose up -d db.` first

    - It worked

### Testing docker compose until now
- `cd ..`
- `docker compose down`
- `docker compose up --build -d`
- Browser: http://localhost:5173/
    - Login: 
        - ricardo
        - dummy-pass-123

    - I created a new user:
        - test1
        - testtest

- Check the database 
    - At root
    - `docker ps` 3 containers should be running
    - `docker compose exec db psql -U taskuser -d task_manager`
        - `\dt` List tables
        - `SELECT * FROM users;` We can see the new user we've created
        - `\q`

- Situation
    - We can create new users
        - Users are stored with BCrypt hashes
    - Registration works
    - We can access with the new users login
    - But all users are sharing the same tasks 

    - Next: Tasks belong to their owner (before the public deployment)


## Related to "Tasks belong to their owner"

### 1) Link tasks to users

#### Add imports and field and accessors inside the class of `Task.java`

#### Replace the code of `TaskRepository.java`

### 2) Scope the controller to the logged-in user

#### Replace the whole code of `TaskController.java`

- What changed:
    - Authentication auth is injected by Spring
    - auth.getName() is the username your JwtFolter put there after validating the token. The user can't forge it
    - Delete used to call deleteById(id) directly (So anyone could delete any task)
        - But now it goes through findOwnedTask first
    - If the token belongs to a user who no longer exists, creating a task returns 401, and the frontend logs out

### 3) One frontend fix

#### Change the `useEffect` of `task-manager-frontend/src/App.jsx`
- Nothing else in the frontend changes, because the token already identifies the user.

### 4) Update the tests

#### Replace the whole file `TaskControllerTest.java`

#### Run the script 
- Go to root
- `docker ps`
- Open 'docker desktop'
- `docker compose up -d db`
- `cd task-manager-api`
- `DB_PASSWORD=taskpass ./test.sh`

- We're suppose to see 13 passing tests
    - 7 in TaskControllerTest 
    - 6 in AuthControllerTest

#### Verify with 2 real users 
- `cd ..`
- `docker compose up --build -d`

```
docker compose exec db psql -U taskuser -d task_manager \
  -c "SELECT id, title, user_id FROM task ORDER BY id;"
```

- Access the database 
    - `docker compose exec db psql -U taskuser -d task_manager`
    - `\dt`
    - `select * from users;`
    - `select * from task;`

    - Users we have
        - ricardo - dummy-pass-123
        - test1 - testest
        - alice - dummy-pass-123
        - bob - dummy-pass-123

- Current situation
    - We are able to create users and each can see only their own list


## Clean up before pushing

### About "ADMIN_PASSWORD"
- Currently, nothing in the project reads `ADMIN_PASSWORD` anymore
- We can remove it from `.env`, `.env.example`, `compose.yml`
    - I've commented in all 3

- Instead: The application use JWT_SECRET for signing and verifying authentication tokens

- We should create a strong, private secret of 32 char for .env
    - We should use openssl rand -base64 48

```
ricardo@LAPTOP-5NQFTCQB:/mnt/c/Users/ASUS/Videos/30 - mini java project$ openssl rand -base64 48

VCpmNUP9MYkzsq9krKdz3fJDc3xMkrIrKnXLtov4c0RMJZxfR7yz+CvxYfULSfjf
```
- We can use this command and it will always generate a new secret
- Copy the generated output into your .env file


### Commit and push
