# task-management-system
An AI-assisted full-stack task management system built with Spring Boot and Vue3, featuring task tracking, dashboard analytics, and real-time tech news integration.

---

## 📌 Project Introduction

This project is a lightweight internal task management platform designed for interns and mentors to manage daily tasks collaboratively.

The system supports:

- User login and registration
- Task CRUD operations
- Task status management
- Task filtering and searching
- Dashboard statistics
- Real-time tech news integration
- Role-based task visibility

The project was completed as an MVP within a 48-hour development cycle.

---

## 🚀 Tech Stack

### Backend

- Java 17
- Spring Boot 3.x
- MyBatis-Plus / JPA
- MySQL / H2
- JWT Authentication
- RESTful API

### Frontend

- Vue3
- Vite
- Composition API
- Element Plus
- Axios
- ECharts

### Other

- Git & GitHub
- AI-assisted development
- REST API integration

---

## 📂 Project Structure

```text
task-management-system
├── backend
│   ├── controller
│   ├── service
│   ├── mapper
│   ├── entity
│   ├── dto
│   └── config
│
├── frontend
│   ├── src
│   │   ├── api
│   │   ├── views
│   │   ├── components
│   │   ├── router
│   │   └── stores
│
└── docs
```

---

## ✨ Features

### ✅ User Module

- User login/register
- JWT authentication
- Mock login supported

### ✅ Task Management

- Create task
- Edit task
- Delete task
- Task detail dialog
- Task status update

### ✅ Task Workflow

- Todo
- In Progress
- Completed

### ✅ Dashboard

- My tasks
- Completed statistics
- Task completion chart

### ✅ News Integration

- Fetch real-time tech news from third-party API/RSS
- Search and refresh news
- Associate related news with tasks

### ✅ Additional Features

- Task priority levels
- Role-based access control
- Deadline reminders
- Export tasks to CSV/Excel

---

## 🖼️ Core Pages

- Login Page
- Task List Page
- Dashboard
- Task Detail Dialog
- News Panel

---

## 🔌 API Design

### Authentication

| Method | API | Description |
|---|---|---|
| POST | /api/auth/login | User login |
| POST | /api/auth/register | User register |

### Task Module

| Method | API | Description |
|---|---|---|
| GET | /api/tasks | Get task list |
| POST | /api/tasks | Create task |
| PUT | /api/tasks/{id} | Update task |
| DELETE | /api/tasks/{id} | Delete task |

### News Module

| Method | API | Description |
|---|---|---|
| GET | /api/news | Fetch latest news |
| GET | /api/news/search | Search news |

---

## ⚙️ Local Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-name/task-management-system.git
```

---

### 2️⃣ Backend Setup

```bash
cd backend
```

Configure database in:

```text
application.yml
```

Run backend:

```bash
mvn spring-boot:run
```

---

### 3️⃣ Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

---

## 📊 Database Design

Main tables:

- user
- task
- news
- task_news_relation

---

## 🤖 AI-Assisted Development

AI tools used during development:

- Claude Code
- ChatGPT
- DeepSeek
- GitHub Copilot

### AI-assisted tasks

- API design
- Frontend component generation
- SQL optimization suggestions
- Code review
- UI layout generation
- Bug fixing assistance

### Validation Process

AI-generated code was manually reviewed and tested for:

- Business logic correctness
- API consistency
- Security risks
- Null pointer issues
- Frontend-backend integration

---

## ⚠️ Challenges & Solutions

### Problem 1

CORS issues between frontend and backend.

### Solution

Configured Spring Boot global CORS policy.

---

### Problem 2

JWT token expiration handling.

### Solution

Added Axios interceptor for token validation and redirect.

---

### Problem 3

News API response inconsistency.

### Solution

Added unified response transformation layer.

---

## 📈 Future Improvements

- Drag-and-drop Kanban board
- WebSocket real-time updates
- Docker deployment
- Redis caching
- Role permission management
- Multi-user collaboration

---

## 👨‍💻 Author

- Name: Peng Shanchao
- Internship Full-Stack Practical Test Project

---

## 📄 License

This project is for internship assessment and learning purposes only.
