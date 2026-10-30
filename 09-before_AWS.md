## Modify the `Task.java` file

- Reason: The enum is stored as a number (0,1,2), so reordering TaskStatus later would silently corrupt data

- Changes for `Task,java`
    - Remove "import javax.annotation.processing.Generated;"
    - Add "@Enumerated(EnumType.STRING)" on status
    - Move the `owner` field up with other fields

## Delete and start fresh the dev database and test

Restart containers
- `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api"`
- `mvn compile`
- `cd ..`
- `docker compose down -v`
- `docker compose up --build -d`

- I manually deleted the containers in "docker desktop"

Check the app in the browser
- Browser: http://localhost:5173

Check the database:
- docker compose exec db psql -U taskuser -d task_manager \
  -c "SELECT id, title, status, user_id FROM task;"\

- `docker compose exec db psql -U taskuser -d task_manager`
    - `\dt`
    - `select * from task;`
    - `select * from users;`
        - Both have no value in it

- I'll create a new user

```
task_manager=# select * from task;
 id | status |   title    | user_id
----+--------+------------+---------
  2 | TODO   | 30/Oct     |       1
  1 | DONE   | First task |       1
(2 rows)
```
- Now the `status` can read `TODO` or `DONE` as text

## Run the tests with the database up:
- `cd task-manager-api`
- `DB_PASSWORD=taskpass ./test.sh`

- It's expected 13 passing.

```
[INFO] Results:
[INFO]
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  23.059 s
[INFO] Finished at: 2026-10-30T13:50:01+07:00
[INFO] ------------------------------------------------------------------------
```
- That's ok

## Commit the changes
- `cd ..` Go to root
- `git add -A`
- 