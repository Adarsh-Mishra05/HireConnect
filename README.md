

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

**HireConnect** is a modern job-board and recruitment management platform. The backend is split into **10 independent Spring Boot microservices**, each owning its own database schema. Services communicate via a combination of synchronous REST (through Feign Clients routed by the API Gateway) and asynchronous RabbitMQ events for real-time notifications.

Key platform capabilities:

| Capability | Description |
|---|---|
| 🔐 Authentication | JWT + Google OAuth2 social login with role-based access |
| 💼 Job Management | Full CRUD, search/filter, and featured job promotion |
| 📄 Applications | End-to-end candidate application lifecycle tracking |
| 🗓️ Interviews | Recruiter-driven scheduling, update, and cancellation |
| 💳 Payments | Razorpay-integrated job feature purchases with webhooks |
| 👤 Profiles | Candidate resume upload/download + recruiter profiles |
| 🔔 Notifications | RabbitMQ-driven transactional email + in-app notifications |
| 📊 Monitoring | Spring Boot Admin dashboard for all service health metrics |



<div align="center">

*HireConnect Backend — All rights Reserved!
</div>
