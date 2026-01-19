# User Service API - Dokumentacja Szczegółowa

## Spis Treści
1. [Informacje Ogólne](#informacje-ogólne)
2. [Autentykacja](#autentykacja)
3. [Rejestracja](#rejestracja)
4. [Logowanie](#logowanie)
5. [Pobieranie Profilu](#pobieranie-profilu)
6. [Edycja Profilu](#edycja-profilu)
7. [Kody Błędów](#kody-błędów)
8. [Przykłady](#przykłady)

## Informacje Ogólne

**Host:** `http://localhost:8081/api` (localhost) / `https://api.cityfix.com` (produkcja)

**Wersja:** 1.0

**Content-Type:** `application/json`

**Uwierzytelnianie:** JWT (Bearer Token)

---

## Autentykacja

Większość endpointów wymaga autentykacji za pomocą JWT. Token jest zwracany po pomyślnym logowaniu.

### Header Autentykacji

```
Authorization: Bearer <JWT_TOKEN>
```

### Przykład
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  http://localhost:8081/api/users/1
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

Logowanie użytkownika i uzyskanie JWT tokenu.

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

**Body:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjczNjEyMDAwLCJleHAiOjE2NzM2OTg0MDB9.signature",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

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
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

---

## Pobieranie Profilu

### GET /users/{id}

Pobranie informacji o użytkowniku po ID.

### Request

**URL:** `GET /users/{id}`

**Headers:** Opcjonalne

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
curl http://localhost:8081/api/users/1
```

---

## Edycja Profilu

### PUT /users/{id}

Edycja danych profilu użytkownika. Wymaga autentykacji JWT.

### Request

**URL:** `PUT /users/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

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
| 401 | Unauthorized | Brak lub nieprawidłowy JWT token |
| 404 | User not found | Użytkownik o danym ID nie istnieje |
| 400 | Email already exists | Email jest już zajęty |
| 400 | Email should be valid | Email w nieprawidłowym formacie |
| 500 | Internal server error | Błąd serwera |

### Przykład

```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{
    "firstName": "Jonathan",
    "lastName": "Smith",
    "email": "jonathan.smith@example.com",
    "phone": "+48987654321"
  }'
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

### Pełny Workflow

#### 1. Rejestracja
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "SecurePass456",
    "firstName": "Alice",
    "lastName": "Johnson"
  }'
```

#### 2. Logowanie
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "SecurePass456"
  }'
```

Odpowiedź (zapisz token):
```json
{
  "id": 2,
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 3. Pobieranie Profilu
```bash
curl http://localhost:8081/api/users/2
```

#### 4. Edycja Profilu
```bash
curl -X PUT http://localhost:8081/api/users/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{
    "firstName": "Alicja",
    "email": "alicja@example.com"
  }'
```

---

## Rate Limiting

Aktualnie brak limitowania przesyłania danych. W przyszłości zostanie zaimplementowany.

---

## Wersjonowanie API

Aktualnie API znajduje się w wersji 1.0. W przyszłości będą dodane nowe wersje.

Struktura wersjonowania: `/api/v1/users`, `/api/v2/users`, etc.

---

## Support

W przypadku problemów, skontaktuj się z zespołem DevOps lub sprawdź logi serwera.

