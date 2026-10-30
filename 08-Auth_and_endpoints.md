# 06 — Endpoints, JWT and Authentication (study notes)

Personal notes to understand how login, tokens and per-user tasks work in this project, file by file.
All passwords and tokens in this file are dummy values.

---

## 1. Big picture

```
Browser (React)
   │  1. POST /auth/register   (create account)
   │  2. POST /auth/login      (username + password)  →  gets a JWT back
   │  3. GET/POST/PUT/DELETE /tasks   with   Authorization: Bearer <JWT>
   ▼
Spring Boot API
   CORS check → JwtFilter → Security rules → Controller → Repository → PostgreSQL
```

About the API requests
- The browser runs frontend code that makes API requests to URLs on the backend. 
- The backend receives those requests, runs the API logic, and sends a response back to the browser.

The password is sent **once** (login). After that the browser proves who it is with a **token**.
- When you log in, you send your password to the backend once to prove your identity. If the login is successful, the backend gives your browser a JWT token, which acts like a temporary digital ID card. The browser stores this token and sends it with future API requests. The backend checks the token to verify who you are, so you don’t need to enter your password every time you make a request.
- The frontend/browser stores and sends the JWT token with API requests, and the backend verifies the token to confirm who the user is.

CORS (Cross-Origin Resource Sharing)
- Is a browser security check fthat controls whether a frontend from one origin (e.g. localhost:3000) is allowed to call an API on another origin (e.g. localhost:8080). It helps prevent unauthorized websites from making requests to your API from a user's browser. 

---
 
### About the `Login.jsx`

