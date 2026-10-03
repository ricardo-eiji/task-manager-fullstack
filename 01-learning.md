# Task Manager — Learning Journal

Personal notes tracking my Java backend learning path, from plain OOP to a Spring Boot REST API.

---

## 📁 Project Folders — What's What

You'll see **two separate project folders**. This is intentional — each represents a different stage of learning, kept separate so I can look back and compare.

### `task-manager-maven/`
The **first version** — plain Java + JDBC.
- In-memory `ArrayList`, later converted to talk directly to PostgreSQL using raw SQL (`JDBC`, `PreparedStatement`).
- No framework — I wrote all the database logic by hand.
- Purpose: understand what's actually happening "under the hood" before letting a framework (Spring) do it for me.
- Entry point: `Task_manager.java` (has `main`), run via:
  ```
  mvn compile exec:java -Dexec.mainClass="com.taskmanager.Task_manager"
  ```

### `task-manager-api/`
The **second version** — Spring Boot REST API.
- Same core idea (Task, TaskStatus), but now exposed as HTTP endpoints instead of a `main` method.
- Uses Spring Data JPA (auto-generated SQL) instead of hand-written JDBC.
- This is the version that will keep growing (Security, Docker, etc.)
- The file that `TaskManagerApiApplication.java` is the `main`
- Run via:
  ```
  mvn spring-boot:run
  ```
- Then visit/test: `http://localhost:8080/tasks`

**Why keep both?** `task-manager-maven` shows I understand the fundamentals (raw SQL, JDBC) before relying on Spring's abstractions. Good to keep as a reference / portfolio piece showing progression.

---

## 📄 File-by-File Explanation & Connections

### `task-manager-maven/` (JDBC version)

```
Task_manager.java (main)
   ↓ creates
TaskRepository (interface) ← implemented by → TaskManager
   ↓ TaskManager uses            ↓ TaskManager uses
Task.java                    Database.java → JDBC → PostgreSQL
   ↑ also used by
TaskNotFoundException.java
TaskStatus.java
```

| File | Role | Connects to |
|---|---|---|
| **`Task.java`** | Defines a single task: `id`, `title`, `status`. Validates itself in the constructor (no negative id, no empty title). Each `Task` object manages *itself* — not the collection. | Used by `TaskManager` (creates/returns `Task` objects) and `Task_manager` (loops through them to print). |
| **`TaskStatus.java`** | Enum: `TODO`, `IN_PROGRESS`, `DONE`. Fixed, type-safe set of states — replaced an old `boolean completed` field. | Used inside `Task` (the `status` field) and by `TaskManager` (to filter/update status). |
| **`TaskRepository.java`** | Interface — the *contract*. Declares what operations must exist (`addTask`, `removeTask`, `findTask`, etc.) without saying how. | Implemented by `TaskManager`. Used as the *type* in `main` (`TaskRepository manager = new TaskManager();`) so `main` only knows the contract, not the implementation. |
| **`TaskNotFoundException.java`** | Custom exception — thrown when a task id doesn't exist, instead of returning `null`. | Thrown by `TaskManager.findTask()`; caught in `Task_manager`'s `main` with try/catch. |
| **`Database.java`** | Centralizes the JDBC connection: URL, username, password. One method, `connect()`, returns a `Connection`. | Used by every method in `TaskManager` that touches the database (`addTask`, `getTasks`, `completeTask`, `removeTask`, `findTask`, `findCompletedTasks`). |
| **`TaskManager.java`** | The core logic. Implements `TaskRepository`. Every method opens a `Database` connection, runs SQL via `PreparedStatement`, and returns `Task` objects (rebuilt from the query results). | Uses `Database`, `Task`, `TaskStatus`, `TaskNotFoundException`. Implements `TaskRepository`. Used by `Task_manager` (`main`). |
| **`Task_manager.java`** | Entry point (`main`). Creates a `TaskManager`, calls its methods through the `TaskRepository` interface, prints results. | Depends on everything above, but only *directly* creates a `TaskManager` and calls it through `TaskRepository`. |
| **`Methods.java`** *(deleted)* | Old static-method version, before `TaskManager` existed. Kept temporarily as reference, then removed as dead code. | — |

