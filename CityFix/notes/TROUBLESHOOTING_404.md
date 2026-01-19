# 🔍 TROUBLESHOOTING - HTTP 404 przy /api/users/register

## 🔧 Krok 1: Sprawdź czy kontenery są uruchomione

```bash
# Sprawdź status kontenerów
docker ps

# Powinno być:
# - cityfix-postgres (port 5432)
# - cityfix-user-service (port 8081)
# - cityfix-gateway (port 8080)
# - cityfix-report-service (port 8082)
# - cityfix-location-service (port 8083)
```

**Jeśli któreś kontenery nie działają:**
```bash
docker-compose logs <nazwa-kontenera>
# np: docker-compose logs cityfix-user-service
```

---

## 🔧 Krok 2: Sprawdź Health Check

```bash
# Health check User Service (bezpośrednio)
curl http://localhost:8081/api/users/health

# Powinno zwrócić:
# User Service is running

# Health check Gateway
curl http://localhost:8080/api/users/health

# Powinno zwrócić:
# User Service is running
```

---

## 🔧 Krok 3: Sprawdź routing w Gateway

```bash
# Test bezpośredni do User Service (port 8081)
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "email": "test@example.com",
    "password": "TestPass123"
  }'

# Jeśli działa → problem w Gateway
# Jeśli nie działa → problem w User Service
```

---

## 🔧 Krok 4: Sprawdź logi Gateway

```bash
# Logi Gateway
docker logs cityfix-gateway

# Szukaj błędów:
# - "Route not found"
# - "Cannot find route"
# - Connection errors
```

---

## 🔧 Krok 5: Sprawdź logi User Service

```bash
# Logi User Service
docker logs cityfix-user-service

# Szukaj:
# - "User Service started"
# - "Mapped {POST /users/register}"
# - Database connection errors
```

---

## 🔧 Krok 6: Sprawdź aplikacje.yml

```yaml
# user-service/src/main/resources/application.yml
server:
  port: 8081
  servlet:
    context-path: /api

# ✅ Powinno być:
# - port: 8081
# - context-path: /api
```

```yaml
# gateway/src/main/resources/application.yml
routes:
  - id: user-service
    uri: http://user-service:8081
    predicates:
      - Path=/api/users/**
    filters:
      - RewritePath=/api/users(?<segment>/?.*), /users${segment}

# ✅ Powinno być:
# - uri: http://user-service:8081 (NO LOCALHOST!)
# - Path=/api/users/**
# - RewritePath: /api/users → /users
```

---

## 🔧 Krok 7: Sprawdź bazy danych

```bash
# Połącz do bazy
docker exec -it cityfix-postgres psql -U cityfix_user -d cityfix

# Sprawdź tabele
\dt

# Powinno pokazać:
# - users
# - reports
# - locations

# Wyjdź
\q
```

---

## 🐛 Typowe problemy i rozwiązania

### Problem: "Cannot find route"
```
Przyczyna: Gateway nie rozpoznaje ścieżki /api/users
Rozwiązanie:
  1. Sprawdź gateway/application.yml - Line 10: Path=/api/users/**
  2. Sprawdzić czy gateway jest uruchomiony: docker ps
  3. Sprawdzić logi gateway: docker logs cityfix-gateway
```

### Problem: "Connection refused" (8081)
```
Przyczyna: User Service nie jest uruchomiony
Rozwiązanie:
  1. docker ps - sprawdź czy user-service jest UP
  2. docker logs cityfix-user-service - sprawdź błędy
  3. docker-compose down && docker-compose up - restart
```

### Problem: "Path does not match route"
```
Przyczyna: RewritePath jest źle skonfigurowany
Rozwiązanie:
  Sprawdzić gateway/application.yml:
  - RewritePath=/api/users(?<segment>/?.*), /users${segment}
  
  Powinno rewrite'ować:
  /api/users/register → /users/register
```

### Problem: "No route to host"
```
Przyczyna: Gateway nie może dostać się do user-service
Rozwiązanie:
  1. Sprawdzić docker-compose.yml - czy jest networks: cityfix-network
  2. Sprawdzić czy user-service jest w tej samej sieci
  3. Sprawdzić czy hostname "user-service" jest dostępny
```

---

## 📋 Szybki Test Request

```bash
# Test 1: Health check gateway
curl -v http://localhost:8080/api/users/health

# Test 2: Register nowy user
curl -v -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123"
  }'

# Flaga -v pokazuje:
# - Status code (powinna być 201 Created)
# - Headers (powinna być Set-Cookie z JWT_TOKEN)
# - Response body
```

---

## 🔄 Jeśli nic nie pomaga - Pełny reset

```bash
# 1. Zatrzymaj wszystko
docker-compose down

# 2. Wyczyść volumes
docker system prune -a --volumes

# 3. Przebuduj bez cache
docker-compose build --no-cache

# 4. Uruchom
docker-compose up

# 5. Czekaj aż wszystkie kontenery będą UP
# Powinno być: (healthy)
```

---

## ✅ Checklist

- [ ] Wszystkie kontenery są UP (docker ps)
- [ ] Health check działa (curl localhost:8081/api/users/health)
- [ ] Gateway routuje do user-service
- [ ] application.yml ma:
  - [ ] port: 8081
  - [ ] context-path: /api
- [ ] gateway/application.yml ma:
  - [ ] uri: http://user-service:8081
  - [ ] Path=/api/users/**
  - [ ] RewritePath poprawnie
- [ ] Baza danych jest initialized (tabele istnieją)

---

**Zgłoś które testy zwróciły błędy - potem wiemy gdzie szukać problemu!**

