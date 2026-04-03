# TaskSphere — Project & Task Management Platform

<div align="center">

**Production API:** [https://api.tasksphere.io.vn](https://api.tasksphere.io.vn)  
**Swagger UI:** [https://api.tasksphere.io.vn/swagger-ui.html](https://api.tasksphere.io.vn/swagger-ui.html)  
**Frontend repo:** [tasksphere-frontend](https://github.com/lenhien06/tasksphere-frontend)

![Java](https://img.shields.io/badge/Java-21-007396?style=flat&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=flat&logo=githubactions)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

---

## Overview

TaskSphere là nền tảng quản lý dự án và công việc theo mô hình **Agile/Scrum**, cung cấp REST API đầy đủ cho các team phần mềm. Hệ thống xử lý toàn bộ vòng đời của một dự án — từ lập kế hoạch sprint, phân công task, theo dõi tiến độ realtime, đến xuất báo cáo burndown và velocity.

### Điểm nổi bật

- **Bảo mật production-grade** — JWT với refresh token rotation, blacklist trên Redis, rate limiting (Bucket4j), virus scan file upload (ClamAV)
- **Realtime** — WebSocket/STOMP cho notification và cập nhật Kanban board tức thì
- **Agile-native** — Sprint management, Kanban custom workflow, backlog, dependency tracking, task recurrence
- **Extensible** — Custom fields theo từng project, webhook outbound, activity log toàn hệ thống
- **Deployment sẵn sàng** — Dockerized, CI/CD tự động lên VPS qua GitHub Actions

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Client Layer                         │
│           Frontend (React)  /  Mobile  /  API Consumer   │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS / WSS
┌────────────────────▼────────────────────────────────────┐
│                  Spring Boot 3.5 (Java 21)               │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  REST API    │  │  WebSocket   │  │  Scheduler    │  │
│  │  (39 ctrl)   │  │  /STOMP      │  │  (cron jobs)  │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬───────┘  │
│         │                 │                   │          │
│  ┌──────▼─────────────────▼───────────────────▼───────┐  │
│  │              Service / Domain Layer                  │  │
│  └──────┬──────────────────────────────────────────────┘  │
│         │                                                │
│  ┌──────▼──────┐  ┌──────────────┐  ┌────────────────┐  │
│  │  MySQL 8.0  │  │   Redis 7    │  │  MinIO (S3)    │  │
│  │  (JPA/HQL)  │  │  cache/BL    │  │  file storage  │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## Features

### Authentication & Security
| Feature | Detail |
|---|---|
| JWT Authentication | Access token (1h) + Refresh token (7 ngày), rotation on refresh |
| Token Blacklist | Logout revokes token instantly via Redis |
| RBAC | Role-based access control: ADMIN / PROJECT_MANAGER / MEMBER / VIEWER |
| Rate Limiting | Bucket4j — chống brute force và spam API |
| Virus Scan | ClamAV scan mọi file upload trước khi lưu |
| XSS Protection | Jsoup sanitize HTML comment, MIME-type detection (Apache Tika) |
| OTP Flow | Email OTP cho đăng ký và reset mật khẩu |

### Project & Task Management
| Feature | Detail |
|---|---|
| Multi-project | Nhiều project độc lập, mỗi project có workflow riêng |
| Kanban Board | Custom columns, drag-and-drop position, soft delete |
| Sprint Management | Tạo/start/complete sprint, batch assign tasks, burndown chart |
| Backlog | Quản lý tasks chưa vào sprint, drag to sprint |
| Task Detail | Priority, label, assignee, due date, story points, estimate |
| Sub-tasks | Cây task con không giới hạn độ sâu |
| Checklist | Checklist items với reorder, progress tracking |
| Dependencies | Blocked-by / blocks giữa các tasks |
| Recurrence | Tạo task lặp lại theo lịch (daily/weekly/monthly/custom) |
| Custom Fields | Text, number, date, select, multi-select — định nghĩa per-project |
| Version / Release | Gắn tasks vào version release, theo dõi tiến độ release |

### Collaboration
| Feature | Detail |
|---|---|
| Real-time Notifications | WebSocket push cho assign, comment, mention, due date |
| Comments | Rich text, @mention thành viên, thread reply |
| Activity Log | Lịch sử toàn bộ thay đổi của task và project |
| Member Invite | Mời qua email (link + OTP), hoặc thêm thẳng (admin) |
| Worklog | Log giờ làm việc, thống kê theo sprint / thành viên |
| Daily Digest | Email tóm tắt công việc hằng ngày (cron job) |

### Reporting & Export
| Feature | Detail |
|---|---|
| Burndown Chart | Sprint burndown theo ngày |
| Sprint Velocity | So sánh velocity qua các sprint |
| Member Performance | Story points hoàn thành, số tasks, worklog |
| Export | Excel (Apache POI) và PDF (iText) cho báo cáo sprint |

### Developer & Ops
| Feature | Detail |
|---|---|
| Swagger / OpenAPI | Full API docs tại `/swagger-ui.html` |
| Webhooks | Outbound webhook khi task thay đổi trạng thái |
| Health Check | `/actuator/health` — tích hợp monitoring |
| Soft Delete | Toàn hệ thống dùng `deleted_at`, không mất data |
| Optimistic Locking | `@Version` trên Task entity, tránh race condition |
| Docker Compose | 1 lệnh khởi động toàn bộ stack |
| CI/CD | GitHub Actions — build, validate, deploy SSH lên VPS, health check |

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.5 |
| Security | Spring Security + JJWT | 0.12.5 |
| Database | MySQL | 8.0 |
| ORM | Spring Data JPA / Hibernate | — |
| Cache | Redis (Lettuce pool) | 7 |
| Messaging | WebSocket + STOMP (SockJS) | — |
| File Storage | MinIO (S3-compatible) | — |
| Virus Scan | ClamAV (capybara client) | 2.1.2 |
| Rate Limiting | Bucket4j | 8.10.1 |
| API Docs | SpringDoc OpenAPI | 2.8.5 |
| Export | Apache POI + iText PDF | 5.2.5 / 5.5.13 |
| HTML Sanitizer | Jsoup | 1.17.2 |
| Build | Maven Wrapper | — |
| Container | Docker + Docker Compose | — |
| CI/CD | GitHub Actions | — |

---

## Quick Start

### Yêu cầu
- Java 21+
- Docker Desktop

### 1. Clone

```bash
git clone https://github.com/lenhien06/tasksphere-be.git
cd tasksphere-be
```

### 2. Khởi động infrastructure

```bash
# MySQL (port 3307) + Redis (port 6379) + MinIO + ClamAV
docker compose up db redis -d
```

### 3. Chạy ứng dụng

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

API sẵn sàng tại `http://localhost:8080/api/v1`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Tài khoản demo (tự động seed)

| Email | Mật khẩu | Role |
|---|---|---|
| `admin@tasksphere.local` | `Admin@123456` | Admin |
| `pm@tasksphere.local` | `Demo@123456` | Project Manager |
| `dev1@tasksphere.local` | `Demo@123456` | Member |
| `viewer@tasksphere.local` | `Demo@123456` | Viewer |

> Demo project `DEMO` — 10 tasks, 2 sprints, Kanban board có sẵn.

### Biến môi trường tuỳ chọn

| Tính năng | Biến | Lấy ở đâu |
|---|---|---|
| Gửi email | `MAIL_USERNAME`, `MAIL_PASSWORD` | Gmail → App Passwords |
| Captcha | `TURNSTILE_SECRET_KEY` | Cloudflare Dashboard |
| File upload | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | AWS / MinIO |

---

## API Reference

Full documentation tại Swagger UI. Các nhóm endpoint chính:

| Module | Prefix |
|---|---|
| Authentication | `/api/auth` |
| Projects & Members | `/api/projects`, `/api/projects/{id}/members` |
| Kanban & Tasks | `/api/projects/{id}/columns`, `/api/tasks` |
| Sprint & Backlog | `/api/projects/{id}/sprints`, `/api/projects/{id}/backlog` |
| Comments & Mentions | `/api/tasks/{id}/comments` |
| Custom Fields | `/api/projects/{id}/custom-fields` |
| Reports & Export | `/api/projects/{id}/reports`, `/api/export` |
| Notifications | `/api/notifications` |
| Webhooks | `/api/webhooks` |
| Admin | `/api/admin` |

**Xem tài liệu chi tiết cho Frontend:**
- [`docs/FE_ACTIVITY_API.md`](docs/FE_ACTIVITY_API.md) — Activity log
- [`docs/FE_BACKLOG_API.md`](docs/FE_BACKLOG_API.md) — Backlog & Sprint

---

## Deployment

### Docker (production-like)

```bash
# Tạo .env từ template
cp .env.example .env
# Điền: JWT_SECRET, DB_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD

docker compose up -d
docker compose logs -f app
```

### CI/CD (GitHub Actions)

Push lên branch `main` tự động:
1. Validate config (không có `localhost` trong prod)
2. SSH vào VPS, chạy deploy script
3. Verify health endpoint `GET /actuator/health`
4. Notify nếu thất bại

Secrets cần cấu hình trên GitHub: `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`, `VPS_PORT`

---

## Project Structure

```
src/main/java/com/zone/tasksphere/
├── controller/     # 18 REST controllers
├── service/        # Business logic
├── repository/     # Spring Data JPA repositories
├── entity/         # JPA entities (UUID PKs, soft delete)
├── dto/            # Request / Response DTOs
├── config/         # Security, Redis, WebSocket, OpenAPI
├── component/      # Schedulers, seeders, migration runners
└── exception/      # Global exception handler
```

---

## License

[MIT](LICENSE) © 2025 TaskSphere