**Flow of a single request (e.g. `manager.addTask(0, "Study Java")`):**
`main` → `TaskManager.addTask()` → `Database.connect()` → SQL `INSERT` via `PreparedStatement` → PostgreSQL.

---

### `task-manager-api/` (Spring Boot version)

```
HTTP Request (curl / browser)
   ↓
TaskController  ──uses──▶  TaskRepository (Spring Data JPA)
   │  ↑ uses                    ↓ manages
   │  TaskRequestDTO         Task (@Entity)
   │  TaskResponseDTO             ↓ has a
   │                          TaskStatus (enum)
   ↓ errors caught by
GlobalExceptionHandler
```

| File | Role | Connects to |
|---|---|---|
| **`TaskManagerApiApplication.java`** | The entry point. `@SpringBootApplication` — starts the embedded server (Tomcat), scans the package for controllers/repositories/entities, wires everything together. | Doesn't call other files directly — Spring Boot auto-discovers them via annotations. |
| **`Task.java`** | `@Entity` — maps this class directly to a database table (`task`). `@Id` + `@GeneratedValue` = auto-incrementing primary key. No manual SQL — Hibernate generates it from this class. | Has a `TaskStatus status` field. Managed by `TaskRepository`. Converted to/from `TaskRequestDTO`/`TaskResponseDTO` in `TaskController`. |
| **`TaskStatus.java`** | Same enum as the JDBC version — `TODO`, `IN_PROGRESS`, `DONE`. | Used inside `Task`. |
| **`TaskRepository.java`** | `interface TaskRepository extends JpaRepository<Task, Integer>` — no method bodies needed. Spring Data JPA auto-generates `save()`, `findAll()`, `findById()`, `deleteById()`, etc. at runtime. | Used by `TaskController` via `@Autowired` (dependency injection). Operates on `Task` entities. |
| **`TaskRequestDTO.java`** | Defines exactly what the client is allowed to *send* (just `title`). Has `@NotBlank` validation. | Received by `TaskController.addTask()` via `@RequestBody @Valid`. Used to build a new `Task`. |
| **`TaskResponseDTO.java`** | Defines exactly what the client *receives* back (`id`, `title`, `status`). Its constructor takes a `Task` and copies the fields over. | Built from a `Task` entity inside `TaskController` methods, returned as the JSON response. |
| **`TaskController.java`** | `@RestController` — handles all HTTP requests under `/tasks`. Each method maps to one endpoint (`@GetMapping`, `@PostMapping`, etc.). Converts between DTOs and entities. | Uses `TaskRepository` (data access), `Task` (entity), `TaskRequestDTO`/`TaskResponseDTO` (shape of data in/out). Exceptions it throws are caught by `GlobalExceptionHandler`. |
| **`GlobalExceptionHandler.java`** | `@RestControllerAdvice` — catches exceptions thrown anywhere in `TaskController` (and any future controllers), centrally. Turns them into clean JSON error responses instead of Spring's default error page. | Listens for `MethodArgumentNotValidException` (validation failures) and `RuntimeException` (e.g. `TaskNotFoundException`-style "not found" errors) thrown from `TaskController`. |
| **`application.properties`** | Configuration file — database URL/credentials, `ddl-auto=update` (auto-create/update tables from entities), `show-sql=true` (logs SQL Hibernate generates). | Read automatically by Spring Boot at startup; used to configure the datasource that `TaskRepository` ultimately runs on. |

**Flow of a single request (e.g. `POST /tasks` with `{"title": "Study Java"}`):**
HTTP request → `TaskController.addTask()` → validates `TaskRequestDTO` (`@Valid`) → builds a `Task` entity → `TaskRepository.save()` (Hibernate generates SQL) → PostgreSQL → returns saved `Task` → wrapped in `TaskResponseDTO` → sent back as JSON.
If something fails (validation, not found), `GlobalExceptionHandler` intercepts it and returns a clean error instead of crashing.

