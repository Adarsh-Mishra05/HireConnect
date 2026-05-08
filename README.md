<!-- [Disha Gujar]: Added READ.md file -->
<div align="center">

# 🔗 HireConnect — Backend Microservices

**A production-grade, cloud-native recruitment platform built on Spring Boot microservices**

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-blue?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=for-the-badge&logo=apachekafka)](https://kafka.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven)](https://maven.apache.org/)

> Built by **Disha Gujar** | Full-Stack Java Microservices Architecture

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-system-architecture)
- [Microservices](#-microservices-at-a-glance)
- [Technology Stack](#-technology-stack)
- [Service Communication](#-service-communication-flow)
- [API Reference](#-api-endpoint-reference)
- [Setup & Running Locally](#-setup--running-locally)
- [Environment Variables](#-environment-variables)
- [Branch Strategy](#-branch-strategy)
- [Author](#-author)

---

## 🌐 Overview

**HireConnect** is a modern job-board and recruitment management platform. The backend is split into **10 independent Spring Boot microservices**, each owning its own database schema. Services communicate via a combination of synchronous REST (through Feign Clients routed by the API Gateway) and asynchronous Kafka events for real-time notifications.

Key platform capabilities:

| Capability | Description |
|---|---|
| 🔐 Authentication | JWT + Google OAuth2 social login with role-based access |
| 💼 Job Management | Full CRUD, search/filter, and featured job promotion |
| 📄 Applications | End-to-end candidate application lifecycle tracking |
| 🗓️ Interviews | Recruiter-driven scheduling, update, and cancellation |
| 💳 Payments | Razorpay-integrated job feature purchases with webhooks |
| 👤 Profiles | Candidate resume upload/download + recruiter profiles |
| 🔔 Notifications | Kafka-driven transactional email + in-app notifications |
| 📊 Monitoring | Spring Boot Admin dashboard for all service health metrics |

---

## 🏗️ System Architecture

```
                        ┌───────────────────────────────────────┐
                        │         Angular Frontend (4200)        │
                        └──────────────────┬────────────────────┘
                                           │ HTTP/REST
                        ┌──────────────────▼────────────────────┐
                        │          API Gateway  (:8080)          │
                        │   JWT Validation · CORS · Routing      │
                        └──┬──────┬───────┬──────┬──────┬───────┘
                           │      │       │      │      │
              ┌────────────▼┐  ┌──▼──┐ ┌─▼───┐ ┌▼────┐ ┌▼──────────┐
              │ Auth Service│  │ Job │ │ App │ │ Int │ │  Payment  │
              │   (:8081)   │  │ Svc │ │ Svc │ │ Svc │ │  Service  │
              │ JWT · OAuth │  │8083 │ │8084 │ │8085 │ │  (:8086)  │
              └─────────────┘  └──┬──┘ └──┬──┘ └──┬──┘ └───────────┘
                                  │       │       │
                        ┌─────────▼───────▼───────▼───────────┐
                        │          Apache Kafka Broker          │
                        │   notification-events · app-events    │
                        └─────────────────┬─────────────────────┘
                                          │ Kafka Consumer
                        ┌─────────────────▼─────────────────────┐
                        │       Notification Service (:8087)      │
                        │   Kafka Consumer · Email · In-App       │
                        └────────────────────────────────────────┘

         ┌──────────────────────┐    ┌──────────────────────────┐
         │  Profile Service     │    │   Service Registry        │
         │  (:8082)             │    │   (Eureka Server :8761)   │
         │  Resume · Profile    │    │   Service Discovery       │
         └──────────────────────┘    └──────────────────────────┘

                        ┌─────────────────────────────────────┐
                        │      Admin Server (:9090)            │
                        │  Health · Metrics · Logs Dashboard   │
                        └─────────────────────────────────────┘
```

---

## 📦 Microservices at a Glance

| # | Service | Port | Database | Description |
|---|---------|------|----------|-------------|
| 1 | **service-registry** | `8761` | — | Netflix Eureka service discovery server |
| 2 | **admin-server** | `9090` | — | Spring Boot Admin monitoring dashboard |
| 3 | **api-gateway** | `8080` | — | JWT auth gateway + request routing |
| 4 | **auth-service** | `8081` | `hireconnect_auth` | Registration, login, JWT, OAuth2, OTP |
| 5 | **profile-service** | `8082` | `hireconnect_profile` | Candidate/recruiter profiles & resumes |
| 6 | **job-service** | `8083` | `hireconnect_job` | Job CRUD, search, featured jobs |
| 7 | **application-service** | `8084` | `hireconnect_application` | Job applications & status tracking |
| 8 | **interview-service** | `8085` | `hireconnect_interview` | Interview scheduling & management |
| 9 | **payment-service** | `8086` | `hireconnect_payment` | Razorpay payment processing |
| 10 | **notification-service** | `8087` | `hireconnect_notification` | Kafka-driven email & in-app alerts |

---

## 🛠️ Technology Stack

### Core Framework
```
Spring Boot 3.x         — Microservice foundation
Spring Security 6.x     — JWT + OAuth2 resource server
Spring Cloud Gateway    — API Gateway with reactive routing
Spring Cloud Netflix    — Eureka service registry & discovery
Spring Cloud OpenFeign  — Declarative HTTP inter-service clients
```

### Data & Messaging
```
MySQL 8.x               — Relational persistence per service
Spring Data JPA         — ORM with Hibernate
Apache Kafka            — Asynchronous event streaming
```

### Security
```
JJWT (io.jsonwebtoken)  — JWT generation, signing, and validation
Spring OAuth2 Client    — Google Social Login
BCrypt                  — Password hashing
```

### Payments & Communication
```
Razorpay Java SDK       — Payment orders & webhook signature verification
JavaMail (SMTP)         — Transactional email dispatch
```

### Observability
```
Spring Boot Admin       — Centralized health & metrics dashboard
Spring Boot Actuator    — Per-service health endpoints
SLF4J + Logback         — Structured logging across all services
```

### Build & Tooling
```
Apache Maven            — Dependency management & build
Lombok                  — Boilerplate reduction
MapStruct               — DTO ↔ Entity mapping
Jakarta Validation      — Request DTO input validation
```

---

## 🔄 Service Communication Flow

### Synchronous (Feign Client → API Gateway → Target Service)
```
Frontend → API Gateway → Auth Service        (login, register, validate)
Frontend → API Gateway → Job Service         (browse, search, post jobs)
Frontend → API Gateway → Application Service (apply, track applications)
Frontend → API Gateway → Interview Service   (schedule, view interviews)
Frontend → API Gateway → Payment Service     (create order, verify)
Frontend → API Gateway → Profile Service     (profile, resume upload)
```

### Asynchronous (Kafka Events)
```
Auth Service        ──► [user-registered]     ──► Notification Service
Application Service ──► [application-status]  ──► Notification Service
Interview Service   ──► [interview-scheduled] ──► Notification Service
```

### Internal Cross-Service Calls (Feign)
```
Application Service ──► Job Service       (verify job exists + is open)
Application Service ──► Profile Service   (candidate preview for recruiter)
Interview Service   ──► Application Svc   (get application summary)
Payment Service     ──► Job Service       (mark job as featured on success)
```

---

## 📡 API Endpoint Reference

### Auth Service `/api/v1/auth`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | ❌ | Register new user (CANDIDATE/RECRUITER) |
| POST | `/login` | ❌ | Email + password login, returns JWT |
| POST | `/refresh` | ❌ | Exchange refresh token for new access token |
| POST | `/forgot-password` | ❌ | Send OTP to registered email |
| POST | `/reset-password` | ❌ | Reset password using OTP |
| GET | `/validate` | ✅ | Validate JWT — used internally by API Gateway |
| GET | `/oauth2/authorize/google` | ❌ | Initiate Google OAuth2 login |

### Job Service `/api/jobs`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | ✅ RECRUITER | Create a new job posting |
| PUT | `/{jobId}` | ✅ RECRUITER | Update an existing job |
| DELETE | `/{jobId}` | ✅ RECRUITER | Delete a job posting |
| GET | `/recruiter/me` | ✅ RECRUITER | Get recruiter's own jobs |
| GET | `/` | ❌ | List all open jobs |
| GET | `/{jobId}` | ❌ | Get single open job details |
| GET | `/search` | ❌ | Filter jobs by keyword/location/type/salary |
| PUT | `/{jobId}/feature` | ✅ RECRUITER | Mark job as featured (requires payment) |

### Application Service `/api/applications`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | ✅ CANDIDATE | Submit a job application |
| GET | `/me` | ✅ CANDIDATE | Get my applications |
| GET | `/me/{applicationId}` | ✅ CANDIDATE | Get single application |
| GET | `/recruiter` | ✅ RECRUITER | Get all applications for recruiter's jobs |
| PUT | `/{applicationId}/status` | ✅ RECRUITER | Update application status |
| GET | `/job/{jobId}` | ✅ RECRUITER | Get applications by job |

### Interview Service `/api/interviews`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | ✅ RECRUITER | Schedule an interview |
| GET | `/recruiter` | ✅ RECRUITER | Get recruiter's scheduled interviews |
| GET | `/candidate` | ✅ CANDIDATE | Get candidate's interviews |
| GET | `/{interviewId}` | ✅ | Get interview details |
| PUT | `/{interviewId}` | ✅ RECRUITER | Update interview details |
| DELETE | `/cancel/{interviewId}` | ✅ RECRUITER | Cancel an interview |

### Payment Service `/api/payments`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/create-order` | ✅ RECRUITER | Create Razorpay payment order |
| POST | `/verify` | ✅ RECRUITER | Verify payment signature |
| GET | `/me` | ✅ | Get my payment history |
| POST | `/webhook` | ❌ | Razorpay server webhook endpoint |

### Profile Service `/api/profiles`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | ✅ | Create profile |
| GET | `/me` | ✅ | Get own profile |
| PUT | `/me` | ✅ | Update own profile |
| POST | `/resume/upload` | ✅ CANDIDATE | Upload resume (PDF/DOCX) |
| GET | `/resume/my` | ✅ CANDIDATE | Download own resume |
| GET | `/resume/recruiter/{candidateId}/{jobId}` | ✅ RECRUITER | Download candidate resume |

### Notification Service `/api/notifications`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | ✅ | Get paginated notifications |
| GET | `/unread-count` | ✅ | Get unread notification count |
| PUT | `/{notificationId}/read` | ✅ | Mark notification as read |
| DELETE | `/{notificationId}` | ✅ | Delete a notification |

---

## 🚀 Setup & Running Locally

### Prerequisites

```bash
Java 17+
Apache Maven 3.8+
MySQL 8.x (running on port 3306)
Apache Kafka (running on localhost:9092)
Git
```

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Dishagujar26/HireConnect-Backend.git
cd HireConnect-Backend
git checkout feature/backend-microservices
```

### Step 2 — Create MySQL Databases

```sql
CREATE DATABASE hireconnect_auth;
CREATE DATABASE hireconnect_profile;
CREATE DATABASE hireconnect_job;
CREATE DATABASE hireconnect_application;
CREATE DATABASE hireconnect_interview;
CREATE DATABASE hireconnect_payment;
CREATE DATABASE hireconnect_notification;
```

### Step 3 — Configure Each Service

Edit `src/main/resources/application.yml` (or `application.properties`) for each service and set the environment variables listed in the section below.

### Step 4 — Start Services in Order

> ⚠️ **Order matters!** Start infrastructure services first.

```bash
# 1. Service Registry (Eureka) — must be first
cd service-registry && mvn spring-boot:run

# 2. Admin Server
cd admin-server && mvn spring-boot:run

# 3. API Gateway
cd api-gateway && mvn spring-boot:run

# 4. Core services (can be parallel)
cd auth-service         && mvn spring-boot:run
cd profile-service      && mvn spring-boot:run
cd job-service          && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# 5. Dependent services (need job + application services)
cd application-service  && mvn spring-boot:run
cd interview-service    && mvn spring-boot:run
cd payment-service      && mvn spring-boot:run
```

### Step 5 — Verify All Services Registered

Open Eureka Dashboard: **http://localhost:8761**

You should see all 10 services registered under "Instances currently registered with Eureka".

Open Admin Dashboard: **http://localhost:9090**

---

## 🔧 Environment Variables

Configure the following variables in each service's `application.yml`:

### auth-service
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hireconnect_auth
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000
  mail:
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### payment-service
```yaml
razorpay:
  key-id: ${RAZORPAY_KEY_ID}
  key-secret: ${RAZORPAY_KEY_SECRET}
  webhook-secret: ${RAZORPAY_WEBHOOK_SECRET}
```

### notification-service
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_APP_PASSWORD}
  kafka:
    bootstrap-servers: localhost:9092
```

---

## 🌿 Branch Strategy

```
main                         ← Production-ready, stable releases only
│
└── dev                      ← Integration branch for tested features
    │
    └── feature/backend-microservices  ← Active development branch (this branch)
        │
        ├── admin-server/
        ├── api-gateway/
        ├── auth-service/
        ├── application-service/
        ├── interview-service/
        ├── job-service/
        ├── notification-service/
        ├── payment-service/
        ├── profile-service/
        └── service-registry/
```

| Branch | Purpose |
|--------|---------|
| `main` | Clean, empty baseline — production releases only |
| `dev` | Empty integration baseline — merge point for tested features |
| `feature/backend-microservices` | **All 10 microservices** — full source code |

---

## 👩‍💻 Author

<div align="center">

**Disha Gujar**

*Full-Stack Java Developer | Spring Boot Microservices | Cloud-Native Architecture*

[![GitHub](https://img.shields.io/badge/GitHub-Dishagujar26-black?style=for-the-badge&logo=github)](https://github.com/Dishagujar26)

</div>

---

<div align="center">

*HireConnect Backend — Built with ❤️ using Spring Boot Microservices*

</div>
