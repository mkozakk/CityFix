# User Service API - Dokumentacja Szczegółowa (v2 - Cookies)

## Spis Treści
1. [Informacje Ogólne](#informacje-ogólne)
2. [Autentykacja](#autentykacja)
3. [Rejestracja](#rejestracja)
4. [Logowanie](#logowanie)
5. [Pobieranie Profilu](#pobieranie-profilu)
6. [Edycja Profilu](#edycja-profilu)
7. [Wylogowanie](#wylogowanie)
8. [Kody Błędów](#kody-błędów)
9. [Przykłady](#przykłady)

## Informacje Ogólne

**Host:** `http://localhost:8081/api` (localhost) / `https://api.cityfix.com` (produkcja)

**Wersja:** 2.0 (JWT w Cookies)

**Content-Type:** `application/json`

**Uwierzytelnianie:** JWT w HTTP Cookie (HttpOnly)

---

## Autentykacja

### JWT w Cookies - Jak Działa

1. **Logowanie:** Server ustawia HTTP Cookie zawierającą JWT
2. **Browser:** Automatycznie przechowuje i wysyła cookie
3. **Frontend:** Nie musi nic robić - cookie wysyłana automatycznie
4. **Server:** Wyodrębnia JWT z cookies i waliduje
5. **Wylogowanie:** Server ustawia cookie Max-Age=0

### Cookie Flagi (Bezpieczeństwo)

```
Set-Cookie: JWT_TOKEN=eyJhbGciOi...; 
  HttpOnly;              # Niedostępne z JS (ochrona przed XSS)
  Secure;                # Tylko HTTPS (produkcja)
  SameSite=Strict;       # Ochrona przed CSRF
  Path=/;                # Dostępne na całej ścieżce
  Max-Age=86400          # Wygaśnięcie za 24h
```

### Frontend (Automatyczne)

```javascript
// Nie trzeba nic robić! Cookie wysyłana automatycznie
fetch('https://api.cityfix.com/users/1', {
  credentials: 'include' // Ważne dla cross-origin
});
```

---

## Rejestracja

### POST /users/register

Rejestracja nowego użytkownika w systemie.

### Request

**URL:** `POST /users/register`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

### Parametry

| Parametr | Typ | Wymagane | Ograniczenia | Opis |
|----------|-----|----------|--------------|------|
| `username` | String | Tak | 3-50 znaków, unikalne | Nazwa użytkownika do logowania |
| `email` | String | Tak | Email, unikalne | Adres email użytkownika |
| `password` | String | Tak | Minimum 8 znaków | Hasło (będzie zhaszowane) |
| `firstName` | String | Nie | Maksymalnie 100 znaków | Imię użytkownika |
| `lastName` | String | Nie | Maksymalnie 100 znaków | Nazwisko użytkownika |
| `phone` | String | Nie | Maksymalnie 20 znaków | Numer telefonu |

### Response

**Status Code:** `201 Created`

**Body:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

### Błędy

| Kod | Komunikat | Przyczyna |
|-----|-----------|-----------|
| 400 | Username is required | Brak username |
| 400 | Username must be between 3 and 50 characters | Username zbyt krótki/długi |
| 400 | Email is required | Brak email |
| 400 | Email should be valid | Email w nieprawidłowym formacie |
| 400 | Password is required | Brak hasła |
| 400 | Password must be at least 8 characters long | Hasło zbyt krótkie |
| 400 | Username already exists | Username jest już zajęty |
| 400 | Email already exists | Email jest już zajęty |
| 500 | Internal server error | Błąd serwera |

### Przykład

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+48123456789"
  }'
```

---

## Logowanie

### POST /users/login

Logowanie użytkownika i ustawienie JWT w cookie.

### Request

**URL:** `POST /users/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

### Parametry

| Parametr | Typ | Wymagane | Opis |
|----------|-----|----------|------|
| `username` | String | Tak | Nazwa użytkownika |
| `password` | String | Tak | Hasło użytkownika |

### Response

**Status Code:** `200 OK`

**Headers:**
```
Set-Cookie: JWT_TOKEN=eyJhbGciOi...; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=86400
```

**Body:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

**Notatka:** Token **jest automatycznie ustawiany w cookie** przez server. Frontend nie musi przechowywać ani obsługiwać tokenu!

### Błędy

| Kod | Komunikat | Przyczyna |
|-----|-----------|-----------|
| 400 | Username is required | Brak username |
| 400 | Password is required | Brak hasła |
| 400 | Invalid username or password | Username lub hasło nieprawidłowe |
| 500 | Internal server error | Błąd serwera |

### Przykład

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

**Notatka:** Flag `-c cookies.txt` zapisuje cookies do pliku (tylko dla cURL)

---

## Pobieranie Profilu

### GET /users/{id}

Pobranie informacji o użytkowniku po ID.

### Request

**URL:** `GET /users/{id}`

**Headers:** Brak wymaganych

**Cookies:** Automatycznie wysyłane (jeśli zalogowany)

### Path Parameters

| Parametr | Typ | Opis |
|----------|-----|------|
| `id` | Integer | ID użytkownika |

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

### Błędy

| Kod | Komunikat | Przyczyna |
|-----|-----------|-----------|
| 404 | User not found | Użytkownik o danym ID nie istnieje |
| 500 | Internal server error | Błąd serwera |

### Przykład

```bash
curl http://localhost:8081/api/users/1 \
  -b cookies.txt
```

---

## Edycja Profilu

### PUT /users/{id}

Edycja danych profilu użytkownika. **Wymaga autentykacji (JWT w cookies).**

### Request

**URL:** `PUT /users/{id}`

**Headers:**
```
Content-Type: application/json
```

**Cookies:** Automatycznie wysyłane

**Body:**
```json
{
  "firstName": "Jonathan",
  "lastName": "Smith",
  "email": "jonathan.smith@example.com",
  "phone": "+48987654321"
}
```

### Path Parameters

| Parametr | Typ | Opis |
|----------|-----|------|
| `id` | Integer | ID użytkownika do edycji |

### Body Parameters

| Parametr | Typ | Wymagane | Ograniczenia | Opis |
|----------|-----|----------|--------------|------|
| `firstName` | String | Nie | Maksymalnie 100 znaków | Nowe imię |
| `lastName` | String | Nie | Maksymalnie 100 znaków | Nowe nazwisko |
| `email` | String | Nie | Email, unikalne | Nowy adres email |
| `phone` | String | Nie | Maksymalnie 20 znaków | Nowy numer telefonu |

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "jonathan.smith@example.com",
  "firstName": "Jonathan",
  "lastName": "Smith",
  "phone": "+48987654321"
}
```

### Błędy

| Kod | Komunikat | Przyczyna |
|-----|-----------|-----------|
| 401 | Unauthorized | Brak lub nieprawidłowy JWT cookie |
| 404 | User not found | Użytkownik o danym ID nie istnieje |
| 400 | Email already exists | Email jest już zajęty |
| 400 | Email should be valid | Email w nieprawidłowym formacie |
| 500 | Internal server error | Błąd serwera |

### Przykład

```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "firstName": "Jonathan",
    "lastName": "Smith",
    "email": "jonathan.smith@example.com",
    "phone": "+48987654321"
  }'
```

---

## Wylogowanie

### POST /users/logout

Wylogowanie użytkownika i usunięcie JWT cookie.

### Request

**URL:** `POST /users/logout`

**Headers:** Brak wymaganych

**Cookies:** Automatycznie wysyłane

### Response

**Status Code:** `200 OK`

**Headers:**
```
Set-Cookie: JWT_TOKEN=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0
```

**Body:**
```json
{
  "message": "Logged out successfully"
}
```

### Przykład

```bash
curl -X POST http://localhost:8081/api/users/logout \
  -b cookies.txt
```

---

## Health Check

### GET /users/health

Sprawdzenie statusu serwisu.

### Request

**URL:** `GET /users/health`

**Headers:** Brak

### Response

**Status Code:** `200 OK`

**Body:**
```
User Service is running
```

### Przykład

```bash
curl http://localhost:8081/api/users/health
```

---

## Kody Błędów

### HTTP Status Codes

| Kod | Znaczenie | Opis |
|-----|-----------|------|
| 200 | OK | Żądanie pomyślne |
| 201 | Created | Zasób został utworzony |
| 400 | Bad Request | Błąd walidacji lub logiki biznesowej |
| 401 | Unauthorized | Brak autoryzacji/błędy JWT |
| 404 | Not Found | Zasób nie znaleziony |
| 500 | Internal Server Error | Błąd serwera |

### Error Response Format

```json
{
  "message": "Invalid username or password",
  "status": 400,
  "timestamp": "2024-01-13T10:30:00",
  "errors": {
    "username": "Username is required",
    "password": "Password must be at least 8 characters long"
  }
}
```

---

## Przykłady

### JavaScript/Fetch API

```javascript
// 1. Rejestracja
const register = async () => {
  const response = await fetch('/api/users/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'alice',
      email: 'alice@example.com',
      password: 'SecurePass456'
    }),
    credentials: 'include' // Wysyła cookies
  });
  return response.json();
};

// 2. Logowanie (Cookie ustawiana automatycznie)
const login = async () => {
  const response = await fetch('/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'alice',
      password: 'SecurePass456'
    }),
    credentials: 'include'
  });
  // JWT jest teraz w cookies!
  return response.json();
};

