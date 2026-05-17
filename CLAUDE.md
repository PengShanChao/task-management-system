# CLAUDE.md — Task Management System

## Project Overview

AI-assisted full-stack task management system for intern-mentor daily task collaboration.
- **Author:** Peng Shanchao (Internship Full-Stack Practical Test)
- **Development cycle:** 48-hour MVP
- **AI tools:** Claude Code, ChatGPT, DeepSeek, GitHub Copilot

---

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Java 17 (Eclipse Temurin 17.0.19), Spring Boot 3.4.5, MyBatis-Plus 3.5.x, MySQL 8.0.37 (prod) / H2 (dev), JWT (jjwt 0.12.x) |
| Frontend | Vue 3.5.x (Composition API), Vite 6.x, Element Plus 2.x, Axios 1.x, ECharts 5.x, Pinia 2.x |
| Build    | Maven 3.9.x (backend), npm (frontend) |

---

## Project Structure

```
task-management-system/
├── backend/
│   ├── src/main/java/com/taskmanager/
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic (interface + impl)
│   │   ├── mapper/         # MyBatis-Plus mappers
│   │   ├── entity/         # Database entities
│   │   ├── dto/            # Request/Response DTOs
│   │   ├── config/         # Spring security, CORS, MyBatis-Plus config
│   │   └── common/         # Unified response, exception handler
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/   # SQL migration scripts
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/            # Axios API wrappers
│   │   ├── views/          # Page-level Vue components
│   │   ├── components/     # Reusable UI components
│   │   ├── router/         # Vue Router config
│   │   ├── stores/         # Pinia state stores
│   │   └── utils/          # Helpers, interceptors
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── docs/                   # API docs, ER diagrams
```

---

## Development Standards

### General

- All code, comments, and commit messages **MUST be in English**.
- Follow **RESTful API** conventions: nouns for resources, HTTP verbs for actions.
- API responses use a **unified envelope**: `{ code, message, data }`.
- Handle **null safety** explicitly at every layer. All DTO fields use `@NotNull` / `@NotBlank` where applicable.
- Backend returns proper HTTP status codes; frontend uses Axios interceptors for global error handling.
- **No magic strings/numbers**: define constants or enums for status values, priority levels, roles.
- Write **unit tests** for service layer and **integration tests** for controllers.
- Every API endpoint must have **Javadoc/comment** describing purpose, params, and return type.

### Backend (Spring Boot)

- **Package prefix:** `com.taskmanager`
- **Naming conventions:**
  - Controller: `XxxController` → handles HTTP, delegates to service, returns `Result<T>`
  - Service: `XxxService` interface + `XxxServiceImpl` implementation
  - Mapper: `XxxMapper` extends MyBatis-Plus `BaseMapper<T>`
  - Entity: matches DB table names (`user` → `User`, `task_news_relation` → `TaskNewsRelation`)
  - DTO: `XxxRequest` (inbound), `XxxResponse` (outbound), `XxxQuery` (search/filter params)
- **Configuration:**
  - CORS: global `WebMvcConfigurer` allowing frontend origin (`http://localhost:5173`)
  - JWT: filter-based, configured in `application.yml` (secret, expiration)
  - MyBatis-Plus: pagination plugin, logical delete configured
- **Security validation rules:**
  - Passwords: bcrypt hashed, min 6 chars
  - JWT: included in `Authorization: Bearer <token>` header
  - RBAC: check role (`USER` / `ADMIN`) on endpoints requiring privileged access
  - Input validation: `@Valid` on all request DTOs with Jakarta validation annotations

### Frontend (Vue 3)

- **Naming conventions:**
  - Views: PascalCase, suffixed with area (`LoginView`, `TaskListView`, `DashboardView`)
  - Components: PascalCase (`TaskCard`, `NewsPanel`, `TaskDialog`)
  - API modules: camelCase, per domain (`authApi.js`, `taskApi.js`, `newsApi.js`)
  - Stores: camelCase (`authStore.js`, `taskStore.js`)
- **Composition API (`<script setup>`) only** — no Options API.
- Use Element Plus components consistently for UI (forms, tables, dialogs, notifications).
- ECharts rendered inside a dedicated composable or chart component.
- Axios base URL configured via Vite proxy (`/api` → backend `:8080`).
- Router guards for authentication: redirect to `/login` if no valid token.

### Database

- **Tables:** `user`, `task`, `news`, `task_news_relation`
- **Primary keys:** auto-increment `BIGINT`
- **Timestamps:** `created_at`, `updated_at` on every table
- **Logical delete:** `deleted` TINYINT(1) DEFAULT 0
- **Indexes:** on foreign keys and commonly queried columns (status, priority, user_id)

### Git

- **Branch naming:** `feature/<name>`, `fix/<name>`, `docs/<name>`
- **Commit messages:** imperative mood, present tense (e.g., "Add task status update endpoint")
- **No committing:** `.env`, `node_modules/`, `target/`, IDE config files

---

## API Design

### Auth — `/api/auth`

| Method | Path             | Description      | Auth |
|--------|------------------|------------------|------|
| POST   | /api/auth/login  | User login       | No   |
| POST   | /api/auth/register | User register | No   |

### Tasks — `/api/tasks`

| Method | Path              | Description   | Auth |
|--------|-------------------|---------------|------|
| GET    | /api/tasks         | List tasks (filterable: status, priority, keyword) | Yes |
| POST   | /api/tasks         | Create task   | Yes  |
| PUT    | /api/tasks/{id}    | Update task   | Yes  |
| DELETE | /api/tasks/{id}    | Delete task   | Yes  |
| GET    | /api/tasks/{id}    | Get task detail | Yes |

### News — `/api/news`

| Method | Path               | Description    | Auth |
|--------|--------------------|----------------|------|
| GET    | /api/news          | Fetch latest   | Yes  |
| GET    | /api/news/search   | Search news    | Yes  |

### Dashboard — `/api/dashboard`

| Method | Path              | Description          | Auth |
|--------|-------------------|----------------------|------|
| GET    | /api/dashboard/stats | User task statistics | Yes |

### Export — `/api/export`

| Method | Path              | Description        | Auth |
|--------|-------------------|--------------------|------|
| GET    | /api/export/tasks | Export tasks CSV   | Yes  |

---

## Entity / DTO Specifications

### User
- id, username, password (bcrypt), email, role (USER/ADMIN), created_at, updated_at

### Task
- id, title, description, status (TODO/IN_PROGRESS/DONE), priority (LOW/MEDIUM/HIGH), deadline, user_id (assignee), created_at, updated_at, deleted

### News
- id, title, summary, url, source, published_at, created_at

### TaskNewsRelation
- id, task_id, news_id, created_at

---

## Error Handling

- **Backend:** Global `@ControllerAdvice` catches validation errors, auth errors, and runtime exceptions. Returns `Result.error(code, message)`.
- **Frontend:** Axios response interceptor catches 401 (redirect to login), 403, 500 and shows `ElMessage` notifications.

---

## AI-Assisted Development Rules

- AI-generated code is **proposal, not final**. Always review for:
  1. Business logic correctness
  2. API contract consistency (frontend ↔ backend)
  3. Security risks (injection, auth bypass, exposed secrets)
  4. Null pointer / undefined handling
  5. Frontend-backend field name alignment
- When the AI suggests an approach, **explain the trade-off** before implementing.
- Prefer the **simplest implementation** that meets requirements. No over-engineering.