[First part | const API_URL](/task-manager-frontend/src/Login.jsx#L3) 
- This `const API_URL = import.meta.env.VITE_API_URL` is the URL that points to `VITE_API_URL` that is inside [.env](/task-manager-frontend/.env.production#L3)
- First, we have this const that give the base URL that points to the backend
- It is used for the for the function "function post" right under it 
- The file related to the endpoints of login is [this file](/task-manager-api/src/main/java/com/taskmanager/AuthController.java#L11)
    - We can search for `@RestController` to find the HTTP requests and returns resopnses (In general, but inside we have the API contract(endpoints))
    - For the specific API contract/endpoint, we can find in [this line (backend endpoint)](/task-manager-api/src/main/java/com/taskmanager/AuthController.java#L32) 

[Second part | Inside "async function post()"](/task-manager-frontend/src/Login.jsx#L6)
- This is the post() function that requires path (ex: '/auth/register') and body (ex: { username, password })
- So this post() takes a path (e.g., /auth/register) and a body (e.g., { username, password }).
    - It connects to the backend by combining the backend URL (API_URL) with the path
- [The line 7](/task-manager-frontend/src/Login.jsx#L7) sends the body as JSON using a POST request and save as `res` (response)

- [The line 13](/task-manager-frontend/src/Login.jsx#L13) converts the response body into JavaScript data

- The [if (!res.ok)](/task-manager-frontend/src/Login.jsx#L15) 
    - Check whether the request succeeded
    - res.ok is true for successful HTTP status codes
    - If the request failed, it throws an error using the server's message if available, or a fallback message

- The [`return data`](/task-manager-frontend/src/Login.jsx#L18) returns [`new RegisterResponse(user.getUsername())`](/task-manager-api/src/main/java/com/taskmanager/AuthController.java#L32-L44)  
    - It contains the registered username. Spring converts it into a JSON response
    - The response is received by [fetch()](/task-manager-frontend/src/Login.jsx#L7) that sends the POST request to the backednand receives its HTTP response - Stored in `res` 
        - The response contains the status code and the response body (such as the registered username).

[Third part | function Login](/task-manager-frontend/src/Login.jsx#L21-L86)
- This part is where the user enter input: [Here](/task-manager-frontend/src/Login.jsx#L54-L62)
    - This `placeholder="Username"` is what we see in the UI 
    - In [`onChange={(e) => setUsername(e.target.value)}`](/task-manager-frontend/src/Login.jsx#L57) we can see that it goes to the [`setUsername`](/task-manager-frontend/src/Login.jsx#L23-L24)  


- We can see that [post('/auth/register', { username, password })](/task-manager-frontend/src/Login.jsx#L34) is the function of [async function post()](/task-manager-frontend/src/Login.jsx#L6) 
    - So inside the `function Login({ onLogin })` we specify the user input values
        - `{ username, password }` these are the values of the user input
    - Then it take these 2 values, which are the `post(path, body)` (body part) and then it uses the `fetch()` sends the data to the backend`

---

## 2. Endpoints

| Method | Path | Auth | Success | Errors |
|---|---|:---:|---|---|
| POST | `/auth/register` | public | `201` `{"username":"..."}` | `400` validation, `409` username taken |
| POST | `/auth/login` | public | `200` `{"token":"..."}` | `401` invalid credentials |
| GET | `/tasks` | JWT | `200` list of **my** tasks | `401` |
| POST | `/tasks` | JWT | `200` the created task | `400` empty title, `401` |
| GET | `/tasks/{id}` | JWT | `200` the task | `404` not found / not mine, `401` |
| PUT | `/tasks/{id}/complete` | JWT | `200` task with status `DONE` | `404`, `401` |
| PUT | `/tasks/{id}/undo` | JWT | `200` task with status `TODO` | `404`, `401` |
| DELETE | `/tasks/{id}` | JWT | `200` empty body | `404`, `401` |

Meaning of the codes: `400` bad input, `401` not authenticated (no or bad token), `404` not found, `409` conflict (already exists).


This is the file for **tasks** endpoints: [File](/task-manager-api/src/main/java/com/taskmanager/TaskController.java#L12) (TaskController.java) 

This is the file for **register/login** endpoints: [File](/task-manager-api/src/main/java/com/taskmanager/AuthController.java#L12) (AuthController.java)

 
---

## 3. What a JWT is

JWT = **JSON Web Token**. A string with three parts separated by dots:

```
HEADER . PAYLOAD . SIGNATURE
```

- **Header**: the signing algorithm, e.g. `{"alg":"HS384"}`
- **Payload** (the "claims"): `{"sub":"ricardo","iat":1791527566,"exp":1791531166}`
  - `sub` = subject (the username), `iat` = issued at, `exp` = expiration (both are Unix timestamps)
- **Signature**: a hash of header + payload made with the server's secret (`JWT_SECRET`)

Key facts:
- Header and payload are only **Base64URL-encoded, not encrypted**. Anyone can read them. Never put secrets in a token.
- Nobody can **change** the payload or forge a token without `JWT_SECRET`, because the signature wouldn't match.
- The server keeps no list of tokens (**stateless**). It just re-checks the signature and the expiry.
- Weak point: a stolen token works until it expires (1 hour here), and there is no way to revoke it early.

Exercise: read the payload of a token yourself.
```bash
payload=$(echo "$TOKEN" | cut -d. -f2 | tr '_-' '/+')
while [ $(( ${#payload} % 4 )) -ne 0 ]; do payload="${payload}="; done
echo "$payload" | base64 -d; echo
```
Use only dev tokens, and don't paste real tokens into websites.

---

## 4. Backend files

### `JwtUtil.java` — creates and checks tokens
```java
public String generateToken(String username)
{
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))  // 1 hour
        .signWith(getKey())
        .compact();
}
```
- `getKey()` builds the signing key from `jwt.secret` with `Keys.hmacShaKeyFor(...)`. The secret must be **at least 32 characters**, or the library throws a "weak key" error.
- `isTokenValid(token)` parses the token. Parsing throws if the signature is wrong or the token is expired, and the method turns that into `false`.
- `extractUsername(token)` returns the `sub` claim.
- The secret comes from the env var `JWT_SECRET` → `application.properties` (`jwt.secret=${JWT_SECRET}`) → `@Value("${jwt.secret}")`.

### `JwtFilter.java` — checks the token on every request
Extends `OncePerRequestFilter`, so it runs once per request, **before** the controller.
```
1. Read the "Authorization" header
2. If it starts with "Bearer ", take the token after it
3. If the token is valid → tell Spring Security "this request is authenticated as <username>"
4. Always call chain.doFilter(...) and let the request continue
```
The filter never rejects a request itself. If the token is missing or invalid, it simply doesn't authenticate, and the security rules in `SecurityConfig` return `401` later.

### `SecurityConfig.java` — the rules
- `.requestMatchers("/auth/**").permitAll()` → register and login need no token.
- `.anyRequest().authenticated()` → everything else needs an authenticated request.
- `.sessionManagement(... STATELESS)` → no server-side session; the token is the only proof.
- `.csrf(disable)` → see section 7.
- `.exceptionHandling(... HttpStatusEntryPoint(401))` → unauthenticated requests get a clean `401`.
- `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` → puts our filter in the chain.
- `corsConfigurationSource()` → which browser origins may call the API (currently only `http://localhost:5173`).
- `PasswordEncoder` bean → `BCryptPasswordEncoder`.

### `AuthController.java` — register and login
```
register: validate input (@Valid) → username already used? → 409
          else hash the password with BCrypt → save the user → 201
login:    find the user by username → compare password with the stored hash
          → if either fails: the same InvalidCredentialsException (401)
          → else generate and return a token
```
- `record` classes (`RegisterRequest`, `LoginResponse`, ...) are compact data carriers for request and response bodies.
- `@Size(min = 8, max = 72)` on the password: 72 because BCrypt only uses the first 72 bytes.

### `User.java` and `UserRepository.java`
- Table is named `users` (`@Table(name = "users")`) because `user` is a reserved word in PostgreSQL.
- Stores `username` (unique) and `passwordHash`. **Never the password.**
- `UserRepository` has `findByUsername` and `existsByUsername`. Spring Data JPA builds the SQL from the method names.

### `Task.java` — now has an owner
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User owner;
```
Many tasks belong to one user. This creates a `user_id` foreign key column in the `task` table.

### `TaskRepository.java`
```java
List<Task> findByOwnerUsernameOrderByIdAsc(String username);
Optional<Task> findByIdAndOwnerUsername(int id, String username);
```
Every lookup is filtered by the owner's username.

### `TaskController.java` — where ownership is enforced
```java
public List<TaskResponseDTO> getAllTasks(Authentication auth)
{
    return taskRepository.findByOwnerUsernameOrderByIdAsc(auth.getName()) ...
}
```
- `Authentication auth` is injected by Spring. `auth.getName()` is the username that `JwtFilter` took from the **validated token**.
- The client never says "I am user X". The identity always comes from the token, so it can't be faked.
- `findOwnedTask(id, auth)` is used by get, complete, undo and delete. If the task isn't yours, it throws "Task not found".

### `GlobalExceptionHandler.java` — errors as JSON
| Exception | Status |
|---|---|
| `MethodArgumentNotValidException` (bad input) | `400` with `{field: message}` |
| `InvalidCredentialsException` | `401` |
| `UsernameAlreadyExistsException` | `409` |
| `RuntimeException` (catch-all, e.g. "Task not found") | `404` |

Spring picks the **most specific** handler, so the `401` and `409` handlers win over the catch-all `RuntimeException` one.

---

## 5. Frontend files

### `Login.jsx`
- One form with two modes: **login** and **sign up** (toggle with a button).
- Sign up calls `/auth/register` and then `/auth/login`, so the user doesn't type credentials twice.
- A small `post()` helper reads the error message from the server's JSON.
- `minLength` on the inputs is only a convenience. The real validation is on the server, because anyone can bypass the browser.

### `App.jsx`
- `const [token, setToken] = useState(null)` → the token lives in memory.
- No token → shows `<Login onLogin={setToken} />`. Token → shows the task list.
- `apiFetch(path, options)` is a wrapper that adds `Authorization: Bearer <token>` to every request and logs out on a `401` (expired token).
- `useEffect(... [token])` loads the tasks when a token appears and clears them when it disappears, so one user's tasks never show to the next.
- Trade-off: a page refresh logs you out. Production apps usually keep the token in an `httpOnly` cookie instead, which JavaScript cannot read (so it is safer against XSS).

### `.env.development` / `.env.production`
`VITE_API_URL` is read by Vite at **build time**. It is baked into the JS, which is why the Docker build passes it as a build argument.

---

## 6. Request lifecycles

**Login**
```
POST /auth/login → CORS → JwtFilter (no header, does nothing) → /auth/** is permitAll
→ AuthController.login → UserRepository.findByUsername → BCrypt matches()
→ JwtUtil.generateToken → {"token": "..."}
```

**Get my tasks**
```
GET /tasks + Bearer token → CORS → JwtFilter validates the token and sets the user
→ rule "authenticated" passes → TaskController.getAllTasks(auth)
→ TaskRepository.findByOwnerUsernameOrderByIdAsc(auth.getName()) → SQL → DTOs → JSON
```

**Missing or bad token**
```
GET /tasks (no/invalid token) → JwtFilter does nothing → no authentication
→ rule "authenticated" fails → HttpStatusEntryPoint → 401
```

**Someone else's task**
```
GET /tasks/5 as bob (task 5 is alice's) → findByIdAndOwnerUsername(5, "bob") → empty
→ "Task not found" → 404
```

---

## 7. Why it's built this way

| Decision | Reason |
|---|---|
| **BCrypt** hashes | Slow, salted hash. If the DB leaks, passwords can't be read back and each guess is expensive. Never store plain text, and don't use fast hashes (MD5, SHA-256) for passwords. |
| Same error for unknown user and wrong password | If the messages differed, the login form would reveal which usernames exist (**user enumeration**). |
| `404`, not `403`, for someone else's task | A `403` would confirm the task exists. |
| Owner taken from the token, never the request | Anything the client sends can be forged. |
| **IDOR** protection | IDOR = Insecure Direct Object Reference: changing an id in the URL to reach another user's data. Fixed by always filtering by owner. |
| **Stateless** + JWT | No session storage on the server, which makes scaling out easier. |
| CSRF disabled | CSRF (Cross-Site Request Forgery) abuses cookies that browsers attach automatically. Our token goes in a header that the browser does **not** attach on its own, so the attack doesn't apply. If tokens move to cookies, CSRF protection must come back. |
| **CORS** config | CORS = Cross-Origin Resource Sharing. A browser rule: JavaScript on `localhost:5173` may only call `localhost:8080` if the API explicitly allows that origin. `curl` ignores CORS. |
| DTOs | Control exactly what goes in and out, so internal fields (like `passwordHash` or the owner) never leak into responses. |
| Validation on the server | The client can always be bypassed. |

---

## 8. What the tests cover

`TaskControllerTest` (7):
- returns only the current user's tasks (alice sees hers, bob sees none)
- creates a task owned by the logged-in user (captures the saved task and checks its owner)
- `404` for a task that doesn't exist
- one user cannot read another user's task
- one user cannot delete another user's task (and `delete` is never called)
- `401` without a token, `401` with an invalid token

`AuthControllerTest` (6): register OK, duplicate username, short password, login OK, wrong password, unknown user.

Tests give themselves a `jwt.secret` in `@SpringBootTest(properties = ...)`, so they don't depend on shell variables. Run them with `DB_PASSWORD=... ./test.sh` after `docker compose up -d db`.

---

## 9. Try it yourself

```bash
API=http://localhost:8080
H='Content-Type: application/json'

# register, log in, keep the token
curl -s -X POST $API/auth/register -H "$H" -d '{"username":"demo","password":"dummy-pass-123"}'
TOKEN=$(curl -s -X POST $API/auth/login -H "$H" \
  -d '{"username":"demo","password":"dummy-pass-123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# use it
curl -s -X POST $API/tasks -H "Authorization: Bearer $TOKEN" -H "$H" -d '{"title":"first task"}'
curl -s $API/tasks -H "Authorization: Bearer $TOKEN"

# no token → 401
curl -i $API/tasks
```
And look at the stored data:
```bash
docker compose exec db psql -U taskuser -d task_manager \
  -c "SELECT id, username, password_hash FROM users;"
```
`password_hash` starts with `$2a$10$`: the algorithm, the cost, then the salt and the hash. The salt is stored inside the hash, which is how `matches()` can verify a password.

---

## 10. Known limitations / ideas

- Token lasts 1 hour with no refresh token. After that you log in again.
- A stolen token can't be revoked before it expires.
- Token is kept in memory, so a refresh logs you out (an `httpOnly` cookie would fix this, but then CSRF protection is needed).
- No rate limiting on `/auth/login`, so it can be brute-forced.
- Passwords only need 8+ characters, with no other rules.
- CORS allows only `http://localhost:5173`. It must change to the real domain when deployed.
- The catch-all `RuntimeException` → `404` handler turns *any* unexpected error into a 404. Replace it with specific exceptions (`TaskNotFoundException`, etc.).
- No password change, no account deletion.
- `ddl-auto=update` lets Hibernate change the schema by itself. Real projects use migrations (Flyway or Liquibase).
- Tasks created before ownership existed have no owner and are invisible.

---

## 11. Questions to answer without looking (interview practice)

1. What are the three parts of a JWT, and which one can be tampered with?
2. Why is a JWT not secure storage for secrets?
3. Where does the server get the username from when I call `GET /tasks`?
4. Why does `JwtFilter` not reject requests itself?
5. Why `404` instead of `403` for another user's task?
6. Why must unknown-user and wrong-password return the same message?
7. What does BCrypt's salt do, and where is it stored?
8. Why is CSRF disabled here, and when would I need it back?
9. What does stateless mean, and what does it cost?
10. What is IDOR, and how does `findByIdAndOwnerUsername` prevent it?