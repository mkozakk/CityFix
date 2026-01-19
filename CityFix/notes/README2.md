# CityFix Dokumentacja - Indeks Główny

## 📚 Struktura Dokumentacji

```
docs/
├── user-service/
│   ├── INDEX.md                      # START HERE!
│   ├── README.md
│   ├── API_DOCUMENTATION.md
│   ├── JWT_COOKIES_GUIDE.md
│   ├── SESSIONS_AUTHENTICATION.md
│   ├── RUNNING.md
│   ├── DATABASE_SCHEMA.md
│   └── CONTRIBUTING.md
├── gateway/
│   └── (coming soon)
├── location-service/
│   └── (coming soon)
├── report-service/
│   └── (coming soon)
└── README.md (this file)
```

## 🎯 Gdzie Zacząć?

### Jestem Nowy w Projekcie
1. Przeczytaj: **CityFix/README.md**
2. Przeczytaj: **docs/user-service/README.md**
3. Przeczytaj: **docs/user-service/RUNNING.md**

### Pracuję nad User Service
1. Przeczytaj: **docs/user-service/INDEX.md**
2. Wybierz dokumentację zależnie od roli (patrz poniżej)

### Pracuję nad Innym Serwisem
- Dokumentacja niedostępna (coming soon)
- Skontaktuj się z zespołem DevOps

## 👥 Dokumentacja po Rolach

### Backend Developer

#### User Service
1. **RUNNING.md** - Jak uruchomić serwis lokalnie
2. **CONTRIBUTING.md** - Standardy kodowania
3. **DATABASE_SCHEMA.md** - Struktura bazy danych
4. **API_DOCUMENTATION.md** - Dokumentacja API
5. **JWT_COOKIES_GUIDE.md** - Jak działa JWT

#### Inne Serwisy
- (coming soon)

### Frontend Developer

#### User Service API
1. **API_DOCUMENTATION.md** - Wszystkie endpoints
2. **JWT_COOKIES_GUIDE.md** - Client-side sekcja
3. **RUNNING.md** - Jak przetestować API
4. Pliki testowe: `test-api.sh`, `test-api.ps1`, `postman-collection.json`

#### Zalecane Narzędzia
- Postman (import `postman-collection.json`)
- VS Code REST Client
- Thunder Client
- Insomnia

### DevOps / SRE

#### User Service Deployment
1. **RUNNING.md** - Docker & Deployment
2. **DATABASE_SCHEMA.md** - Baza danych
3. **JWT_COOKIES_GUIDE.md** - Bezpieczeństwo
4. **SESSIONS_AUTHENTICATION.md** - Monitoring sesji

#### Produkcja
- Ustaw **secure** zmienne środowiskowe
- Włącz **HTTPS**
- Zabezpiecz **JWT secret key**

### Security Engineer

#### User Service Security
1. **JWT_COOKIES_GUIDE.md** - Security model
2. **SESSIONS_AUTHENTICATION.md** - Bezpieczeństwo sesji
3. **API_DOCUMENTATION.md** - Error handling
4. **CONTRIBUTING.md** - Code standards

#### Threat Model
- ✅ XSS Protection (HttpOnly cookies)
- ✅ CSRF Protection (SameSite=Strict)
- ✅ Password Security (BCrypt)
- ✅ Token Signing (HS512)
- ⚠️ Session Revocation (nie działa dla JWT)

## 🔐 Security Overview

### JWT w Cookies (Wdrażane)
```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       │ 1. POST /login
       ├────────────────>
       │
       │ 2. Set-Cookie: JWT_TOKEN=... (HttpOnly)
       │<────────────────
       │
       │ 3. GET /users/1
       │    (Cookie wysyłana automatycznie)
       ├────────────────>
       │
       │ 4. Serwer waliduje
       │<────────────────
```

### Flagi Bezpieczeństwa
- ✅ HttpOnly - Ochrona przed XSS
- ✅ Secure - Tylko HTTPS (produkcja)
- ✅ SameSite=Strict - Ochrona przed CSRF
- ✅ Max-Age=86400 - Expiration za 24h

## 📋 User Service API

| Metoda | Endpoint | Status |
|--------|----------|--------|
| POST | `/users/register` | ✅ Gotowy |
| POST | `/users/login` | ✅ Gotowy |
| POST | `/users/logout` | ✅ Gotowy |
| GET | `/users/{id}` | ✅ Gotowy |
| PUT | `/users/{id}` | ✅ Gotowy |
| GET | `/users/health` | ✅ Gotowy |

## 🚀 Quick Start

### Uruchomienie Lokalnie
```bash
# Terminal 1: Database
docker-compose up postgres

# Terminal 2: User Service
cd user-service
./gradlew bootRun
```

