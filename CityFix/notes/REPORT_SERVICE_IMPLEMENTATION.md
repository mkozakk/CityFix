# ✅ REPORT SERVICE - IMPLEMENTATION COMPLETE

## 📊 Zaimplementowane Pliki

### Entities (1)
- ✅ `Report.java` - Entity z JPA annotations

### DTOs (3)
- ✅ `CreateReportRequest.java` - Request dla POST /reports
- ✅ `UpdateReportRequest.java` - Request dla PUT /reports/{id}
- ✅ `ReportResponse.java` - Response dla wszystkich endpoints

### Repository (1)
- ✅ `ReportRepository.java` - JPA Repository z custom queries

### Service (1)
- ✅ `ReportService.java` - Business logic + RabbitMQ event publishing

### Controller (1)
- ✅ `ReportController.java` - REST API (5 endpoints)

### Security (2)
- ✅ `JwtTokenProvider.java` - JWT parsing i validation
- ✅ `JwtAuthenticationFilter.java` - JWT filter dla cookies
- ✅ `SecurityConfig.java` - Spring Security configuration

### Messaging (2)
- ✅ `ReportCreatedEvent.java` - Event class
- ✅ `ReportEventPublisher.java` - RabbitMQ publisher
- ✅ `RabbitMQConfig.java` - RabbitMQ configuration

### Configuration (2)
- ✅ `application.yml` - Updated (JWT + RabbitMQ)
- ✅ `build.gradle.kts` - Updated (JWT + Security dependencies)

---

## 📝 API Endpoints

### POST /reports
- **Auth:** ✅ Required (JWT in cookies)
- **Action:** Create new report
- **User ID:** Extracted from JWT token
- **Event:** Publishes `ReportCreatedEvent` to RabbitMQ

### GET /reports
- **Auth:** ❌ Public
- **Action:** Get all reports

### GET /reports/{id}
- **Auth:** ❌ Public
- **Action:** Get report by ID

### PUT /reports/{id}
- **Auth:** ✅ Required (JWT in cookies)
- **Ownership:** ✅ Validated (user can only update own reports)
- **Action:** Update report

### DELETE /reports/{id}
- **Auth:** ✅ Required (JWT in cookies)
- **Ownership:** ✅ Validated (user can only delete own reports)
- **Action:** Delete report

---

## 🔐 Security Flow

```
1. User logs in to User Service
   → JWT cookie set with username + userId

2. User creates report
   POST /reports (JWT cookie sent automatically)
   ↓
   JwtAuthenticationFilter extracts JWT from cookie
   ↓
   Validates JWT and extracts userId
   ↓
   Sets userId in request attribute
   ↓
   ReportController gets userId from request
   ↓
   ReportService creates report with userId
   ↓
   Event published to RabbitMQ
   ↓
   Response 201 Created

3. User updates report
   PUT /reports/1 (JWT cookie sent automatically)
   ↓
   JwtAuthenticationFilter extracts userId
   ↓
   ReportService validates ownership
   ↓
   If owner → Update ✅
   If not owner → 403 Forbidden ❌
```

---

## 🐰 RabbitMQ Integration

### Configuration
```yaml
Exchange: cityfix.reports (TopicExchange)
Queue: report.created.queue
Routing Key: report.created
```

### Event Flow
```
POST /reports
  ↓
ReportService.createReport()
  ↓
Report saved to database
  ↓
ReportCreatedEvent created
  ↓
ReportEventPublisher.publishReportCreated()
  ↓
RabbitTemplate.convertAndSend()
  ↓
Message sent to RabbitMQ
  ↓
Location Service receives event ✅
Notification Service receives event ✅
```

### Event Structure
```json
{
  "reportId": 1,
  "userId": 1,
  "title": "Broken street light",
  "status": "OPEN",
  "category": "INFRASTRUCTURE",
  "priority": "MEDIUM",
  "createdAt": "2026-01-13T18:00:00Z"
}
```

---

## 🔄 Database Schema

### Table: reports
```sql
id              BIGSERIAL PRIMARY KEY
user_id         INTEGER NOT NULL (FK to users.id)
title           VARCHAR(255) NOT NULL
description     TEXT
status          VARCHAR(50) DEFAULT 'OPEN'
category        VARCHAR(100)
priority        VARCHAR(50) DEFAULT 'MEDIUM'
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

**Already exists in postgres-init.sql** ✅

---

## ✅ Implementation Checklist

### Backend
- [x] Report entity with JPA
- [x] DTOs (Create, Update, Response)
- [x] ReportRepository with JPA
- [x] ReportService with business logic
- [x] ReportController with 5 endpoints
- [x] JWT authentication filter
- [x] Ownership validation (PUT/DELETE)
- [x] RabbitMQ event publisher
- [x] RabbitMQ configuration
- [x] Security configuration
- [x] Exception handling
- [x] Validation annotations

### Configuration
- [x] application.yml updated (JWT + RabbitMQ)
- [x] build.gradle.kts updated (JWT + Security)
- [x] Database schema (already in postgres-init.sql)

### Integration
- [x] JWT token includes userId claim
- [x] UserService generates token with userId
- [x] Report Service extracts userId from JWT
- [x] Cookies forwarded through Gateway

---

## 🧪 Testing Flow

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "alice",
    "password": "SecurePass123"
  }'
```

### 3. Create Report
```bash
curl -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Broken street light",
    "description": "Street light at Main St is not working",
    "category": "INFRASTRUCTURE",
    "priority": "MEDIUM"
  }'
```

### 4. Get All Reports
```bash
curl http://localhost:8080/api/reports
```

### 5. Update Report
```bash
curl -X PUT http://localhost:8080/api/reports/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "status": "IN_PROGRESS",
    "priority": "HIGH"
  }'
```

### 6. Delete Report
```bash
curl -X DELETE http://localhost:8080/api/reports/1 \
  -b cookies.txt
```

---

## 📊 Dependencies Added

### build.gradle.kts
```kotlin
// JWT
implementation("io.jsonwebtoken:jjwt-api:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

// Security
implementation("org.springframework.boot:spring-boot-starter-security")
```

### application.yml
```yaml
jwt:
  secret: your-secret-key-change-in-production...
  cookie:
    name: JWT_TOKEN

rabbitmq:
  exchange:
    reports: cityfix.reports
  queue:
    report-created: report.created.queue
  routing-key:
    report-created: report.created
```

---

## 🚀 Build & Run

```bash
# Clean and rebuild
docker-compose down
docker system prune -a --volumes

# Build without cache
docker-compose build --no-cache

# Start services
docker-compose up

# Watch logs
docker logs cityfix-report-service --follow
```

---

## 🔍 Monitoring

### Check RabbitMQ Management
```
http://localhost:15672
Username: guest
Password: guest

Check:
- Exchange: cityfix.reports
- Queue: report.created.queue
- Bindings
- Messages
```

### Check Logs
```bash
# Report Service logs
docker logs cityfix-report-service

# RabbitMQ logs
docker logs cityfix-rabbitmq

# Database logs
docker logs cityfix-postgres
```

---

## ✅ Status

| Component | Status |
|-----------|--------|
| Report Entity | ✅ Complete |
| DTOs | ✅ Complete |
| Repository | ✅ Complete |
| Service | ✅ Complete |
| Controller | ✅ Complete |
| Security | ✅ Complete |
| RabbitMQ | ✅ Complete |
| Configuration | ✅ Complete |
| **Overall** | ✅ **READY TO BUILD** |

---

**Next Step:** Build and test! 🚀

