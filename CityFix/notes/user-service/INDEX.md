# Dokumentacja User Service - Indeks

## 📚 Spis Dokumentów

### 1. **README.md** - Przegląd Projektu
- Szybki start
- Główne funkcjonalności
- Wymagania
- Bezpieczeństwo JWT w cookies

### 2. **API_DOCUMENTATION.md** - Dokumentacja API
- Szczegółowy opis wszystkich endpointów
- Parametry i response'y
- Kody błędów
- Przykłady (JavaScript, cURL)
- CORS configuration

### 3. **JWT_COOKIES_GUIDE.md** - Przewodnik JWT w Cookies
- Jak działa JWT
- Cookies vs LocalStorage
- Implementacja server-side
- Implementacja client-side
- Workflow autentykacji
- Diagram przepływów

### 4. **SESSIONS_AUTHENTICATION.md** - Sesje i Autentykacja
- Typy sesji (Stateful vs Stateless)
- Struktura JWT
- Bezpieczeństwo sesji
- Lifecycle sesji
- Monitoring
- Troubleshooting

### 5. **RUNNING.md** - Uruchomienie Serwisu
- Wymagania
- Uruchomienie lokalnie
- Uruchomienie w Docker
- Konfiguracja profili (local, docker, test)
- Zmienne środowiskowe
- Testowanie API
- Troubleshooting

### 6. **DATABASE_SCHEMA.md** - Schemat Bazy Danych
- Struktura tabeli `users`
- Kolumny i ograniczenia
- Indeksy
- Migracje Flyway
- Backup/Restore
- Monitoring

### 7. **CONTRIBUTING.md** - Przewodnik dla Developerów
- Kod postępowania
- Setup środowiska
- Standardy kodowania
- Testy
- Commit messages
- Pull requests
- Database migrations

## 🎯 Szybki Start

### Dla Developerów
1. Przeczytaj: **README.md**
2. Przeczytaj: **RUNNING.md**
3. Przeczytaj: **CONTRIBUTING.md**
4. Przeczytaj: **JWT_COOKIES_GUIDE.md**

### Dla Frontend Developerów
1. Przeczytaj: **API_DOCUMENTATION.md**
2. Przeczytaj: **JWT_COOKIES_GUIDE.md** (Client-Side sekcja)
3. Testuj: Użyj przykładów JavaScript/Fetch API

### Dla DevOps
1. Przeczytaj: **RUNNING.md**
2. Przeczytaj: **DATABASE_SCHEMA.md**
3. Ustaw: Docker/Kubernetes

### Dla Security Engineers
1. Przeczytaj: **JWT_COOKIES_GUIDE.md**
2. Przeczytaj: **SESSIONS_AUTHENTICATION.md**
3. Sprawdź: Security checklist

## 📋 Zawartość Katalogów

```
docs/user-service/
├── README.md                      # Przegląd (START HERE)
├── API_DOCUMENTATION.md           # Dokumentacja API
├── JWT_COOKIES_GUIDE.md          # Przewodnik JWT
├── SESSIONS_AUTHENTICATION.md    # Sesje i autentykacja
├── RUNNING.md                     # Uruchomienie
├── DATABASE_SCHEMA.md             # Baza danych
├── CONTRIBUTING.md                # Contributing
└── INDEX.md                       # Ten plik
```

## 🔐 Bezpieczeństwo

### JWT w Cookies (Wdrożone)
- ✅ HttpOnly flag (ochrona przed XSS)
- ✅ Secure flag (tylko HTTPS w produkcji)
- ✅ SameSite=Strict (ochrona przed CSRF)
- ✅ Max-Age ustawiony (24h)
- ✅ BCrypt haszowanie haseł

### Nie Mamy (Tradycyjne Sesje)
- ❌ Tabeli sesji (Stateless!)
- ❌ PHPSESSID cookies
- ❌ Session ID w bazie danych

## 🚀 Endpoints

| Metoda | Endpoint | Autentykacja | Plik |
|--------|----------|--------------|------|
| POST | `/users/register` | Nie | API_DOCUMENTATION.md |
| POST | `/users/login` | Nie | API_DOCUMENTATION.md |
| POST | `/users/logout` | Cookies | API_DOCUMENTATION.md |
| GET | `/users/{id}` | Nie* | API_DOCUMENTATION.md |
| PUT | `/users/{id}` | Cookies** | API_DOCUMENTATION.md |
| GET | `/users/health` | Nie | API_DOCUMENTATION.md |

