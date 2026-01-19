# ✅ SECURITY AUDIT - User Service Endpoints

## 📊 Analiza Bezpieczeństwa

### ❌ PROBLEM ZNALEZIONY (Teraz naprawiony)

**GET /users/{id}** - Był niezabezpieczony!
```
Przed:
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
}

Problem: Każdy mógł pobrać dane dowolnego użytkownika!
Atak: GET /users/1 → Dane usera 1
     GET /users/2 → Dane usera 2
     GET /users/3 → Dane usera 3
```

**PUT /users/{id}** - Miał autentykację ale NIE sprawdzał ownership!
```
Przed:
@PutMapping("/{id}")
public ResponseEntity<UserResponse> updateUser(...) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return UNAUTHORIZED;
    }
    return userService.updateUser(id, request);
}

Problem: User zalogowany mógł edytować DOWOLNEGO użytkownika!
Atak: User "alice" zalogowana mogła:
     PUT /users/1 → edytować użytkownika "bob"
     PUT /users/3 → edytować użytkownika "charlie"
```

---

## ✅ NAPRAWA - Security Controls

### GET /users/{id} - TERAZ BEZPIECZNE

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    // 1. Sprawdzenie autentykacji
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return UNAUTHORIZED;  // 401
    }
    
    // 2. Sprawdzenie ownership
    String authenticatedUsername = authentication.getName();
    UserResponse userToRetrieve = userService.getUserById(id);
    
    // 3. Porównanie użytkownika
    if (!userToRetrieve.getUsername().equals(authenticatedUsername)) {
        return FORBIDDEN;  // 403
    }
    
    return ResponseEntity.ok(userToRetrieve);
}
```

**Teraz:**
- ✅ Wymaga JWT authentication
- ✅ Sprawdza czy user pobiera własne dane
- ✅ Zwraca 401 jeśli nie zalogowany
- ✅ Zwraca 403 jeśli próbuje dostępu do cudzych danych

---

### PUT /users/{id} - TERAZ BEZPIECZNE

```java
@PutMapping("/{id}")
public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, ...) {
    // 1. Sprawdzenie autentykacji
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return UNAUTHORIZED;  // 401
    }
    
    // 2. Sprawdzenie ownership
    String authenticatedUsername = authentication.getName();
    UserResponse currentUser = userService.getUserById(id);
    
    // 3. Porównanie użytkownika
    if (!currentUser.getUsername().equals(authenticatedUsername)) {
        return FORBIDDEN;  // 403
    }
    
    // 4. Aktualizacja (BEZPIECZNA)
    return ResponseEntity.ok(userService.updateUser(id, request));
}
```

**Teraz:**
- ✅ Wymaga JWT authentication
- ✅ Sprawdza czy user edytuje własne dane
- ✅ Zwraca 401 jeśli nie zalogowany
- ✅ Zwraca 403 jeśli próbuje edytować cudze dane

---

## 📋 Security Matrix - Wszystkie Endpoints

| Endpoint | Metoda | Public | Autentykacja | Ownership | Status |
|----------|--------|--------|--------------|-----------|--------|
| /users/register | POST | ✅ | ❌ | ❌ | ✅ SAFE |
| /users/login | POST | ✅ | ❌ | ❌ | ✅ SAFE |
| /users/logout | POST | ❌ | ✅ | ❌ | ✅ SAFE |
| /users/{id} | GET | ❌ | ✅ | ✅ | ✅ SAFE |
| /users/{id} | PUT | ❌ | ✅ | ✅ | ✅ SAFE |
| /users/health | GET | ✅ | ❌ | ❌ | ✅ SAFE |

---

## 🔐 Security Features

### 1. Authentication (JWT Cookies)
```
✅ JWT token w HttpOnly cookies
✅ Secure flag (HTTPS w produkcji)
✅ SameSite=Strict (CSRF protection)
✅ Token expiration: 24h
```

### 2. Authorization (Ownership Check)
```
✅ GET /users/{id} - sprawdzenie czy to swoje dane
✅ PUT /users/{id} - sprawdzenie czy to swoje dane
✅ Zwracanie 403 Forbidden jeśli brak ownership
```

### 3. Spring Security Config
```yaml
permitAll:
  - /users/register    (rejestracja)
  - /users/login       (logowanie)
  - /users/health      (health check)
  - /actuator/**       (monitoring)

authenticated:
  - /users/logout      (wylogowanie - wymaga JWT)
  - /users/{id}        (GET/PUT - wymaga JWT + ownership)
```

---

## 🧪 Test Cases - Bezpieczeństwo

### Test 1: Pobieranie danych bez autentykacji
```bash
GET /users/1

Response: 401 Unauthorized ✅
```

### Test 2: Pobieranie swoich danych
```bash
POST /users/login
# Zwraca: JWT w cookies

GET /users/1 (ze swoim JWT w cookies)

Response: 200 OK + swoje dane ✅
```

### Test 3: Próba pobierania danych innego użytkownika
```bash
# User "alice" zalogowana

GET /users/2 (user "bob")

Response: 403 Forbidden ✅
```

### Test 4: Edycja bez autentykacji
```bash
PUT /users/1
{ "email": "hacker@example.com" }

Response: 401 Unauthorized ✅
```

### Test 5: Edycja swoich danych
```bash
# User "alice" zalogowana

PUT /users/1
{ "email": "alice.newemail@example.com" }

Response: 200 OK + updated data ✅
```

### Test 6: Próba edycji danych innego użytkownika
```bash
# User "alice" zalogowana

PUT /users/2
{ "email": "hacker@example.com" }

Response: 403 Forbidden ✅
```

---

## 🔒 Summary

| Element | Przed | Po |
|---------|-------|-------|
| GET /users/{id} | ❌ PUBLIC - każdy może pobrać | ✅ PRIVATE - tylko swoje dane |
| PUT /users/{id} | ⚠️ HALF - auth bez ownership | ✅ FULL - auth + ownership |
| Atak danych | 🔓 MOŻLIWY | 🔒 NIEMOŻLIWY |
| Atak edycji | 🔓 MOŻLIWY | 🔒 NIEMOŻLIWY |

---

**Status:** ✅ BEZPIECZNE

Wszystkie endpoints są teraz prawidłowo zabezpieczone!