// 3. Żądanie Autentykowane (Cookie wysyłana automatycznie)
const getProfile = async (userId) => {
  const response = await fetch(`/api/users/${userId}`, {
    credentials: 'include'
  });
  return response.json();
};

// 4. Edycja (Cookie wysyłana automatycznie)
const updateProfile = async (userId, data) => {
  const response = await fetch(`/api/users/${userId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
    credentials: 'include'
  });
  return response.json();
};

// 5. Wylogowanie
const logout = async () => {
  await fetch('/api/users/logout', {
    method: 'POST',
    credentials: 'include'
  });
  // Cookie usunięta automatycznie
};
```

### cURL

```bash
# 1. Rejestracja
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "SecurePass456"
  }'

# 2. Logowanie
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -c cookies.txt \
  -d '{
    "username": "alice",
    "password": "SecurePass456"
  }'

# 3. Pobieranie Profilu
curl http://localhost:8081/api/users/1 \
  -b cookies.txt

# 4. Edycja Profilu
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "firstName": "Alicja",
    "email": "alicja@example.com"
  }'

# 5. Wylogowanie
curl -X POST http://localhost:8081/api/users/logout \
  -b cookies.txt
```

---

## Rate Limiting

Aktualnie brak limitowania przesyłania danych. W przyszłości zostanie zaimplementowany.

---

## CORS Configuration

Server akceptuje żądania cross-origin:

```yaml
# CORS Headers
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
```

**Ważne:** Dla cookies z credentials, frontend musi użyć `credentials: 'include'`

---

## Wersjonowanie API

Aktualnie API znajduje się w wersji 2.0 (JWT w Cookies).

Struktura wersjonowania: `/api/v1/users`, `/api/v2/users`, etc.

---

## Support

W przypadku problemów, skontaktuj się z zespołem DevOps lub sprawdź logi serwera.