*Bez autentykacji, ale inne serwisy mogą wymagać
**Wymaga JWT w cookies

## 🛠️ Konfiguracja

### JWT Properties
```yaml
jwt:
  secret: your-secret-key-256-bits          # W .env
  expiration: 86400000                      # 24h
  cookie:
    name: JWT_TOKEN                         # Cookie name
```

### Baza Danych (PostgreSQL)
```
Host: postgres
Port: 5432
Database: cityfix_users
User: cityfix_user
Password: cityfix_password
```

### Docker
```bash
docker-compose up user-service
```

## 📊 Architektura

```
┌─────────────────┐
│     Frontend    │
│ (React/Vue/Angular)
└────────┬────────┘
         │ credentials: 'include'
         │
         ▼
┌──────────────────────────────┐
│   User Service API           │
│   Spring Boot 3.2.0          │
├──────────────────────────────┤
│ Controllers (REST)           │
│ Services (Business Logic)    │
│ Security (JWT + BCrypt)      │
│ JPA (Database Access)        │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────┐
│   PostgreSQL 15  │
│   users table    │
└──────────────────┘
```

## 🧪 Testowanie

### Unit Tests
```bash
./gradlew test --tests UserServiceTest
```

### Integration Tests
```bash
./gradlew test --tests UserControllerIntegrationTest
```

### API Tests
```bash
# Użyj Postman (postman-collection.json)
# Lub cURL (test-api.sh)
# Lub PowerShell (test-api.ps1)
```

## 📈 Monitorowanie

### Health Check
```bash
curl http://localhost:8081/api/users/health
```

### Logs
```bash
./gradlew bootRun | grep "userservice"
# lub
docker logs cityfix-user-service
```

## 🔄 CI/CD

### Build
```bash
./gradlew clean build -x test
```

### Docker Image
```bash
docker build -f user-service/Dockerfile -t cityfix/user-service:latest .
```

### Push to Registry
```bash
docker push registry.example.com/cityfix/user-service:latest
```

## 📞 Support

### Problemy?
1. Sprawdź: **RUNNING.md** (Troubleshooting)
2. Sprawdź: **SESSIONS_AUTHENTICATION.md** (Troubleshooting)
3. Sprawdź: Logi aplikacji (`docker logs`)
4. Skontaktuj się z zespołem DevOps

### Pytania o API?
- Przeczytaj: **API_DOCUMENTATION.md**
- Sprawdź: **JWT_COOKIES_GUIDE.md**

### Pytania o Kodzie?
- Przeczytaj: **CONTRIBUTING.md**
- Sprawdź: Komentarze w kodzie
- Otwórz Issue

## 📝 Wersja

- **User Service Version:** 1.0-SNAPSHOT
- **API Version:** 2.0 (JWT w Cookies)
- **Spring Boot:** 3.2.0
- **Java:** 17
- **PostgreSQL:** 15

## 🎓 Dodatkowe Materiały

### JWT
- https://jwt.io/
- https://tools.ietf.org/html/rfc7519

### Spring Security
- https://spring.io/projects/spring-security
- https://spring.io/guides/gs/securing-web/

### OWASP
- https://owasp.org/www-community/attacks/csrf
- https://owasp.org/www-community/attacks/xss/

### HTTP Cookies
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies
- https://tools.ietf.org/html/rfc6265

## ✅ Checklist Implementacji

- [x] JWT w Cookies (HttpOnly)
- [x] BCrypt Haszowanie
- [x] API Endpoints
- [x] Walidacja Danych
- [x] Error Handling
- [x] Unit Tests
- [x] Integration Tests
- [x] Docker Support
- [x] Flyway Migrations
- [x] Dokumentacja API
- [x] Dokumentacja JWT
- [x] Dokumentacja Sesji
- [x] Dokumentacja Bazy Danych
- [ ] Rate Limiting (TODO)
- [ ] Two-Factor Auth (TODO)
- [ ] Email Verification (TODO)
- [ ] Password Reset (TODO)

---

**Data:** 2024-01-13
**Autor:** GitHub Copilot
**Licencja:** MIT

