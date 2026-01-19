# User Service - Quick Reference

## 🚀 Start Szybko

### Uruchomienie
```bash
# Lokalnie (H2)
cd user-service
./gradlew bootRun

# Docker
docker-compose up user-service
```

### Health Check
```bash
curl http://localhost:8081/api/users/health
```

---

## 📚 Dokumentacja - Co Przeczytać?

| Potrzeba | Plik |
|----------|------|
| Szybki start | INDEX.md |
| Jak działa JWT? | JWT_SIMPLE_EXPLANATION.md |
| API Reference | API_DOCUMENTATION.md |
| Pełny JWT Guide | JWT_COOKIES_GUIDE.md |
| Sesje Detale | SESSIONS_AUTHENTICATION.md |
| Migracja Kodu | MIGRATION_GUIDE.md |
| Baza Danych | DATABASE_SCHEMA.md (user-service/) |
| Uruchomienie | RUNNING.md (user-service/) |
| Contributing | CONTRIBUTING.md (user-service/) |

---

## 🔐 JWT w Cookies - TL;DR

```
Login:     JWT ustawiana w cookie (nie w body!)
Żądania:   Cookie wysyłana automatycznie
Logout:    Cookie usunięta (Max-Age=0)
Frontend:  credentials: 'include' w fetch()
```

**Flagi:**
```
HttpOnly   → JS nie może odczytać
Secure     → Tylko HTTPS
SameSite   → Ochrona CSRF
```

---

## 📡 API Endpoints

### Register
```bash
POST /api/users/register
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}

Response: 201 Created
{
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login
```bash
POST /api/users/login
Content-Type: application/json
credentials: include

{
  "username": "john",
  "password": "SecurePass123"
}

Response: 200 OK
Set-Cookie: JWT_TOKEN=...; HttpOnly; Secure; SameSite=Strict; Max-Age=86400
{
  "id": 1,
  "username": "john",
  "email": "john@example.com"
}
```

### Logout
```bash
POST /api/users/logout
credentials: include

Response: 200 OK
Set-Cookie: JWT_TOKEN=; Max-Age=0
{
  "message": "Logged out successfully"
}
```

### Get User
```bash
GET /api/users/{id}

