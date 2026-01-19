# Uruchomienie User Service

## Wymagania

- **Java:** 17 lub nowsze
- **Gradle:** 8.6 (wrapper jest w projekcie)
- **PostgreSQL:** 15 (dla produkcji)
- **Docker:** (opcjonalnie, do uruchomienia w kontenerze)

## Uruchomienie Lokalnie

### 1. Przygotowanie

```bash
cd user-service
```

### 2. Czyszczenie i Budowanie

```bash
./gradlew clean build -x test
```

Na Windows:
```bash
./gradlew.bat clean build -x test
```

### 3. Uruchomienie

#### Opcja 1: Spring Boot Run (z domyślnym profilem)

```bash
./gradlew bootRun
```

#### Opcja 2: Spring Boot Run (z lokalnym profilem H2)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### Opcja 3: Uruchomienie JAR bezpośrednio

```bash
java -jar build/libs/user-service-1.0-SNAPSHOT.jar
```

#### Opcja 4: Z parametrami środowiskowymi

```bash
java -jar build/libs/user-service-1.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/cityfix_users \
  --spring.datasource.username=cityfix_user \
  --spring.datasource.password=cityfix_password \
  --jwt.secret=your-secure-key
```

## Uruchomienie w Docker

### 1. Budowanie Obrazu

Z katalogu root projektu:

```bash
docker build -f user-service/Dockerfile -t cityfix/user-service:latest ./user-service
```

### 2. Uruchomienie Kontenera

```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cityfix_users \
  -e SPRING_DATASOURCE_USERNAME=cityfix_user \
  -e SPRING_DATASOURCE_PASSWORD=cityfix_password \
  -e SPRING_PROFILES_ACTIVE=docker \
  cityfix/user-service:latest
```

### 3. Uruchomienie z docker-compose

Z katalogu root projektu:

```bash
docker-compose up user-service
```

Aby uruchomić wszystkie serwisy:

```bash
docker-compose up
```

## Konfiguracja

### Profil: local (H2 - In-Memory Database)

Domyślny profil dla lokalnego rozwoju.

**Połączenie:**
- URL: `jdbc:h2:mem:cityfix`
- Użytkownik: `sa`
- Baza danych: In-memory

**Uruchomienie:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Profil: docker (PostgreSQL)

Profil dla Docker/produkcji.

**Połączenie:**
- URL: `jdbc:postgresql://postgres:5432/cityfix_users`
- Użytkownik: `cityfix_user`
- Hasło: `cityfix_password`

**Uruchomienie:**
```bash
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### Profil: test (H2 - Testing)

Profil dla testów.

**Uruchomienie testów:**
```bash
./gradlew test
```

## Zmienne Środowiskowe

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cityfix_users
SPRING_DATASOURCE_USERNAME=cityfix_user
SPRING_DATASOURCE_PASSWORD=cityfix_password

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Spring Profile
SPRING_PROFILES_ACTIVE=docker

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_ORG_EXAMPLE_USERSERVICE=DEBUG
```

## Sprawdzenie Statusu

### Health Check Endpoint

```bash
curl http://localhost:8081/api/users/health
```

Odpowiedź:
```
User Service is running
```

### Actuator Endpoints

```bash
# Health information
curl http://localhost:8081/actuator/health

# Info about application
curl http://localhost:8081/actuator/info

# List all endpoints
curl http://localhost:8081/actuator
```

## Testowanie API

### Przy użyciu cURL

```bash
# Rejestracja
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"TestPass123"}'

# Logowanie
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"TestPass123"}'
```

### Przy użyciu PowerShell

```powershell
# Rejestracja
$body = @{
    username = "test"
    email = "test@example.com"
    password = "TestPass123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Przy użyciu Postman

Importuj `postman-collection.json` do Postman i uruchom żądania.

## Uruchamianie Testów

### Wszystkie testy

```bash
./gradlew test
```

### Konkretne testy

```bash
./gradlew test --tests UserServiceTest
./gradlew test --tests UserControllerIntegrationTest
```

### Testy z pokryciem kodu

```bash
./gradlew test jacocoTestReport
```

## Logi

### Wyświetlanie Logów

```bash
# Podczas uruchamiania bootRun
./gradlew bootRun

# Logów Docker-a
docker logs cityfix-user-service

# Z docker-compose
docker-compose logs user-service
```

### Konfiguracja Poziomów Logów

W `application.yml`:

```yaml
logging:
  level:
    root: INFO
    org.example.userservice: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

## Troubleshooting

### Port 8081 jest zajęty

```bash
# Linux/Mac: Znaleźć proces
lsof -i :8081

# Windows: PowerShell
Get-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess

# Zmienić port w application.yml
server:
  port: 8085
```

### Błąd Połączenia z Bazą Danych

```bash
# Sprawdzić czy PostgreSQL jest uruchomiony
docker ps | grep postgres

# Sprawdzić łączność
psql -h localhost -U cityfix_user -d cityfix
```

### Błędy Kompilacji

```bash
# Czyszczenie cache'a
./gradlew clean

# Aktualizacja zależności
./gradlew --refresh-dependencies

# Pełna przebudowa
./gradlew clean build
```

## Wdrażanie do Produkcji

### Zmienne Produkcyjne

```bash
# Zmień secret key!
export JWT_SECRET=$(openssl rand -base64 32)
export JWT_EXPIRATION=86400000

# Baza danych
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.example.com:5432/cityfix_users
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password

# Profil
export SPRING_PROFILES_ACTIVE=docker
```

### Build dla Produkcji

```bash
./gradlew clean build -Pproduction
```

### Docker Push

```bash
docker tag cityfix/user-service:latest registry.example.com/cityfix/user-service:1.0.0
docker push registry.example.com/cityfix/user-service:1.0.0
```

## Monitoring

### Metryki

Serwis udostępnia metryki Prometheus:

```bash
curl http://localhost:8081/actuator/metrics
```

### Śledzi Zmiany

Wyświetl logi zmian aplikacji:

```bash
docker logs -f cityfix-user-service
```

## Support

W przypadku problemów, sprawdź:
1. Logi aplikacji
2. Dokumentację API
3. Dokumentację JWT
4. Dokumentację Bazy Danych

