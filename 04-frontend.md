For this Frontend part
- We'll use `react + Vite` (Standard real-world choice)
- Fast, modern tooling, what most companies use today (replaced older tools like Create React App).

Setup of the frontend part
- Go to root cd `30 - mini java project`
- Run: `npm create vite@latest task-manager-frontend -- --template react`
    - Select `ESLint`
    - Install with npm and start now? Yes
    - It will be running on terminal (Open a new terminal)

    - When running the command, it create a new folder `task-manager-frontend` with files on it
        - `npm -v`


- Open new terminal
    - For any other commands (git, editing files, etc.).

- Open browser to `http://localhost:5173/` 
    - We should see default Vite + React page.


Connect the frontend with the API
- We need to enable CORS (Cross Origin Resource Sharing) on the backend
    - The API will reject request from the browser, unless explicitly allowed
    - in the `SecurityConfig.java` 
        - We'll add a `@Bean` for CORS configuration (corsConfigurationSource())
        - And update the @Bean of `securityFilterChain` (securityFilterChain)

    - After modifying both parts
        - `docker compose up --build`

    - Open new terminal
        - `curl -i -u admin:admin123 http://localhost:8080/tasks` (after the 'admin' it should be the password in .env)
            - It should show 200 and the list of the tasks
            - 200 means backend successfully handled the request

        - `curl -i -X OPTIONS http://localhost:8080/tasks -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET"`
            - CORS preflight (OPTIONS /tasks)
            ```
            HTTP/1.1 200
            Access-Control-Allow-Origin: http://localhost:5173
            Access-Control-Allow-Methods: GET,POST,PUT,DELETE
            Access-Control-Allow-Credentials: true
            ```
            - This confirms the backend is allowing the frontend at http://localhost:5173 

            - Preflight request is accepted

Fetch tasks from API
- The file with the HTML and content is `task-manager-frontend\src\App.jsx`

- We'll need to replace  it's content

- How to run after modyfing the code
    - `cd task-manager-frontend/`
    - `npm run dev` (It will run in the terminal the react server with Vite)
    - Check on the browser `http://localhost:5173/`

- Add some curls
    - `curl -i -u admin:admin123 http://localhost:8080/tasks` (To see the list of tasks)
    - `curl -u admin:admin123 -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title": "Second task"}'` (I'll add another task)

    - After adding the second task via terminal, I refreshed the webpage and it showed the second task

I'll do the git setup to be able to commit the evolution
- `cd task-manager-frontend/` (Where we should init git)
- `git status`
- `git init`
- `cat .gitignore`
    - It should have node_modules, dist, etc.

- `git add .`
- `git commit -m "Initial commit: fetch and display tasks from API"`


- Now I'll add the second version of `task-manager-frontend\src\App.jsx`
    - Added, and now we have a **add-task form** 
    - Stop the react server `ctrl + c` and `npm run dev`
    - Ps: we have 3 terminals (One for the docker, react, curl)

- `git add .`
- `git commit -m "Add form to create new tasks"`

Create a new github repo for frontend and connect to the local git
- Create a new repo (task-manager-frontend)
- `git remote add origin git@github.com:ricardo-eiji/task-manager-frontend.git`
- `git branch -M main`
- `git push -u origin main`
- `git remote -v`
- `git branch -vv`

Third version of the `src/App.jsx`
- add two handlers and buttons

- We've added all the `const`

- Key concept: same pattern as handleSubmit — fire the request, then re-fetch the list so the UI reflects the server's current state. 
    - This is a simple way to keep frontend and backend in sync without more complex state management.

 - Save, test completing and deleting a task in the browser. Paste any issues.
    -  `ctrl + c` and `npm run dev`
    - Browser: `http://localhost:5173/`


Add a "undo" button for completed tasks 
- We need a new backend endpoint first, since you only have "mark complete," not "mark incomplete

    - Go to `TaskController.java` in task-manager-api

        - Added `@PutMapping("/{id}/undo")`
        - Restar the docker compose (`docker compose up --build`)

- Now we can go to `src/App.jsx` to add a new handler 
    - Add the `const handleUndo ` 
    - And then update the list rendering to show "Undo" instead of "Complete" when a task is already DONE
        - Inside the `<ul>` we have added the new button

- Test the new code
    - First terminal: `docker compose up --build`
    - Second terminal: `ctrl + c` and `npm run dev`

- Commit both repos:
    - > in task-manager-api
    - git add .
    - git commit -m "Add undo endpoint to revert task to TODO"
    - git push

    - > in task-manager-frontend
    - git add .
    - git commit -m "Add undo button for completed tasks"
    - git push

Now, we are going to do basic styling 
- We'll use Tailwind CSS (Instead of plain CSS)
    - it's the dominant choice in modern React projects (used heavily at companies like GitHub, Shopify, OpenAI's tools, etc.)
    - Much more common than plain CSS files for new projects today.

