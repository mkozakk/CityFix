# Log Service - Security Documentation

## Overview
Log Service jest usługą odpowiedzialną za przechowywanie i udostępnianie logów audytu dla całego systemu CityFix.

## Routing
- **Direct Access**: `http://localhost:8084/logs`
- **Gateway Access**: `http://localhost:8080/api/logs` (routing przez API Gateway)

## Security - Password Protection

### Mechanizm Ochrony
Log Service chroni dostęp do logów za pomocą **prostego mechanizmu hasłem**. Wszystkie endpointy logów wymagają przekazania hasła w parametrze `password`.

### Konfiguracja
```yaml
log-service:
  access-password: someverylongstring
```

**Lokalizacja**: `log-service/src/main/resources/application.yml`

### Sposób Dostępu
Wszystkie zapytania do logów MUSZĄ zawierać parametr query `password`:

```
GET /logs?password=someverylongstring
GET /logs?password=someverylongstring&userId=1&limit=50
GET /logs?password=someverylongstring&eventType=REPORT_CREATED
```

### Odpowiedź na Błędne Hasło
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "message": "Unauthorized: Invalid password"
}
```

## Available Endpoints

### 1. Get All Audit Logs
```http
GET /logs?password={{log_access_password}}
```

**Query Parameters:**
- `password` (required) - Access password
- `limit` (optional, default: 100) - Maximum number of logs to return

**Example:**
```
GET http://localhost:8084/logs?password=someverylongstring&limit=50
```

### 2. Get Logs by User ID
```http
GET /logs?password={{log_access_password}}&userId=1&limit=50
```

**Query Parameters:**
- `password` (required) - Access password
- `userId` (optional) - Filter logs by user ID
- `limit` (optional, default: 100) - Maximum number of logs

**Example:**
```
GET http://localhost:8084/logs?password=someverylongstring&userId=1&limit=50
```

### 3. Get Logs by Event Type
```http
GET /logs?password={{log_access_password}}&eventType=REPORT_CREATED&limit=50
```

**Query Parameters:**
- `password` (required) - Access password
- `eventType` (optional) - Filter logs by event type
- `limit` (optional, default: 100) - Maximum number of logs

**Event Types:**
- `REPORT_CREATED` - When a new report is created
- `REPORT_UPDATED` - When a report is updated
- `REPORT_DELETED` - When a report is deleted
- `USER_REGISTERED` - When a user registers
- `USER_LOGGED_IN` - When a user logs in

**Example:**
```
GET http://localhost:8084/logs?password=someverylongstring&eventType=REPORT_CREATED&limit=50
```

### 4. Health Check
```http
GET /health
```

**Example:**
```
GET http://localhost:8084/health
```

Response:
```
Log Service is running
```

## Log Entity Structure

```json
{
  "id": 1,
  "userId": 1,
  "eventType": "REPORT_CREATED",
  "entityType": "REPORT",
  "entityId": 123,
  "action": "CREATED",
  "description": "Report created by user",
  "timestamp": "2025-01-13T10:30:00",
  "status": "SUCCESS"
}
```

## Security Best Practices

### Current Implementation
- ✅ Password-based access control
- ✅ Simple query parameter validation
- ✅ Logging of unauthorized attempts

### Recommendations for Production
1. **Use HTTPS Only** - Ensure all requests use HTTPS to prevent password exposure
2. **Change Default Password** - Replace `someverylongstring` with a strong, unique password
3. **Implement API Key Authentication** - Consider replacing password with API keys
4. **Implement JWT Authentication** - Align with other services' authentication
5. **Rate Limiting** - Add rate limiting to prevent brute force attacks
6. **Audit Logging** - Log all access attempts to the log service itself
7. **Database Encryption** - Encrypt sensitive data in the database

## Testing with Postman

### Configuration
Set the `log_access_password` variable in Postman:
```
Key: log_access_password
Value: someverylongstring
```

### Available Collections
- **Log Service (Gateway)** - Access through API Gateway at `http://localhost:8080/api/logs`
- **Log Service (Direct)** - Direct access at `http://localhost:8084/logs`

Both require the `password` query parameter for authentication.

## Gateway Integration

The API Gateway routes all `/api/logs/**` requests to the Log Service:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: log-service
          uri: http://log-service:8084
          predicates:
            - Path=/api/logs/**
          filters:
            - RewritePath=/api/logs(?<segment>/?.*), /logs${segment}
```

This means:
- Request: `GET http://localhost:8080/api/logs?password=xxx`
- Forwarded to: `GET http://log-service:8084/logs?password=xxx`

## RabbitMQ Integration

Log Service listens to events from Report Service via RabbitMQ:

```yaml
rabbitmq:
  exchange:
    reports: cityfix.reports
  queue:
    report-created: report.created.queue
  routing-key:
    report-created: report.created
```

Events are automatically captured and stored in the database whenever reports are created, updated, or deleted.

