# Task Manager — Full Stack Learning Project

A full-stack task manager built from scratch while learning Java and backend development: starting with plain Java and JDBC, progressing to a Spring Boot REST API, a React frontend, and Docker Compose tying it all together. Kept as a monorepo, including the personal learning notes written along the way.

## What's in this repo

| Folder | What it is |
|---|---|
| **[task-manager-maven](./task-manager-maven)** | The first version: plain Java, OOP, and raw JDBC talking directly to PostgreSQL. No framework — written to understand what Spring automates later. |
| **[task-manager-api](./task-manager-api)** | The REST API: Java 17, Spring Boot, Spring Data JPA, Spring Security, DTOs, validation, global exception handling, unit tests, Dockerized. Has its own standalone repo and README. |
| **[task-manager-frontend](./task-manager-frontend)** | React + Vite frontend, styled with Tailwind CSS, served via Nginx in production. Has its own standalone repo and README. |
| `docker-compose.yml` | Runs all three services (API, frontend, PostgreSQL) together with one command. |
| `01-learning.md` → `05-large-project.md` | Personal learning notes: concepts learned, problems hit and how they were debugged, and the reasoning behind each decision. Kept as-is, not polished — a real record of the learning process. |

`task-manager-api` and `task-manager-frontend` also exist as **separate, standalone repos** on GitHub, so each can be reviewed independently. This repo is the integration point: where they're wired together with Compose, plus the project's full history and notes in one place.

## Architecture

```
Browser
   ↓
React frontend (Nginx, port 5173)
   ↓  HTTP + Basic Auth
Spring Boot API (port 8080)
   ↓  Spring Data JPA
PostgreSQL (port 5433 on host, 5432 in container)
```

## Running everything

1. Copy the example environment file and set your own values:
   ```
   cp .env.example .env
   ```
2. Start all three services:
   ```
   docker compose up --build -d
   ```
3. Open the app: `http://localhost:5173`
4. API directly: `http://localhost:8080/tasks` (requires Basic Auth — see `.env`)

```
docker compose logs app        # backend logs
docker compose logs frontend   # frontend logs
docker compose down            # stop containers (data is kept)
docker compose down -v         # stop containers and wipe the database
```

## Configuration

| Variable | Purpose | Required |
|---|---|:---:|
| `DB_PASSWORD` | PostgreSQL password (shared by `db` and `app`) | Yes |
| `ADMIN_PASSWORD` | API login password (Basic Auth) | Yes |

See `.env.example` for a template. Never commit `.env`.

## Learning path

```
Java fundamentals + OOP
 ↓
Collections, exceptions, interfaces, enums, streams
 ↓
Maven, SQL, JDBC          → task-manager-maven
 ↓
Spring Boot REST API      → task-manager-api
 ↓
DTOs, validation, global exception handling, testing, security
 ↓
Docker (API + Postgres)
 ↓
React frontend            → task-manager-frontend
 ↓
Docker Compose — all 3 services together (this repo)
 ↓
[NEXT] AWS deployment: RDS, EC2, domain + HTTPS
```

See the numbered `.md` notes in this repo for the detailed, chronological version of this path, including mistakes made and how they were fixed.

## Known limitations / next steps

- Single hardcoded admin user — no registration, roles, or JWT yet.
- Basic Auth over plain HTTP locally; HTTPS is part of the AWS deployment step.
- Not yet deployed anywhere public — local Docker Compose only, for now.
- `task-manager-maven` is kept frozen as a reference; active development continues in `task-manager-api`.

## Next milestone: AWS deployment

- Create an AWS RDS PostgreSQL instance (replacing the containerized `db` service in production)
- Launch an EC2 instance, install Docker
- Point the API's datasource at RDS
- Configure security groups/networking
- Point a domain (Namecheap) at the EC2 instance, with HTTPS