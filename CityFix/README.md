# CityFix - System Zgłoszeń Miejskich

Platforma mikrousługowa do zarządzania zgłoszeniami problemów miejskich (dziury w drogach, oświetlenie, graffiti).

## Zadanie Aplikacji

CityFix umożliwia mieszkańcom zgłaszanie problemów miejskich oraz administracji efektywne zarządzanie zgłoszeniami. System zapewnia:

- 👤 **Rejestrację i autentykację** użytkowników (JWT)
- 📝 **Tworzenie zgłoszeń** z geolokalizacją
- 📊 **Zarządzanie statusami** (Pending → In Progress → Resolved)
- 📋 **Audit logging** wszystkich operacji
- 🔔 **Asynchroniczną komunikację** przez RabbitMQ

## Architektura

```
┌─────────────────────────────────────────────────┐
│              Clients (Web/Mobile)               │
└────────────────────┬────────────────────────────┘
                     │ HTTPS
                     ▼
┌─────────────────────────────────────────────────┐
│           API Gateway (Port 8080)               │
│  - Routing  - JWT Auth  - Load Balancing       │
└──────┬──────────────┬──────────────┬────────────┘
       │              │              │
       ▼              ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌──────────────┐
│User Service │ │Report Service│ │ Log Service  │
│  (8081)     │ │  (8082)      │ │  (8084)      │
│             │ │              │ │              │
│• Register   │ │• Create      │ │• Audit Logs  │
│• Login      │ │• Update      │ │• Analytics   │
│• Profile    │ │• Delete      │ │• Query       │
└──────┬──────┘ └──────┬───────┘ └──────┬───────┘
       │               │                │
       └───────┬───────┴────────┬───────┘
               │                │
        ┌──────▼──────┐  ┌──────▼──────┐
        │ PostgreSQL  │  │  RabbitMQ   │
        │   (5432)    │  │   (5672)    │
        └─────────────┘  └─────────────┘
```

### Serwisy

**User Service (8081)**
- Autentykacja JWT z cookie-based sessions
- CRUD operacje na użytkownikach
- Role: USER, ADMIN

**Report Service (8082)**
- CRUD operacje na zgłoszeniach
- Geolokalizacja (latitude/longitude)
- Kategorie: ROAD_DAMAGE, LIGHTING, GRAFFITI
- Statusy: PENDING, IN_PROGRESS, RESOLVED, REJECTED

**Log Service (8084)**
- Centralne logowanie audit trails
- Konsumpcja eventów z RabbitMQ
- Analytics i statystyki

**API Gateway (8080)**
- Routing: `/api/users/**` → User Service
- Routing: `/api/reports/**` → Report Service
- Routing: `/api/logs/**` → Log Service
- JWT validation

### Stack Technologiczny

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 15
- **Message Broker**: RabbitMQ 3.12
- **Monitoring**: Prometheus + Grafana
- **Container**: Docker + Docker Compose
- **CI/CD**: GitHub Actions

## Uruchomienie Aplikacji

### Wymagania
- Docker & Docker Compose
- Java 17+ (do lokalnego developmentu)

### Start z Docker Compose

```bash
# Uruchom wszystkie serwisy
docker-compose up -d --build

# Sprawdź status
docker-compose ps

# Oczekiwany output:
# cityfix-gateway         Up (healthy)
# cityfix-user-service    Up (healthy)
# cityfix-report-service  Up (healthy)
# cityfix-log-service     Up (healthy)
# cityfix-postgres        Up (healthy)
# cityfix-rabbitmq        Up (healthy)
```

### Weryfikacja

```bash
# Health checks
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Report Service
curl http://localhost:8084/actuator/health  # Log Service
```

### Dostęp do Serwisów

| Serwis | URL | Credentials |
|--------|-----|-------------|
| API Gateway | http://localhost:8080 | - |
| RabbitMQ UI | http://localhost:15672 | guest / guest |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3001 | admin / admin |

### Zatrzymanie

```bash
docker-compose down

# Z usunięciem wolumenów (baza danych)
docker-compose down -v
```

## Testy Jednostkowe

### Uruchamianie Wszystkich Testów

```bash
# Windows
.\gradlew.bat test

# Linux/Mac
./gradlew test
```

## CI/CD Workflows

Projekt zawiera GitHub Actions workflows w `.github/workflows/`:

**1. ci-cd.yml** - Główny pipeline
- ✅ Uruchamia testy (3 serwisy parallel)
- ✅ CodeQL security analysis
- ✅ Build Docker images
- ✅ Deploy (opcjonalnie)

**Trigger:** Push do main/develop, PR, tag v*

```bash
git push origin main
# → GitHub Actions automatycznie uruchamia testy i build
```

**2. release.yml** - Automatyczne release'y
- 📦 Tworzy GitHub Release
- 📦 Generuje changelog
- 📦 Buduje wersjonowane obrazy