- Install Tailwind
    - `cd task-manager-frontend/`
    - `npm install tailwindcss @tailwindcss/vite`

- Configure the `vite.config.js` - add the Tailwind plugin
    - We added the `import tailwindcss` and the `tailwindcss()`
    
- In the `src/index.css` I removed everything and added `@import "tailwindcss";`

- Restart dev server
    - To confirm no errors, then I'll give you styled JSX for your task list 

    - `ctrl + c` → `npm run dev`

    - It's expected to see the white screen without any styling

    - Tailwind is installed, but your JSX has no Tailwind classes yet, so nothing visually changes except the default browser stylesheet being stripped away (Tailwind resets default styles).


- Now we can add the styling in `src/App.jsx` in the "return" statement
    - Add the new code

- Now we can save and check the browser
    - `ctrl + c` → `npm run dev`
    - Refresh: `http://localhost:5173/`

- It worked!

Commit basic styling 
- add styling

Understanding the connection and code

``` jsx
const fetchTasks = () => {
    fetch('http://localhost:8080/tasks', {
      headers: { Authorization: authHeader }
    })
      .then(res => res.json())
      .then(data => setTasks(data))
      .catch(err => console.error(err))
  }
```

- Inside the React App.jsx, in the "fetch("http://localhost:8080/tasks")" it goes to Spring Boot API
    - Then in the "setTasks(data)" the React displays the tasks


    - Also, in the `SecurityConfig.java` we've added the code to allow React app to communicate with the API
        - The browser's React app (localhost:5173) can communicate with API (localhost:8080)
        - We've added the (Imports of CORS) and the method (corsConfigurationSource())

