<div align="center">

# 🎓 Campus Connect

**An inter-college event booking platform to discover, register for and manage campus events**

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

</div>

---

## 📖 About

Campus Connect brings events from different colleges onto one platform. Students can browse events, register for the ones they like and sign in securely, while organizers can create and manage events through REST APIs.

## ✨ Features

- Browse event listings from multiple colleges
- User registration and secure authentication
- Event booking and registration
- Event management through REST APIs (create, update, delete)
- Persistent storage with PostgreSQL

> Edit this list so it matches exactly what your app does (for example roles like admin and student, search or filters, or ticket confirmation).

## 🛠️ Tech stack

| Layer | Technology |
|---|---|
| Frontend | Angular |
| Backend | Java, Spring Boot, REST APIs, Authentication |
| Database | PostgreSQL |
| Tools | Git, GitHub, Postman, Maven |

## 📸 Screenshots

<!-- Add screenshots to a /screenshots folder, then uncomment and edit: -->
<!-- ![Home](screenshots/home.png) -->
<!-- ![Event details](screenshots/event.png) -->

## 📂 Project structure

```
Campus-Connect/
├── campusconnect-backend/    # Spring Boot REST API
└── campusconnect-frontend/   # Angular app
```

## 🚀 Getting started

### Prerequisites
- Java 17+
- Maven
- Node.js 18+ and Angular CLI (`npm install -g @angular/cli`)
- PostgreSQL

### 1. Clone the repo
```bash
git clone https://github.com/nitijain18/Campus-Connect.git
cd Campus-Connect
```

### 2. Set up the database
```sql
CREATE DATABASE campusconnect;
```

### 3. Run the backend
Update `campusconnect-backend/src/main/resources/application.properties` with your own database details:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/campusconnect
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
Then start it:
```bash
cd campusconnect-backend
mvn spring-boot:run
```

### 4. Run the frontend
```bash
cd campusconnect-frontend
npm install
ng serve
```
The app opens at `http://localhost:4200`, and the API runs at `http://localhost:8080`.

## 🔌 API overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Log in |
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get one event |
| POST | `/api/events` | Create an event |
| POST | `/api/events/{id}/register` | Register for an event |

> Replace these with your real endpoints.

## 🔮 Future improvements

- Email confirmation for bookings
- Event search and filters by college and category
- Docker setup and deployment on AWS

## 👩‍💻 Author

**Niti Jain**
[LinkedIn](https://linkedin.com/in/nitijain18) · [GitHub](https://github.com/nitijain18) · jainniti1807@gmail.com
