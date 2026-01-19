# ✅ ENDPOINTS UPDATED - /me Instead of /{id}

## 📝 API Endpoints (Updated)

### User Service Endpoints

#### 1. Register
```
POST /users/register
```
- **Auth:** ❌ No authentication required
- **Body:** `{ username, email, password, firstName, lastName, phone }`
- **Response:** `201 Created` - User data
- **Public:** Yes

---

#### 2. Login
```
POST /users/login
```
- **Auth:** ❌ No authentication required
- **Body:** `{ username, password }`
- **Response:** `200 OK` - User data + JWT in cookies
- **Cookies:** `JWT_TOKEN` set with HttpOnly, Secure, SameSite=Strict
- **Public:** Yes

---

#### 3. Logout
```
POST /users/logout
```
- **Auth:** ✅ JWT required (in cookies)
- **Body:** None
- **Response:** `200 OK` - `{ message: "Logged out successfully" }`
- **Cookies:** JWT_TOKEN removed
- **Security:** Requires authentication

---

#### 4. Get Current User Profile ⭐ **NEW**
```
GET /users/me
```
- **Auth:** ✅ JWT required (in cookies)
- **Params:** None (automatically uses logged-in user)
- **Response:** `200 OK` - Current user data
- **Security:** Requires authentication
- **Example:**
  ```bash
  curl -X GET http://localhost:8080/api/users/me \
    -H "Cookie: JWT_TOKEN=<token>"
  
  Response:
  {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "firstName": "Alice",
    "lastName": "Smith",
    "phone": "+48123456789"
  }
  ```

---

#### 5. Update Current User Profile ⭐ **NEW**
```
PUT /users/me
```
- **Auth:** ✅ JWT required (in cookies)
- **Body:** `{ firstName, lastName, email, phone }` (all optional)
- **Response:** `200 OK` - Updated user data
- **Security:** Requires authentication
- **Example:**
  ```bash
  curl -X PUT http://localhost:8080/api/users/me \
    -H "Content-Type: application/json" \
    -H "Cookie: JWT_TOKEN=<token>" \
    -d '{
      "firstName": "Alice",
      "lastName": "Johnson",
      "email": "alice.johnson@example.com"
    }'
  
  Response:
  {
    "id": 1,
    "username": "alice",
    "email": "alice.johnson@example.com",
    "firstName": "Alice",
    "lastName": "Johnson",
    "phone": "+48123456789"
  }
  ```

---

#### 6. Health Check
```
GET /users/health
```
- **Auth:** ❌ No authentication required
- **Response:** `200 OK` - `User Service is running`
- **Public:** Yes

---

## 🎯 Advantages of /me Endpoints

| Aspect | Before | After |
|--------|--------|-------|
| **Get own data** | `GET /users/1` (need ID) | `GET /users/me` (auto-detected) |
| **Update own data** | `PUT /users/1` (need ID) | `PUT /users/me` (auto-detected) |
| **Security check** | Check ownership in code | Implicit (can only access own data) |
| **Clarity** | Ambiguous - could be anyone's ID | Clear - it's YOUR data |
| **REST compliance** | Resource-oriented | Resource + context-oriented |

---

## 📊 Complete API Endpoints Summary

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/users/register` | ❌ | Register new user |
| POST | `/users/login` | ❌ | Login and get JWT |
| POST | `/users/logout` | ✅ | Logout and remove JWT |
| GET | `/users/me` | ✅ | Get current user data |
| PUT | `/users/me` | ✅ | Update current user data |
| GET | `/users/health` | ❌ | Health check |

---

## 🧪 Testing Examples

### Via API Gateway (port 8080)

**1. Register**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "SecurePass123",
    "firstName": "Alice",
    "lastName": "Smith"
  }'
```

**2. Login**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "alice",
    "password": "SecurePass123"
  }'
```

**3. Get Current User**
```bash
curl -X GET http://localhost:8080/api/users/me \
  -b cookies.txt
```

**4. Update Current User**
```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "firstName": "Alice",
    "email": "alice.newemail@example.com"
  }'
```

**5. Logout**
```bash
curl -X POST http://localhost:8080/api/users/logout \
  -b cookies.txt
```

---

## ✅ Security Features

✅ **GET /users/me**
- Only authenticated users can access
- Cannot access other users' data (implicit)
- Returns 401 if not authenticated

✅ **PUT /users/me**
- Only authenticated users can modify
- Cannot modify other users' data (implicit)
- Returns 401 if not authenticated

---

## 🔄 Migration from /users/{id}

If you were using `GET /users/1` and `PUT /users/1`, change to:
- `GET /users/me` (no ID needed!)
- `PUT /users/me` (no ID needed!)

---

**Status:** ✅ Endpoints updated and secured!