- Related to 2 public repos 
    - Although the repos ar eseparated, the running apps communicate over HTTP
    - frontend (React + Vite) | api (Spring Boot API)

    - Browser → React/Vite localhost:5173 → fetch() → Spring Boot API localhost:8080 → Database

    ```jsx
    // So the App.jsx connects to the API here

    fetch('http://localhost:8080/tasks', {
        headers: { Authorization: authHeader }
    })
    ```
    - It basically connects to the running API server at localhost:8080
        - Not the GitHub or API's repository... But the running server

    - How to run with the 2 public repos?
        - Clone both repos separately:
            - `git clone <frontend-repo>`
            - `git clone <api-repo>`

        - In the API repo
            - `docker compose up --build -d`
            - Browser: `localhost:8080` (Where Spring Boot API runs)

        - In the frontend repo 
            - `npm install`
            - `npm run dev`
            - Browser: `localhost:5173` (Where React/Vite runs)

        Then:
        ```
        localhost:5173 (React)
                │
                │ HTTP fetch()
                ↓
        localhost:8080 (Spring Boot)
                │
                ↓
            Database
        ```

        - The repositories being seperated doesn't cause a problem
        - It's common structure: frontend and backend can be developed, versioned, and deployed independently

    - Question: What about using docker compose in frontend too?    
        - We'd structure it like:
        ```
        GitHub
        ├── task-manager-frontend
        │   ├── src/
        │   ├── package.json
        │   ├── Dockerfile
        │   └── ...
        │
        └── task-manager-api
            ├── src/
            ├── Dockerfile
            ├── compose.yaml
            └── ...
        ```

        - Then the architecture becomes:
        ```
                        Docker Compose
                               │  
                 ┌─────────────┼─────────────┐
                 ↓             ↓             ↓
            Frontend        Backend       Database
            React/Vite     Spring Boot     PostgreSQL
            :5173          :8080           :5432
        ```

        - For produdction
            - We don't want to run the Vite development server inside Docker
            - React source → npm run build → dist/ → Nginx container → Browser
            
        - So the frontend Dockerfile could use a multi-stage build:
            - Node container → npm install, npm run build → Nginx container → serve React's dist/

    - For the current project
        - Recommend keeping the 2 GitHub repos seperate
        - Then Adding Docker to support both

        ```
        task-manager/
        │
        ├── task-manager-frontend/
        │   └── Dockerfile
        │
        ├── task-manager-api/
        │   └── Dockerfile
        │
        └── compose.yaml
        ```
        - The compose.yaml can then orchestrate the 2 containers (plus the database)

    - Thoughts
        - Professionals choose the approach based on the environment and goal
        - For portfolio, having both a normal devevlopment workflow and a Docker Compose workflow is actually a nice demonstration of that distinction 

    - For real project / professionals commonly use
        - GitHub → Frontend repo and Backend repo → Deployment platform → Frontend and API and Backend

        - Git
            - Seperate repos or a monorepo
            - Pull requests, code review, branches, CI/CD
        - Docker
            - Frontend → Docker image
            - Backend → Docker image
            - Database → Managed servicer or container depending on environment

        - Docker Compose
            - Very common for local development
            - Devs can start the entire application with `docker compose up`

        - Then we have CI/CD (github actions) and Kubernetes (Useful for more complex production infrastructure, multiple services, replicas, automatic scaling, rolling deployments, etc.)

        - What we have now:
            - React/Vite → Spring Boot → PostgreSQL
            
        - Professional local setup
            ```
            Docker Compose
            ├── frontend
            ├── api
            └── postgres
            ```

        - Professional deployment
            ```
            GitHub
            ↓
            GitHub Actions
            ↓
            Docker images
            ↓
            Cloud deployment
            ├── Frontend
            ├── API
            └── Managed PostgreSQL
            ```

        - Large-scale architecture
            ```
            GitHub → CI/CD → Kubernetes
                                ├── Frontend
                                ├── API
                                ├── Other services
                                └── Workers
                                    ↓
                            Managed database
            ```
            - But for portolio the stage 3 is ok
            - It demonstrates (React, Spring Boot, REST, authentication, CORS, Docker, PostgreSQL, Git/GitHub, CI/CD)


            ```
            Kubernetes
            ├── Frontend → Pod(s)
            ├── API      → Pod(s)
            ├── Services → Pod(s)
            └── Workers  → Pod(s)
                ↓
            Managed Database
            ```
            - The Frontend, API, services, and workers are normally deployed to K8s as Pods (Managed by Deployments)

            - But the database is a managed database (AWS RDS, Google Cloud SQL, Azure Database, etc.)
                - It's provided by a cloud provider
                - It's outside the K8s cluster 

            ```
            Kubernetes
            │
            ├── Frontend Deployment
            │      └── 2 Pods
            │
            └── API Deployment
                    └── 3 Pods
                            │
                            ↓
                    Managed PostgreSQL
            ```
            - That's an example

Future plan
- We can add a Dockerfile for the React app
    - The frontend will be the third service in docker-compose
    - Compose will run API, DB and frontend together 

- Then we'll deploy to AWS 
    - I think I'll use EC2 for hosting the application
    - Also, we'll use AWS RDS
    - And I'm thinking on adding a domain name pointing to the application (namecheap)

Dockerfile for React 
- It will be a multi-stage
    - Build with node and serve with Nginx 

    - The final image doesn't carry Node.js at all, much smaller

- Create the `.dockerignore`
    - And add "node_modules, dist, .git"

- Ps: The App.jsx currently calls the "http://localhost:8080/tasks" directly
    - The React code uses `http://localhost:8080/tasks`
    - The browser makes that request, not the Nginx container
    - So it works locally even if React is inside Docker
    - But later, the user's browser's localhost would mean their own computer
        - Not our AWS server, so the API request would fail 

    - Instead of localhost:8080, we would use something like https://api.yourdomain.com

    - Usually, we replace the hardcoded URL with an environment variable
        - In App.jsx
            - We'll have `const API_URL = import.meta.env.VITE_API_URL` and `fetch(`${API_URL}/tasks`)`

        - Related to the .env files, have different .env files for different environments (not both URLs in the same .env)
            ```
            task-manager-frontend/
            ├── .env.development
            ├── .env.production
            ├── src/
            │   └── App.jsx
            └── package.json
            ```
            - For `.env.development` VITE_API_URL=http://localhost:8080
            - For `.env.production` VITE_API_URL=https://api.yourdomain.com
            
        - How does App.jsx know about .env?
            - When Vite runs the React app, it sees `import.meta.env.VITE_API_URL`

            - And it looks for the env var named `VITE_API_URL`

            - Then the 
                - fetch(`${API_URL}/tasks`)
                - fetch("http://localhost:8080/tasks")
                - fetch("https://api.yourdomain.com/tasks")

        - How Vite knows if it's development or production the .env?
            - It knows by the command we're running
                - npm run dev
                    - Vite uses the development mode, so it loads `.env.development` and `VITE_API_URL=http://localhost:8080`

                - npm run build
                    - Vite loads .env.production and `VITE_API_URL=https://api.yourdomain.com`

        - How production React deployments happen:
            - For Docker, the `npm run build` happen while building the docker image 

            - The Docker compose, we write in the file the `command: npm run dev`

            - For K8s, 
                - Build a separate image/config for each environment 
                - Use a runtime config approach 

