Clone repo
- `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project"`
- Create a new folder 'clone'
- Select the SSH copy on the 
- Open git bash
    - `git clone git@github.com:ricardo-eiji/task-manager-fullstack.git`

Open vs code
- `cd Clone/`
- `cd task-manager-fullstack/`

Set up environment
- `cp .env.example .env`
- `docker ps` Make sure nothing else is using the ports first
    - Nothing running on 8080, 5173, 5433

Explaining the .env file and other files related:
- The `DB_PASSWORD` is used/associated with `compose.yaml` at root
    - So for this variable, we can use any value 

- But for `ADMIN_PASSWORD` we cannot add any value
    - The file associated with the API login is the `SecurityConfig.java`
    - It needs to be `admin:admin123` because it's hardcoded in the frontend `App.jsx`
        - In the line `const authHeader = 'Basic ' + btoa('admin:admin123')`

    - Professional: Typically use login + session/cookie or short-lived tokens, with credentials stored securely on the server.
        - Hardcoding in frontend is not professional because anyone can inspect the browser and see the credentials 

- I'm leaving as `change_me` and `admin123`

- Run the compose
    - `docker compose up --build -d`

- Test 
    - `curl -i http://localhost:8080/tasks`
        - Should display 401
    - `curl -u admin:admin123 http://localhost:8080/tasks`
        - Should display []

    - Browser: http://localhost:5173/
        - I was able to Create, Complete, and delete the tasks

    - `docker compose down`