Response: 200 OK
{
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Update User
```bash
PUT /api/users/{id}
Content-Type: application/json
credentials: include

{
  "firstName": "Jonathan",
  "email": "jonathan@example.com"
}

Response: 200 OK
{
  "id": 1,
  "username": "john",
  "email": "jonathan@example.com",
  "firstName": "Jonathan"
}
```

---

## 💻 Frontend Kod

### JavaScript/Fetch

```javascript
// 1. Rejestracja
fetch('/api/users/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username, email, password, firstName, lastName
  }),
  credentials: 'include'  // WAŻNE!
});

// 2. Logowanie
fetch('/api/users/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password }),
  credentials: 'include'  // Cookie ustawiana!
});

// 3. Żądanie Autentykowane
fetch('/api/users/1', {
  credentials: 'include'  // Cookie wysyłana!
});

// 4. Wylogowanie
fetch('/api/users/logout', {
  method: 'POST',
  credentials: 'include'  // Cookie usunięta!
});
```

### Axios

```javascript
// Ustawienie raz:
axios.defaults.withCredentials = true;

// Potem normalne żądania:
await axios.post('/api/users/login', {username, password});
await axios.get('/api/users/1');
```

---

## 🧪 Testowanie

### cURL
```bash
# Login z zapisem cookies
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"john","password":"SecurePass123"}'

# Żądanie z cookies
curl http://localhost:8081/api/users/1 -b cookies.txt

# Logout
curl -X POST http://localhost:8081/api/users/logout -b cookies.txt
```

### Postman
1. Import `postman-collection.json`
2. Cookies są ustawiane automatycznie!
3. Przejrzyj cookies w zakładce "Cookies"

---

## 🔧 Konfiguracja

### application.yml
```yaml
jwt:
  secret: your-secret-key-256-bits
  expiration: 86400000  # 24h
  cookie:
    name: JWT_TOKEN
```

### Docker
```bash
docker-compose up user-service
```

### Zmienne Środowiskowe
```bash
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=docker
```

---

## 🔐 Bezpieczeństwo - Checklist

- [x] JWT w cookies (nie body)
- [x] HttpOnly flag
- [x] Secure flag (produkcja)
- [x] SameSite=Strict
- [x] BCrypt haszowanie
- [x] HS512 signing
- [x] 24h expiration
- [ ] HTTPS (produkcja)
- [ ] Zmieniony JWT secret (produkcja)

---

## ❓ FAQ

**P: Czy mogę użyć Authorization header zamiast cookies?**
A: Tak, Jest fallback. Ale cookies są bezpieczniejsze.

**P: Jak zmienić JWT secret?**
A: Edytuj `jwt.secret` w `application.yml` lub `.env`

**P: Jak wydłużyć lub skrócić ważność tokenu?**
A: Zmień `jwt.expiration` (w millisekund)

**P: Czy token może być odwołany?**
A: Nie (stateless). Działa do expiration.

**P: Co jeśli ktoś ukradzienymi tokenem?**
A: Ograniczony do 24h. Zawsze używaj HTTPS!

**P: Czy mogę testować bez HTTPS?**
A: Tak lokalnie. `Secure` flag jest ignorowany dla localhost.

---

## 🐛 Troubleshooting

### 401 Unauthorized
```
Przyczyna: Cookie nie wysyłana lub wygasła
Rozwiązanie: Dodaj credentials: 'include'
             Lub zaloguj się ponownie
```

### Cookie Nie Jest Wysyłana
```
Przyczyna: Brak credentials: 'include'
Rozwiązanie: fetch(url, {credentials: 'include'})
```

### CORS Error
```
Przyczyna: Frontend i Backend na innych portach
Rozwiązanie: Server musi mieć Allow-Credentials header
             Frontend musi mieć credentials: 'include'
```

---

## 📊 Diagram Autentykacji

```
┌─────────────────────────────────────────────────────────┐
│ 1. REJESTRACJA                                          │
├─────────────────────────────────────────────────────────┤
│ POST /users/register → Server → 201 Created            │
│ Brak sesji - user musi się zalogować                   │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. LOGOWANIE                                            │
├─────────────────────────────────────────────────────────┤
│ POST /login → Server generates JWT → Sets Cookie       │
│ Set-Cookie: JWT_TOKEN=...; HttpOnly; Secure;           │
│ Response: {user data} (bez tokenu)                      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. ŻĄDANIA AUTENTYKOWANE                                │
├─────────────────────────────────────────────────────────┤
│ GET /users/1 + credentials: 'include'                  │
│ Browser automatycznie wysyła Cookie                     │
│ Server validates JWT from cookie                        │
│ Response: {data}                                         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. WYLOGOWANIE                                          │
├─────────────────────────────────────────────────────────┤
│ POST /logout → Server removes cookie                    │
│ Set-Cookie: JWT_TOKEN=; Max-Age=0                      │
│ Browser usuwa cookie - Sesja skończona!                │
└─────────────────────────────────────────────────────────┘
```

---

## 📚 Dodatkowe Materiały

### Wewnętrzne
- `/docs/user-service/INDEX.md` - Główny indeks
- `/docs/user-service/JWT_SIMPLE_EXPLANATION.md` - Proste wyjaśnienie
- `/docs/user-service/API_DOCUMENTATION.md` - Wszystkie endpoints

### Zewnętrzne
- https://jwt.io/ - JWT debugger
- https://spring.io/projects/spring-boot
- https://owasp.org/

---

## ✅ Checklist Wdrażania

- [ ] Przeczytaj JWT_SIMPLE_EXPLANATION.md
- [ ] Przeczytaj API_DOCUMENTATION.md
- [ ] Dodaj `credentials: 'include'` do fetch()
- [ ] Uruchom `./gradlew test`
- [ ] Przetestuj API (cURL/Postman)
- [ ] Deploy do staging
- [ ] Zmień JWT secret (produkcja)
- [ ] Włącz HTTPS (produkcja)
- [ ] Ustaw Secure flag (produkcja)
- [ ] Deploy do produkcji

---

**Wersja:** 2.0 (JWT w Cookies)
**Data:** 2024-01-13
**Status:** ✅ Gotowy do produkcji

