# Task Manager API

A REST API for managing tasks, built with Java and Spring Boot as part of a hands-on backend learning project — progressing from plain Java/OOP to a containerized, tested, secured REST service.

## Features

- Full CRUD for tasks (create, list, get one, mark complete, delete)
- Request/response separated via DTOs
- Input validation with clear, field-specific error messages
- Centralized exception handling (no default Spring error pages)
- Basic authentication (Spring Security)
- Unit tests (JUnit 5, Mockito, MockMvc)
- Dockerized — app and PostgreSQL run together with one command
- Configuration via environment variables (no secrets committed to source)

## Tech stack

Java 17 · Spring Boot 4 · Spring Data JPA · Spring Security · PostgreSQL · Maven · Docker / Docker Compose

## Architecture

```
HTTP request
   ↓
TaskController        (endpoints, request/response handling)
   ↓  uses                    ↓ caught by
TaskRepository          GlobalExceptionHandler
(Spring Data JPA)
   ↓ manages
Task (@Entity)
   ↓ has a
TaskStatus (enum)
```

- **`Task`** — JPA entity, mapped directly to the database table (`@Entity`, `@Id`, `@GeneratedValue`).
- **`TaskRepository`** — extends `JpaRepository<Task, Integer>`; CRUD methods generated automatically, no manual SQL.
- **`TaskRequestDTO` / `TaskResponseDTO`** — control exactly what the client can send and receive; entities are never exposed directly.
- **`TaskController`** — REST endpoints, delegates persistence to the repository.
- **`GlobalExceptionHandler`** — catches validation and "not found" errors centrally, returns clean JSON instead of Spring's default error pages.
- **`SecurityConfig`** — defines a single in-memory user and requires basic auth on every request.

## Endpoints

| Method | Path                    | Description        | Auth required |
|--------|-------------------------|---------------------|:---:|
| GET    | `/tasks`                | List all tasks      | ✅ |
| GET    | `/tasks/{id}`           | Get one task        | ✅ |
| POST   | `/tasks`                | Create a task       | ✅ |
| PUT    | `/tasks/{id}/complete`  | Mark task as DONE   | ✅ |
| DELETE | `/tasks/{id}`           | Delete a task       | ✅ |

Example request body for `POST /tasks`:
```json
{ "title": "Study Spring Boot" }
```

## Running with Docker (recommended)

1. Copy the example environment file and set your own values:
   ```
   cp .env.example .env
   # then open .env and replace the placeholder values
   ```
2. Start the app and database together:
   ```
   docker compose up --build
   ```
3. API is available at `http://localhost:8080`.

```
docker compose logs app     # view app logs
docker compose down         # stop containers (data is kept)
docker compose down -v      # stop containers and wipe the database
```

## Running locally (without Docker)

Requires Java 17, Maven, and a local PostgreSQL instance.

```
export DB_URL=jdbc:postgresql://localhost:5432/task_manager
export DB_USER=taskuser
export DB_PASSWORD=taskpass
export ADMIN_PASSWORD=admin123

mvn spring-boot:run
```

## Example usage

```bash
# Unauthenticated request → 401
curl -i http://localhost:8080/tasks

# Create a task
curl -u admin:admin123 -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Study Spring Boot"}'

# List tasks
curl -u admin:admin123 http://localhost:8080/tasks

# The one we used without modifying the code: 
curl -u admin:change_me http://localhost:8080/tasks

# Mark a task complete
curl -u admin:admin123 -X PUT http://localhost:8080/tasks/1/complete

# Delete a task
curl -u admin:admin123 -X DELETE http://localhost:8080/tasks/1
```

## Running tests

```
export DB_PASSWORD=taskpass
export ADMIN_PASSWORD=admin123
mvn test
```

Covers: listing tasks, creating a task, and handling a "task not found" case — using `MockMvc` and Mockito to isolate the controller from the real database.

## Configuration

All configuration lives in `application.properties` and is sourced from environment variables, with safe defaults for local development where appropriate:

| Variable | Purpose | Required |
|---|---|:---:|
| `DB_URL` | JDBC connection string | No (defaults to local PostgreSQL) |
| `DB_USER` | Database username | No (defaults to `taskuser`) |
| `DB_PASSWORD` | Database password | No (defaults to `taskpass` for local dev) |
| `ADMIN_PASSWORD` | API login password | **Yes** — no default, app fails to start without it |

See `.env.example` for a template.

## Known limitations / next steps

- Single hardcoded in-memory user — no user registration, roles beyond a single role, or JWT yet.
- No service layer — controller talks to the repository directly; fine at this scale, would split out for a larger domain.
- No CI/CD pipeline yet.
- No frontend yet — API only.

## Project history

This API is the second stage of a larger learning project. An earlier version (`task-manager-maven`) implements the same core logic using plain Java, JDBC, and hand-written SQL — kept as a separate project to show the progression from manual persistence to Spring Data JPA.