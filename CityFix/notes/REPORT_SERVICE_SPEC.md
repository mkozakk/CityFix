# 📋 REPORT SERVICE - API Specification

## 🎯 Overview

Report Service manages city problem reports (zgłoszenia) with full CRUD operations and asynchronous event publishing to RabbitMQ.

---

## 📝 API Endpoints

### 1. Create Report
```
POST /reports
```
- **Auth:** ✅ JWT required (in cookies)
- **Body:**
  ```json
  {
    "title": "string (required)",
    "description": "string (optional)",
    "category": "string (optional)",
    "priority": "string (optional, default: MEDIUM)"
  }
  ```
- **Response:** `201 Created`
  ```json
  {
    "id": 1,
    "userId": 1,
    "title": "Broken street light",
    "description": "Street light at Main St is not working",
    "status": "OPEN",
    "category": "INFRASTRUCTURE",
    "priority": "MEDIUM",
    "createdAt": "2026-01-13T18:00:00Z",
    "updatedAt": "2026-01-13T18:00:00Z"
  }
  ```
- **Security:** User ID automatically assigned from JWT
- **Event:** Publishes `ReportCreatedEvent` to RabbitMQ

---

### 2. Get All Reports
```
GET /reports
```
- **Auth:** ❌ No authentication required (public)
- **Query Params:**
  - `status` (optional): Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
  - `category` (optional): Filter by category
  - `priority` (optional): Filter by priority (LOW, MEDIUM, HIGH, CRITICAL)
  - `userId` (optional): Filter by user ID
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 1,
      "userId": 1,
      "title": "Broken street light",
      "description": "...",
      "status": "OPEN",
      "category": "INFRASTRUCTURE",
      "priority": "MEDIUM",
      "createdAt": "2026-01-13T18:00:00Z",
      "updatedAt": "2026-01-13T18:00:00Z"
    },
    ...
  ]
  ```

---

### 3. Get Report by ID
```
GET /reports/{id}
```
- **Auth:** ❌ No authentication required (public)
- **Path Params:** `id` - Report ID
- **Response:** `200 OK`
  ```json
  {
    "id": 1,
    "userId": 1,
    "title": "Broken street light",
    "description": "Street light at Main St is not working",
    "status": "OPEN",
    "category": "INFRASTRUCTURE",
    "priority": "MEDIUM",
    "createdAt": "2026-01-13T18:00:00Z",
    "updatedAt": "2026-01-13T18:00:00Z"
  }
  ```
- **Error:** `404 Not Found` if report doesn't exist

---

### 4. Update Report
```
PUT /reports/{id}
```
- **Auth:** ✅ JWT required (in cookies)
- **Path Params:** `id` - Report ID
- **Body:**
  ```json
  {
    "title": "string (optional)",
    "description": "string (optional)",
    "status": "string (optional)",
    "category": "string (optional)",
    "priority": "string (optional)"
  }
  ```
- **Response:** `200 OK`
  ```json
  {
    "id": 1,
    "userId": 1,
    "title": "Broken street light - URGENT",
    "description": "Updated description",
    "status": "IN_PROGRESS",
    "category": "INFRASTRUCTURE",
    "priority": "HIGH",
    "createdAt": "2026-01-13T18:00:00Z",
    "updatedAt": "2026-01-13T18:10:00Z"
  }
  ```
- **Security:** User can only update their own reports
- **Error:** 
  - `401 Unauthorized` - Not authenticated
  - `403 Forbidden` - Trying to update another user's report
  - `404 Not Found` - Report doesn't exist

---

### 5. Delete Report
```
DELETE /reports/{id}
```
- **Auth:** ✅ JWT required (in cookies)
- **Path Params:** `id` - Report ID
- **Response:** `204 No Content`
- **Security:** User can only delete their own reports
- **Error:**
  - `401 Unauthorized` - Not authenticated
  - `403 Forbidden` - Trying to delete another user's report
  - `404 Not Found` - Report doesn't exist

---

## 🔐 Security Matrix

| Endpoint | Auth Required | Ownership Check | Public Access |
|----------|---------------|-----------------|---------------|
| POST /reports | ✅ | ❌ (auto-assign user_id) | ❌ |
| GET /reports | ❌ | ❌ | ✅ Public |
| GET /reports/{id} | ❌ | ❌ | ✅ Public |
| PUT /reports/{id} | ✅ | ✅ | ❌ |
| DELETE /reports/{id} | ✅ | ✅ | ❌ |

---

## 📊 Data Models

### Report Entity
```java
{
  "id": Long,
  "userId": Integer (FK to users.id),
  "title": String (max 255 chars, required),
  "description": Text (optional),
  "status": String (default: "OPEN"),
  "category": String (max 100 chars),
  "priority": String (default: "MEDIUM"),
  "createdAt": Timestamp,
  "updatedAt": Timestamp
}
```

### Status Values
- `OPEN` - Nowe zgłoszenie
- `IN_PROGRESS` - W trakcie realizacji
- `RESOLVED` - Rozwiązane
- `CLOSED` - Zamknięte

### Priority Values
- `LOW` - Niska
- `MEDIUM` - Średnia (default)
- `HIGH` - Wysoka
- `CRITICAL` - Krytyczna

### Category Examples
- `INFRASTRUCTURE` - Infrastruktura
- `ROAD` - Drogi
- `LIGHTING` - Oświetlenie
- `WASTE` - Odpady
- `PARK` - Parki
- `OTHER` - Inne

---

## 🐰 RabbitMQ Integration

### Event: ReportCreatedEvent
Published when new report is created via POST /reports

**Exchange:** `cityfix.reports`  
**Routing Key:** `report.created`  
**Message:**
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

**Consumers:**
- Location Service (adds location to report)
- Notification Service (sends notification to admins)

---

## 🧪 Testing Examples

### Via API Gateway (port 8080)

**1. Create Report (Authenticated)**
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

**2. Get All Reports (Public)**
```bash
curl http://localhost:8080/api/reports
```

**3. Get Reports with Filters**
```bash
curl "http://localhost:8080/api/reports?status=OPEN&priority=HIGH"
```

**4. Get Report by ID (Public)**
```bash
curl http://localhost:8080/api/reports/1
```

**5. Update Report (Authenticated)**
```bash
curl -X PUT http://localhost:8080/api/reports/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "status": "IN_PROGRESS",
    "priority": "HIGH"
  }'