---

## 🗺️ Roadmap Followed

```
Java fundamentals
 ↓
OOP (classes, objects, encapsulation)
 ↓
Collections (ArrayList)
 ↓
Constructor validation + Exceptions
 ↓
Custom Exceptions
 ↓
Interfaces
 ↓
Generics (concept)
 ↓
Enums
 ↓
Streams / Lambdas
 ↓
Maven
 ↓
SQL / PostgreSQL
 ↓
JDBC (Java ↔ Database)
 ↓
Spring Boot
 ↓
REST Controller
 ↓
Spring Data JPA
 ↓
DTOs + Validation
 ↓
Global Exception Handling
 ↓
[NEXT] Testing (JUnit/Mockito)
 ↓
Spring Security
 ↓
Docker
```

---

## 📚 Concepts Learned (with why they matter)

### OOP & Java Fundamentals

**Encapsulation**
- Moved from static `Methods.java` (loose functions passing an `ArrayList` around) to a `TaskManager` class that *owns* its data.
- Why: object controls its own state — nothing outside can corrupt it directly.

**Constructor validation**
- `Task`'s constructor rejects negative ids and empty titles immediately.
- Why: "fail fast" — catch bad data at the earliest point instead of letting it spread through the program.

**Custom Exceptions**
- Created `TaskNotFoundException` instead of returning `null` when a task isn't found.
- Why: clearer than `if (task == null)` everywhere; this is how real APIs signal specific errors.

**Interfaces**
- Created `TaskRepository` interface; `TaskManager` implements it.
- `main` depends on `TaskRepository manager`, not the concrete class.
- Why: "program to an interface, not an implementation" — lets you swap the underlying implementation (e.g. in-memory → database → different database) without touching the calling code.

**Generics (concept only)**
- Practiced with a small `Box<T>` class.
- Didn't force it into `TaskRepository` yet (would've needed extra design work) — revisited properly later via Spring's `JpaRepository<Task, Integer>`.

**Enums**
- Replaced a `boolean completed` field with `TaskStatus` (`TODO`, `IN_PROGRESS`, `DONE`).
- Why: fixed, safe set of states — prevents typos/invalid values; compiler enforces it.

**Streams / Lambdas**
- Rewrote loop-based filtering (`findCompletedTasks`) using `.stream().filter(...).collect(...)`.
- Also used for sorting: `.sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))`.
- Why: more declarative/readable for simple transformations; very common in Spring codebases.

---

### Tooling

**Maven**
- Installed via `sudo apt install maven`.
- Migrated flat `.java` files into proper Maven structure (`src/main/java/...`).
- `pom.xml` manages dependencies and build config (`maven.compiler.source/target`).
- Key lesson: `package` declaration must be the *first line* in every file, and match the folder structure.

**Git**
- Committed after each logical milestone (constructor validation, TaskManager, interface, etc.)
- Learned to add `.gitignore` with `target/` — never commit build output, it's auto-regenerated and creates noisy diffs.

---

### Databases

**SQL basics**
- `CREATE TABLE`, `INSERT`, `SELECT`, `UPDATE ... WHERE`, `DELETE ... WHERE`.
- Practiced directly in `psql`.

**JDBC**
- `Database.java` centralizes the connection (URL, user, password) in one place.
- `PreparedStatement` with `?` placeholders — never string-concatenate SQL (prevents SQL injection).
- Converted every `TaskManager` method (`addTask`, `getTasks`, `completeTask`, `removeTask`, `findTask`, `findCompletedTasks`) from in-memory `ArrayList` logic to real SQL queries.
- Lesson learned the hard way: the DB *persists* between runs — unlike the old in-memory list, re-running `main` re-inserts data every time. Had to clean up test code accordingly.

---

### Spring Boot

