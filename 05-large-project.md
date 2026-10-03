More infos
- How can we know/see the project's structure?
    - We normally use `tree`
    - But professionals don't rely only on it
        - README.md - Architecture and setup documentation
        - compose.yaml / Kubernetes manifests - infrastructure
        - package.json - Frontend dependencies/scripts
        - pom.xml / build.gradle - backend dependencies 
        - src/ - actual application structure 
    - So tree give the map
    - Configuration and source files tell how the system actually works


- Example of large real-world application
    ```
    my-application/
    │
    ├── frontend/                         # React / Next.js
    │   ├── src/
    │   │   ├── components/               # Reusable UI components
    │   │   │   ├── Button/
    │   │   │   ├── Modal/
    │   │   │   ├── Navbar/
    │   │   │   └── Sidebar/
    │   │   │
    │   │   ├── pages/                    # Main application pages
    │   │   │   ├── Login/
    │   │   │   ├── Dashboard/
    │   │   │   ├── Profile/
    │   │   │   └── Settings/
    │   │   │
    │   │   ├── features/                 # Business features
    │   │   │   ├── auth/                 # Login, logout, registration
    │   │   │   ├── users/
    │   │   │   ├── payments/
    │   │   │   ├── notifications/
    │   │   │   └── orders/
    │   │   │
    │   │   ├── services/                 # API communication
    │   │   │   ├── authApi.js
    │   │   │   ├── userApi.js
    │   │   │   └── orderApi.js
    │   │   │
    │   │   ├── hooks/                    # Custom React hooks
    │   │   ├── context/                  # Global state/context
    │   │   ├── utils/                    # Helper functions
    │   │   └── App.jsx
    │   │
    │   ├── package.json
    │   └── Dockerfile
    │
    ├── backend/                          # Spring Boot / Node / etc.
    │   ├── src/
    │   │   ├── controller/               # HTTP endpoints
    │   │   │   ├── AuthController
    │   │   │   ├── UserController
    │   │   │   ├── OrderController
    │   │   │   └── PaymentController
    │   │   │
    │   │   ├── service/                  # Business logic
    │   │   │   ├── AuthService
    │   │   │   ├── UserService
    │   │   │   └── OrderService
    │   │   │
    │   │   ├── repository/               # Database access
    │   │   │   ├── UserRepository
    │   │   │   └── OrderRepository
    │   │   │
    │   │   ├── model/                    # Database/domain objects
    │   │   │   ├── User
    │   │   │   ├── Order
    │   │   │   └── Payment
    │   │   │
    │   │   ├── security/                 # Authentication/authorization
    │   │   │   ├── SecurityConfig
    │   │   │   ├── JwtFilter
    │   │   │   └── UserDetailsService
    │   │   │
    │   │   └── config/
    │   │
    │   ├── pom.xml
    │   └── Dockerfile
    │
    ├── database/
    │   ├── migrations/                   # Database schema changes
    │   └── seeds/
    │
    ├── infrastructure/
    │   ├── docker/
    │   ├── kubernetes/
    │   │   ├── frontend/
    │   │   ├── backend/
    │   │   └── ingress/
    │   └── terraform/
    │
    ├── tests/
    │   ├── integration/
    │   └── e2e/
    │
    ├── .github/
    │   └── workflows/                    # CI/CD
    │       ├── frontend.yml
    │       └── backend.yml
    │
    ├── compose.yaml
    ├── README.md
    └── .gitignore
    ```

- Architecture in layers
    ```
    Browser
    ↓
    React Login page
    ↓
    authApi.js
    ↓
    POST /api/auth/login
    ↓
    AuthController
    ↓
    AuthService
    ↓
    UserRepository
    ↓
    PostgreSQL
    ↓
    JWT returned
    ↓
    React stores authentication state
    ```

- The K8s would sit underneath the application, handling how the containers are deployed
    ```
                    Kubernetes
                        │
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
    Frontend         API        Background
        Pods           Pods          Pods
        │             │
        └─────────────┤
                        ↓
                Managed Database
    ```

- Your current task manager is actually a small version of this architecture

    ```
    Your project
    │
    ├── task-manager-frontend
    │   └── App.jsx
    │
    └── task-manager-api
        ├── Controller
        ├── Service
        ├── Repository
        └── Database
    ```

Related to repo
- In real professional projects, both monorepos and multiple repositories are common
    - The choice depends on the teams structure, deployment boundaries, and how independently components need to evolve
- Smaller companies might use monorepo (Containing frontend, backend, shared libraries, Docker and compose, and sometimes infra)

- Larger organizations often use multiple repos
    - One for frontend, one for backend, one for infrastructure (Terraform, K8s, AWS config, CI/CD)
    - The repository boundary usually reflects ownership and lifecycle

- For your task manager, a single root repo is a realistic professional approach because the frontend, API, database configuration, and local Docker setup are all parts of one small application. 
    - If the project later grows into independently deployed services with separate teams, you could split them into multiple repos.