After creating the Dockerfile, we'll test the image
- `docker build -t task-manager-frontend .`
- `docker images`

Now, we'll create a docker-compose file outside the folders to connect with all the folders
- Added the `compose.yml` outside the folder 
    - We defined the 3 containers 

- Stop all servers
    - With only one terminal run `docker compose up --build`

- Check the browser `http://localhost:5173/`

- Problem:
    - I tried to create the task, but it's not creating
    - Frontend is running, but the API request can't reach the backend

    - We need to create a .env in the root 
        - DB_PASSWORD=your_database_password
        - ADMIN_PASSWORD=your_admin_password


    - `docker compose down`
    - `docker compose up --build -d`

    - I'll leave the .env at the root 
        - Then we need to create the .gitignore also at the root 

    - I've added the correct db and user password in .env

    - `docker compose down`
    - `docker compose up --build -d`

    - With `docker compose ps` we can see the app service it not working

    - `docker compose down -v`
        - We needed to add this -v 
        - It also removes pgdata, causing PostgreSQL to initialize from scratch using the password currently in your .env.
    - `docker compose up --build -d`

    - `curl -i -u admin:admin123 http://localhost:8080/tasks`
         - after the `docker compose down -v` it worked

    - Infos:
        - docker compose down
            - It's safe for the database (Keeps the data)

        - docker compose down -v
            - means "take everything down and remove the Compose volumes too."

        - docker compose up --build -d
        - `http://localhost:5173/`


- Next steps
    - ✅ Backend container
    - ✅ PostgreSQL container + persistent volume
    - ✅ Frontend Dockerfile + Nginx
    - ✅ Add frontend to compose.yaml
    - So we can run all 3 services (frontend, api, database) with one command

    - Prepare for AWS
    - Create an RDS PostgreSQL database
    - Move the backend from containerized PostgreSQL → RDS
    - Configure AWS security groups/networking
    - Put the frontend/API behind a domain + HTTPS

Few improvements
- Added in Dockerfile of frontend in "build" 
``` dockerfile
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
```

- Added this in the frontend service
``` yml
args:
        VITE_API_URL: http://localhost:8080
```

- In the App.jsx 
    - We've added `const API_URL = import.meta.env.VITE_API_URL` outside the function App()

    - Instead of `fetch('http://localhost:8080/tasks'` we replaced with `fetch(${API_URL}/tasks` 
        - Instead of `http://localhost:8080` I've added `${API_URL}`

    - Every hardcoded URL replaced with ${API_URL}

    - API_URL -> `const API_URL = import.meta.env.VITE_API_URL`

    - This VITE_API_URL comes from docker-compose file
        ```
        args:
            VITE_API_URL: http://localhost:8080
        ```

- Related to .env files
    - It's saying I should create a .env.development file 

    - Because now we have 2 development workflows
        - `npm run dev` -> .env.development -> VITE_API_URL

        - `docker compose up --build` -> compose.yaml → build.args -> VITE_API_URL

        - Both ultimately give your React code:
            - `const API_URL = import.meta.env.VITE_API_URL;`

    - So the .env.development is for `npm run dev` so Vite can read `task-manager-frontend/.env.development`

- We created 2 .env files inside task-manager-frontend
    - One for .development and one for .production