**Setup**
- Generated via [Spring Initializr](https://start.spring.io) (Maven, Java 17, Spring Web + Spring Data JPA + PostgreSQL Driver).
- Configured `application.properties` with datasource URL/credentials.
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate auto-creates/updates tables based on `@Entity` classes. No manual `CREATE TABLE` needed anymore.

**Entity**
- `@Entity` on `Task` → maps the class to a database table (named after the class, lowercase, unless overridden with `@Table(name = "...")`).
- `@Id` + `@GeneratedValue` → primary key, auto-incremented.

**Repository**
- `TaskRepository extends JpaRepository<Task, Integer>` — no method bodies needed. Free CRUD methods (`save`, `findAll`, `findById`, `deleteById`) generated automatically.
- This is what "generics done properly" looks like — `JpaRepository<T, ID>` is reusable for *any* entity.

**REST Controller**
- `@RestController` + `@RequestMapping("/tasks")` — base path for all task-related endpoints.
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` — map HTTP verbs to methods.
- `@PathVariable` — captures `{id}` from the URL.
- `@RequestBody` — converts incoming JSON into a Java object.
- `@Autowired` — Spring automatically provides a working `TaskRepository` instance (dependency injection).

Endpoints built:
| Method | Path                  | Purpose              |
|--------|------------------------|-----------------------|
| GET    | `/tasks`               | List all tasks        |
| GET    | `/tasks/{id}`          | Get one task          |
| POST   | `/tasks`               | Create a task          |
| PUT    | `/tasks/{id}/complete` | Mark task as DONE      |
| DELETE | `/tasks/{id}`          | Delete a task          |

**DTOs (Data Transfer Objects)**
- `TaskRequestDTO` — controls exactly what the client can send (just `title`; not `id` or `status`).
- `TaskResponseDTO` — controls exactly what's returned to the client.
- Why: never expose entities directly — keeps internal structure decoupled from the public API contract.

**Validation**
- `@NotBlank` on `TaskRequestDTO.title`.
- `@Valid` on the controller parameter triggers the check automatically before the method runs.
- Added `spring-boot-starter-validation` dependency.

**Global Exception Handling**
- `@RestControllerAdvice` + `@ExceptionHandler` — catches exceptions from *any* controller, centrally, instead of try/catch in every method.
- Handles validation errors (400 Bad Request, with field-specific messages) and "not found" errors (404, clean JSON message) instead of Spring's generic default error pages.

---

## 🧪 Testing Commands Used (curl)

```bash
# Get all tasks
curl http://localhost:8080/tasks

# Get one task
curl http://localhost:8080/tasks/3

# Create a task
curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Learn Spring Data JPA"}'

# Complete a task
curl -X PUT http://localhost:8080/tasks/1/complete

# Delete a task
curl -X DELETE http://localhost:8080/tasks/1
```

---

## 🐘 PostgreSQL Setup Notes

```sql
CREATE DATABASE task_manager;
CREATE USER taskuser WITH PASSWORD 'taskpass';
GRANT ALL PRIVILEGES ON DATABASE task_manager TO taskuser;
```

Connect:
```bash
psql -U taskuser -d task_manager -h localhost
```

Note: the old `task-manager-maven` project manually created a table called `tasks` (plural). The new `task-manager-api` project's Hibernate auto-created a *separate* table called `task` (singular, from the `@Entity` class name) — they coexist in the same database but are unrelated tables.

---

## ➡️ Next Steps

1. Testing (JUnit + Mockito) — unit tests for controller/service logic
2. Spring Security — authentication, roles, JWT
3. Docker — containerize the app + database
4. (Optional/later) Frontend to make it a full-stack project

---

## 💡 General Lessons (non-Java)

- **Best practice reasoning matters more than memorizing syntax** — every step here solved a real problem I'd just hit (e.g. enums after hitting boolean's limits), which sticks better than learning theory upfront.
- **Commit messages + README = real documentation.** Professionals don't write a step-by-step log of every debugging session — they keep meaningful commit history and a clear README. Routine fixes (typos, stale builds) aren't worth documenting.
- **`.gitignore` build output (`target/`) from day one** — avoids noisy diffs and bloated repos.
- Being able to **explain your own code** (not just that AI helped write it) is what actually matters in interviews — the debugging and "why" conversations are the real learning, not the code itself.