**Trigger:** Tag v*.*.*

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
# → Automatyczny release
```

## API Endpoints

### 🔐 Autentykacja

Wszystkie endpointy wymagające autentykacji używają **JWT w formie HTTP Cookie** (nie header Authorization):

- **Cookie Name:** `TOKEN__cityfix`
- **Transport:** HTTP Cookie (ustawiana automatycznie przez `/users/login`)
- **Flags:** HttpOnly (bezpieczne przed XSS), SameSite=Strict (ochrona CSRF)
- **Expires:** 24 godziny (konfigurowalne)

**Przepływ autentykacji:**
```bash
1. POST /users/login → Serwer ustawia cookie TOKEN__cityfix w response
2. curl -b cookies.txt → Automatycznie wysyła cookie w następnych requestach
3. Gateway waliduje JWT z cookie
4. userId jest ekstrahowany z tokenu i przekazywany do serwisów
5. POST /users/logout → Serwer usuwa cookie
```

---

### User Service (8081)

#### POST /users/register
Rejestracja nowego użytkownika (bez autentykacji)

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+48123456789"
  }'

# Response: 201 Created
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

#### POST /users/login
Logowanie użytkownika (ustawia JWT cookie)

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'

# Response: 200 OK + Set-Cookie: TOKEN__cityfix=<token>; HttpOnly; SameSite=Strict
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

#### POST /users/logout
Wylogowanie użytkownika (usuwa JWT cookie) - **wymaga cookie JWT**

```bash
curl -X POST http://localhost:8080/api/users/logout \
  -b cookies.txt

# Response: 200 OK
{
  "message": "Logged out successfully"
}
```

#### GET /users/me
Pobiera profil bieżącego użytkownika - **wymaga cookie JWT**

```bash
curl -X GET http://localhost:8080/api/users/me \
  -b cookies.txt

# Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

#### PUT /users/me
Aktualizacja profilu bieżącego użytkownika - **wymaga cookie JWT**

```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "phone": "+48987654321"
  }'

# Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+48987654321"
}
```

#### GET /users/health
Health check serwisu (bez autentykacji)

```bash
curl http://localhost:8081/users/health

# Response: 200 OK
"User Service is running"
```

---

### Report Service (8082)

#### POST /reports
Tworzenie nowego zgłoszenia - **wymaga cookie JWT**

```bash
curl -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Duża dziura w jezdni",
    "description": "Niezalatana dziura przy ulicy Głównej",
    "category": "ROAD_DAMAGE",
    "latitude": 52.2297,
    "longitude": 21.0122,
    "address": "ul. Główna 15, Warszawa"
  }'

# Response: 201 Created
{
  "id": 1,
  "title": "Duża dziura w jezdni",
  "description": "Niezalatana dziura przy ulicy Głównej",
  "category": "ROAD_DAMAGE",
  "status": "PENDING",
  "latitude": 52.2297,
  "longitude": 21.0122,
  "address": "ul. Główna 15, Warszawa",
  "userId": 1,
  "createdAt": "2026-01-16T10:30:00Z",
  "updatedAt": "2026-01-16T10:30:00Z"
}
```

#### GET /reports
Pobiera wszystkie zgłoszenia (bez autentykacji)

```bash
curl http://localhost:8080/api/reports

# Response: 200 OK
[
  {
    "id": 1,
    "title": "Duża dziura w jezdni",
    "category": "ROAD_DAMAGE",
    "status": "PENDING",
    "latitude": 52.2297,
    "longitude": 21.0122,
    "address": "ul. Główna 15, Warszawa",
    "userId": 1,
    "createdAt": "2026-01-16T10:30:00Z",
    "updatedAt": "2026-01-16T10:30:00Z"
  }
]
```

#### GET /reports/{id}
Pobiera szczegóły konkretnego zgłoszenia (bez autentykacji)

```bash
curl http://localhost:8080/api/reports/1

# Response: 200 OK
{
  "id": 1,
  "title": "Duża dziura w jezdni",
  "description": "Niezalatana dziura przy ulicy Głównej",
  "category": "ROAD_DAMAGE",
  "status": "PENDING",
  "latitude": 52.2297,
  "longitude": 21.0122,
  "address": "ul. Główna 15, Warszawa",
  "userId": 1,
  "createdAt": "2026-01-16T10:30:00Z",
  "updatedAt": "2026-01-16T10:30:00Z"
}
```

#### PUT /reports/{id}
Aktualizacja zgłoszenia (tylko twórca może edytować) - **wymaga cookie JWT**

```bash
curl -X PUT http://localhost:8080/api/reports/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "PILNE: Bardzo duża dziura w jezdni",
    "description": "Niebezpieczna dziura, samochody niszczą się",
    "category": "ROAD_DAMAGE"
  }'

# Response: 200 OK
{
  "id": 1,
  "title": "PILNE: Bardzo duża dziura w jezdni",
  "description": "Niebezpieczna dziura, samochody niszczą się",
  "category": "ROAD_DAMAGE",
  "status": "PENDING",
  "latitude": 52.2297,
  "longitude": 21.0122,
  "address": "ul. Główna 15, Warszawa",
  "userId": 1,
  "createdAt": "2026-01-16T10:30:00Z",
  "updatedAt": "2026-01-16T11:45:00Z"
}

# Response: 403 Forbidden (jeśli nie jesteś twórcą)
# Response: 404 Not Found (jeśli zgłoszenie nie istnieje)
```