### Testowanie API
```bash
# Rejestracja
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{...}'

# Logowanie
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{...}'

# Żądanie Autentykowane
curl http://localhost:8081/api/users/1 -b cookies.txt
```

## 📊 Statystyki Projektu

### User Service
- **Lines of Code:** ~2000
- **Test Coverage:** 70%+
- **Documentation:** 8 plików (.md)
- **Classes:** 15+
- **Endpoints:** 6

### Baza Danych
- **Tables:** 1 (users)
- **Migrations:** 1 (Flyway)
- **Indexes:** 2

## 🔄 Development Workflow

### 1. Tworzenie Feature'u
```bash
git checkout -b feature/add-email-verification
# Edytuj kod
./gradlew test
git commit -m "feat(user): add email verification"
git push origin feature/add-email-verification
```

### 2. Code Review
- Minimum 1 reviewer
- Testy muszą przechodzić
- Dokumentacja zaktualizowana

### 3. Merge & Deploy
```bash
git merge develop
git push origin develop
# CI/CD wdrażają do staging
# Po testowaniu merge do main
```

## 📚 Dokumentacja Serwisów

### ✅ User Service
- Dokumentacja: **docs/user-service/**
- Status: Gotowy do produkcji
- Wersja: 1.0-SNAPSHOT

### ⏳ Gateway Service
- Dokumentacja: (coming soon)
- Status: W trakcie
- Wersja: Planowana

### ⏳ Location Service
- Dokumentacja: (coming soon)
- Status: W trakcie
- Wersja: Planowana

### ⏳ Report Service
- Dokumentacja: (coming soon)
- Status: W trakcie
- Wersja: Planowana

## 🛠️ Tech Stack

### User Service
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.0
- **Database:** PostgreSQL 15
- **Authentication:** JWT + BCrypt
- **Testing:** JUnit 5 + Mockito
- **Build:** Gradle 8.6
- **Container:** Docker

## 📈 Roadmap

### Q1 2024
- ✅ User Service MVP
- [ ] Gateway Service
- [ ] Location Service
- [ ] Report Service

### Q2 2024
- [ ] Authentication Service Integration
- [ ] Rate Limiting
- [ ] Email Verification
- [ ] Password Reset

### Q3 2024
- [ ] Two-Factor Authentication
- [ ] API Versioning
- [ ] Monitoring/Logging
- [ ] Performance Optimization

## 📞 Support & Communication

### Kanały
- **Issues:** GitHub Issues
- **Docs:** docs/ folder
- **Code:** GitHub Repository
- **Team:** Slack #cityfix-dev

### FAQ

**Q: Gdzie są bazy danych?**
A: PostgreSQL 15 w Docker. Sprawdź `docker-compose.yml`

**Q: Jak zmienić JWT secret?**
A: Edytuj `.env` lub zmienne środowiskowe. Patrz `JWT_COOKIES_GUIDE.md`

**Q: Jak dodać nowy endpoint?**
A: Patrz `CONTRIBUTING.md` - sekcja "Adding Features"

**Q: Jak uruchomić testy?**
A: `./gradlew test` - patrz `RUNNING.md`

## 📝 Konwencje

### Naming
- Classes: `PascalCase` (UserController)
- Methods: `camelCase` (getUser)
- Constants: `UPPER_SNAKE_CASE` (MAX_ATTEMPTS)
- Packages: `lowercase.separated` (org.example.userservice)

### Commits
- Format: `type(scope): subject`
- Przykłady:
  - `feat(auth): add email verification`
  - `fix(user): handle null pointer exception`
  - `docs(api): update endpoints documentation`

### Branches
- Feature: `feature/description`
- Fix: `fix/description`
- Docs: `docs/description`
- Release: `release/v1.0.0`

## 🎓 Learning Resources

### Tutoriale
- Spring Boot: https://spring.io/guides
- JWT: https://jwt.io/
- PostgreSQL: https://www.postgresql.org/docs/
- Docker: https://docs.docker.com/

### Best Practices
- OWASP Security: https://owasp.org/
- Code Quality: https://www.sonarqube.org/
- REST API Design: https://restfulapi.net/

## ✅ Checklist dla Nowych Developerów

- [ ] Przeczytaj CityFix/README.md
- [ ] Przeczytaj docs/user-service/README.md
- [ ] Setup environment (JDK 17, Docker)
- [ ] Clone repository
- [ ] Uruchom `./gradlew bootRun`
- [ ] Przetestuj `/api/users/health`
- [ ] Przeczytaj CONTRIBUTING.md
- [ ] Skonfiguruj IDE
- [ ] Dodaj do Slack'a
- [ ] Poproś kod review na PR

## 📄 Licencja

MIT License - patrz LICENSE file

## 📞 Kontakt

**Team Lead:** [Name]
**DevOps:** [Name]
**Security:** [Name]

---

**Last Updated:** 2024-01-13
**Next Review:** 2024-02-13