```

**6. Delete Report (Authenticated)**
```bash
curl -X DELETE http://localhost:8080/api/reports/1 \
  -b cookies.txt
```

---

## 🔄 User Flow Example

### Creating a Report

```
1. User logs in
   POST /api/users/login
   → JWT cookie set

2. User creates report
   POST /api/reports
   {
     "title": "Pothole on Main St",
     "description": "Large pothole near intersection",
     "category": "ROAD",
     "priority": "HIGH"
   }
   → Report created with user_id from JWT
   → Event published to RabbitMQ

3. Location Service receives event
   → Associates location with report

4. Notification Service receives event
   → Sends notification to admins

5. User views their reports
   GET /api/reports?userId=1
```

---

## ✅ Validation Rules

### Create Report
- `title`: Required, max 255 characters
- `description`: Optional, max 5000 characters
- `category`: Optional, max 100 characters
- `priority`: Optional, must be valid enum value
- `userId`: Automatically assigned from JWT

### Update Report
- All fields optional
- Cannot change `userId` or `createdAt`
- `status`: Must be valid enum value if provided
- `priority`: Must be valid enum value if provided

---

## 🛡️ Security Features

### Authentication
- POST /reports: Requires JWT (user_id extracted from token)
- PUT /reports/{id}: Requires JWT + ownership check
- DELETE /reports/{id}: Requires JWT + ownership check
- GET endpoints: Public (no auth required)

### Authorization
- Users can only UPDATE/DELETE their own reports
- Users can CREATE reports (auto-assigned user_id)
- Everyone can VIEW all reports (public data)

### Ownership Check
```java
if (!report.getUserId().equals(authenticatedUserId)) {
    return 403 FORBIDDEN;
}
```

---

## 📈 Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-01-13T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-01-13T18:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-01-13T18:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only modify your own reports"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-01-13T18:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Report not found with id: 1"
}
```

---

## 🎯 Implementation Checklist

- [ ] ReportController with 5 endpoints
- [ ] ReportService with business logic
- [ ] ReportRepository (JPA)
- [ ] Report entity with validation
- [ ] DTOs: CreateReportRequest, UpdateReportRequest, ReportResponse
- [ ] RabbitMQ publisher for ReportCreatedEvent
- [ ] JWT authentication filter
- [ ] Ownership validation for PUT/DELETE
- [ ] Exception handling
- [ ] Unit tests

---

**Status:** 📋 Specification Ready
**Next Step:** Implementation

