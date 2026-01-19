# ✅ POSTMAN COLLECTION UPDATED

## 📦 Updated Collection

**File:** `postman-gateway-collection.json`

### 🔄 Changes Made

#### 1. User Service - Updated Endpoints ⭐
- ❌ Removed: `GET /users/{id}` (old)
- ✅ Added: `GET /users/me` (new - get current user)
- ❌ Removed: `PUT /users/{id}` (old)
- ✅ Added: `PUT /users/me` (new - update current user)
- ✅ Kept: POST /users/register
- ✅ Kept: POST /users/login
- ✅ Kept: POST /users/logout
- ✅ Kept: GET /users/health

#### 2. Report Service - Added CRUD Endpoints ⭐
- ✅ Added: `POST /reports` (create report)
- ✅ Added: `GET /reports` (get all reports)
- ✅ Added: `GET /reports/{id}` (get report by ID)
- ✅ Added: `PUT /reports/{id}` (update report)
- ✅ Added: `DELETE /reports/{id}` (delete report)

---

## 📋 Collection Structure

```
CityFix API Gateway - Complete
├── User Service (6 endpoints)
│   ├── Register User
│   ├── Login
│   ├── Get Current User ⭐ NEW
│   ├── Update Current User ⭐ NEW
│   ├── Logout
│   └── Health Check
│
└── Report Service (5 endpoints) ⭐ NEW
    ├── Create Report
    ├── Get All Reports
    ├── Get Report by ID
    ├── Update Report
    └── Delete Report
```

---

## 🔧 Variables

```json
{
  "gateway_url": "http://localhost:8080/api",
  "report_id": "1"
}
```

**Note:** `user_id` variable removed (no longer needed with /me endpoints)

---

## 🧪 How to Use

### 1. Import to Postman
```
File → Import → Select postman-gateway-collection.json
```

### 2. Test User Service Flow
```
1. Register User
   → POST /users/register

2. Login
   → POST /users/login
   → Cookies automatically saved

3. Get Current User
   → GET /users/me
   → Uses cookies automatically

4. Update Current User
   → PUT /users/me
   → Uses cookies automatically

5. Logout
   → POST /users/logout
```

### 3. Test Report Service Flow
```
1. Login first (to get JWT)
   → POST /users/login

2. Create Report
   → POST /reports
   → Uses cookies automatically
   → user_id assigned from JWT

3. Get All Reports
   → GET /reports
   → No auth needed (public)

4. Get Report by ID
   → GET /reports/1
   → No auth needed (public)

5. Update Report
   → PUT /reports/1
   → Uses cookies automatically
   → Only owner can update

6. Delete Report
   → DELETE /reports/1
   → Uses cookies automatically
   → Only owner can delete
```

---

## 📝 Request Examples

### User Service

#### Register User
```json
POST {{gateway_url}}/users/register

{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

#### Login
```json
POST {{gateway_url}}/users/login

{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

#### Get Current User
```
GET {{gateway_url}}/users/me
(cookies sent automatically)
```

#### Update Current User
```json
PUT {{gateway_url}}/users/me

{
  "firstName": "Jonathan",
  "lastName": "Smith",
  "email": "jonathan.smith@example.com",
  "phone": "+48987654321"
}
```

---

### Report Service

#### Create Report
```json
POST {{gateway_url}}/reports

{
  "title": "Broken street light",
  "description": "Street light at Main St is not working",
  "category": "INFRASTRUCTURE",
  "priority": "MEDIUM"
}
```

#### Get All Reports
```
GET {{gateway_url}}/reports
```

#### Get Report by ID
```
GET {{gateway_url}}/reports/{{report_id}}
```

#### Update Report
```json
PUT {{gateway_url}}/reports/{{report_id}}

{
  "title": "Broken street light - URGENT",
  "description": "Street light at Main St is not working - needs immediate attention",
  "status": "IN_PROGRESS",
  "priority": "HIGH"
}
```

#### Delete Report
```
DELETE {{gateway_url}}/reports/{{report_id}}
```

---

## 🔐 Authentication

### Automatic Cookie Management
Postman automatically handles cookies:
1. Login → JWT cookie saved
2. Subsequent requests → Cookie sent automatically
3. Logout → Cookie removed

### Manual Cookie Management
If needed, you can view/edit cookies:
```
Postman → Cookies (bottom right)
→ Manage Cookies
→ View JWT_TOKEN
```

---

## ✅ Testing Checklist

### User Service
- [ ] Register new user
- [ ] Login (check JWT cookie is set)
- [ ] Get current user profile
- [ ] Update current user profile
- [ ] Logout (check JWT cookie is removed)
- [ ] Health check

### Report Service
- [ ] Create report (authenticated)
- [ ] Get all reports (public)
- [ ] Get report by ID (public)
- [ ] Update own report (authenticated)
- [ ] Try to update another user's report (should fail with 403)
- [ ] Delete own report (authenticated)
- [ ] Try to delete another user's report (should fail with 403)

---

## 🎯 Quick Start

```bash
# 1. Start services
docker-compose up

# 2. Import collection to Postman
File → Import → postman-gateway-collection.json

# 3. Run requests in order:
   a. Register User
   b. Login (cookies saved automatically)
   c. Create Report
   d. Get All Reports
   e. Update Report
   f. Delete Report
```

---

## 📊 Response Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST (register, create report) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | Not authorized (wrong ownership) |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Internal error |

---

**Status:** ✅ Collection Updated & Ready
**File Location:** `/postman-gateway-collection.json`