#### DELETE /reports/{id}
Usunięcie zgłoszenia (tylko twórca może usuwać) - **wymaga cookie JWT**

```bash
curl -X DELETE http://localhost:8080/api/reports/1 \
  -b cookies.txt

# Response: 204 No Content

# Response: 403 Forbidden (jeśli nie jesteś twórcą)
# Response: 404 Not Found (jeśli zgłoszenie nie istnieje)
```

#### GET /reports/health
Health check serwisu (bez autentykacji)

```bash
curl http://localhost:8082/reports/health

# Response: 200 OK
"Report Service is running"
```

---

### Log Service (8084)

#### GET /logs
Pobiera logi auditowe (wymaga hasła w query param)

```bash
curl "http://localhost:8080/api/logs?password=someverylongandsecurestringusedforauthorization&limit=100"

# Response: 200 OK
[
  {
    "id": 1,
    "userId": 1,
    "eventType": "USER_REGISTERED",
    "timestamp": "2026-01-16T10:30:00Z"
  },
  {
    "id": 2,
    "userId": 1,
    "eventType": "REPORT_CREATED",
    "entityId": 1,
    "timestamp": "2026-01-16T10:31:00Z"
  }
]

# Response: 401 Unauthorized (jeśli password jest nieprawidłowy)
```

#### GET /logs?userId={id}
Filtruje logi dla konkretnego użytkownika (wymaga hasła)

```bash
curl "http://localhost:8080/api/logs?password=someverylongandsecurestringusedforauthorization&userId=1"

# Response: 200 OK - lista logów użytkownika
```

#### GET /logs?eventType={type}
Filtruje logi dla konkretnego typu zdarzenia (wymaga hasła)

```bash
curl "http://localhost:8080/api/logs?password=someverylongandsecurestringusedforauthorization&eventType=REPORT_CREATED"

# Response: 200 OK - lista logów zdarzenia
```

#### GET /logs/health
Health check serwisu (bez autentykacji)

```bash
curl http://localhost:8084/logs/health

# Response: 200 OK
"Log Service is running"
```

---

### Podsumowanie autoryzacji

| Endpoint | Autentykacja | Metoda | Notatka |
|----------|:---:|:---:|---------|
| POST /users/register | ❌ Nie | - | Publiczny |
| POST /users/login | ❌ Nie | - | Ustawia cookie JWT |
| POST /users/logout | ✅ Tak (cookie) | JWT | - |
| GET /users/me | ✅ Tak (cookie) | JWT | - |
| PUT /users/me | ✅ Tak (cookie) | JWT | - |
| GET /users/health | ❌ Nie | - | Publiczny |
| POST /reports | ✅ Tak (cookie) | JWT | Wymaga userId z cookie |
| GET /reports | ❌ Nie | - | Publiczny |
| GET /reports/{id} | ❌ Nie | - | Publiczny |
| PUT /reports/{id} | ✅ Tak (cookie) | JWT | Tylko twórca |
| DELETE /reports/{id} | ✅ Tak (cookie) | JWT | Tylko twórca |
| GET /reports/health | ❌ Nie | - | Publiczny |
| GET /logs | ❌ Nie | Query param | Wymaga password |
| GET /logs?userId | ❌ Nie | Query param | Wymaga password |
| GET /logs?eventType | ❌ Nie | Query param | Wymaga password |
| GET /logs/health | ❌ Nie | - | Publiczny |

### Error Responses

```bash
# 400 Bad Request
{
  "timestamp": "2026-01-16T10:30:00Z",
  "status": 400,
  "error": "Validation error",
  "message": "Invalid request body"
}

# 401 Unauthorized
{
  "timestamp": "2026-01-16T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing JWT token"
}

# 403 Forbidden
{
  "timestamp": "2026-01-16T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only modify your own resources"
}

# 404 Not Found
{
  "timestamp": "2026-01-16T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Report not found"
}

# 500 Internal Server Error
{
  "timestamp": "2026-01-16T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed"
}
```

## Monitoring

### Prometheus Alerts (9 alertów)

| Alert | Threshold | Severity |
|-------|-----------|----------|
| ServiceDown | Serwis offline 1min | 🔴 Critical |
| HighErrorRate | > 5% błędów | 🔴 Critical |
| HighResponseTime | P95 > 1s | ⚠️ Warning |
| HighMemoryUsage | > 85% RAM | ⚠️ Warning |
| HighCPUUsage | > 80% CPU | ⚠️ Warning |

**Podgląd alertów:** http://localhost:9090/alerts

### Grafana Dashboard

**URL:** http://localhost:3001 (admin/admin)

**Dashboard:** CityFix Microservices Monitoring

**6 paneli:**
- HTTP Request Rate
- Response Time
- Status Codes
- Memory Usage
- CPU Usage
- Error Rate