- Test locally without docker to see if the URL still works
    - Run the Compose of the API folder 
        - `cd task-manager-api/`
        - `docker compose down`
        - `docker compose up --build -d`
    - Go to frontend folder
        - `cd task-manager-frontend/`
        - `npm run dev`
        - Browser: `http://localhost:5173/`

    - For this setup, the curl is
        - `curl -i -u admin:admin123 http://localhost:8080/tasks`

        - This admin:admin123 is from the API login
            - Backend expects admin and admin123
            - The file that define this is the `task-manager-api\SecurityConfig.java`
                - It defines the credentials and that all API requests must have these credentials 

    - What was the problem:
        - It was in the `App.jsx` 
        - The [fetch(`${API_URL}/tasks`, {]   Needed to backticks (Not single quotes)

    - What is the database related to it?
        - It's the PostgreSQL running in Docker (docker compose)
        - How to access it?
            - `docker ps` to see the container name
                - task-manager-api-db-1
            - `docker exec -it task-manager-api-db-1 psql -U taskuser -d task_manager`
                - `\l` List the databases
                - `\dt` List the tables 
                - `SELECT * FROM task;` List the tasks of the table
                - `\q` Exit

        - The compose.yaml defines how PostgreSQL is created, but the actual database is saved in Docker volume "pgdata"

        - Ps: Each compose file normally creates its own volume and network, so the databases can be seperate PostgreSQL instances.

        
    - To stop the compose, we need to be in the same path to run `docker compose down`

- Test the full Docker stack at root
    - `cd ..`
    - `docker compose down -v`
    - `docker compose up --build -d`

    - Browser: `http://localhost:5173/`

Until now: Local dev (Non-docker) and Docker stack (3 services) both are working fine end-to-end

Commit and push
- Since we only have .git inside each folder (frontend and api folder) we need to go to each and check the situation of them

- Commit and push for `task-manager-api` folder
    - `cd task-manager-api/`
    - `git status`
    - `git diff` (There are only comments added into compose and securityconfig)
    - `git add .`
    - `git commit -m "just comments in docker-compose and SecurityConfig"`
    - `git push`

- Commit and push for `task-manager-frontend`
    - `cd task-manager-frontend/`
    - `git status`
    - `git diff`
    - `git add .`

        ```
        git commit -m "update
        - fixed App.jsx code to fetch the correct API_URL
        - added .env for development and production for frontend React App
        - Created Dockerfile for frontend
        - 'npm run dev' and 'docker compose up' at root is working"
        ```

    - `git push`


Next:
- I think I'll create a monorepo and add everything (including my notes)
- I believe I'll also add the folder of infra inside this monorepo

- AWS: create RDS PostgreSQL instance
- AWS: launch EC2, install Docker
- Point compose.yml's app service at RDS instead of containerized db
- Domain (Namecheap) → EC2, HTTPS


Create the README and add the files to create and push the public repo
- Create the `.env.example`
    ```
    DB_PASSWORD=change_me
    ADMIN_PASSWORD=change_me
    ```

- Add .env in the .gitignore
    ```.gitignore
    .env
    node_modules/
    target/
    dist/
    *.class
    ```

- Ps: We'll work with **plain copied folders**, instead of submodules (pointing to existing repos)

- Since we have .git inside the 2 folders, we'll have to remove them (Otherwise, Git will treat them as submodules)
    - `cd "/mnt/c/Users/ASUS/Videos/30 - mini java project"`
    - `rm -rf task-manager-api/.git`
    - `rm -rf task-manager-frontend/.git`

        ```bash
        ricardo@LAPTOP-5NQFTCQB:/mnt/c/Users/ASUS/Videos/30 - mini java project$ cd task-manager-api/
        
        ricardo@LAPTOP-5NQFTCQB:/mnt/c/Users/ASUS/Videos/30 - mini java project/task-manager-api$ git status
        
        fatal: not a git repository (or any parent up to mount point /mnt)
        Stopping at filesystem boundary (GIT_DISCOVERY_ACROSS_FILESYSTEM not set).
        ```
    
    - `git init`

    - `git status`
    - `git add .`

    - `rm -rf task-manager-maven/.git` Can't be a submodule
    - `git rm --cached task-manager-maven`
    - `git add task-manager-maven`

- Create a Public Repo in GitHub
    - Name: task-manager-fullstack
    - Public
    - No README
    - No .gitignore

- Connect the local repo to public repo
    - `git remote add origin git@github.com:ricardo-eiji/task-manager-fullstack.git`

    - `git branch -M main`

    - `git push -u origin main`

- in task-manager-maven in GitHub it's showing a submodule link
    - `ls -la task-manager-maven/.git`
    - `rm -rf task-manager-maven/.git`
    - `git rm -r --cached task-manager-maven`
    `git add task-manager-maven`
    ``