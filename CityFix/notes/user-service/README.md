# User Service - Dokumentacja API

## Przegląd

User Service jest odpowiedzialny za zarządzanie użytkownikami, rejestrację, autentykację oraz edycję profili. Serwis wykorzystuje JWT do autentykacji (przechowywane w cookies) i BCrypt do haszowania haseł.

## Wymagania

- Java 17+
- PostgreSQL 15
- Spring Boot 3.2.0

## Funkcjonalności

### 1. Rejestracja użytkownika
**Endpoint:** `POST /users/register`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

**Walidacja:**
- `username`: wymagane, 3-50 znaków, unikalne
- `email`: wymagane, prawidłowy format email, unikalne
- `password`: wymagane, minimum 8 znaków

### 2. Logowanie
**Endpoint:** `POST /users/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

**HTTP Cookie Response Header:**
```
Set-Cookie: JWT_TOKEN=eyJhbGciOiJIUzUxMiJ9...; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=86400
```

**Błędy:**
- `400 Bad Request`: Nieprawidłowe username lub password

### 3. Pobranie danych użytkownika
**Endpoint:** `GET /users/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48123456789"
}
```

**Błędy:**
- `404 Not Found`: Użytkownik nie znaleziony

### 4. Edycja profilu użytkownika
**Endpoint:** `PUT /users/{id}`

**Wymogi:**
- Użytkownik musi być zalogowany (cookie JWT)
- Użytkownik może edytować tylko swój profil

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+48987654321"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+48987654321"
}
```

**Błędy:**
- `401 Unauthorized`: Brak lub nieprawidłowy JWT cookie
- `404 Not Found`: Użytkownik nie znaleziony
- `400 Bad Request`: Email już zajęty

### 5. Wylogowanie
**Endpoint:** `POST /users/logout`

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**HTTP Cookie Response Header:**
```
Set-Cookie: JWT_TOKEN=; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=0
```

## Bezpieczeństwo

### JWT (JSON Web Token) w Cookies

#### Jak Działa JWT

1. **Podczas Logowania:**
   - Użytkownik wysyła username + password
   - Serwer weryfikuje hasło (BCrypt)
   - Serwer generuje JWT token
   - **Serwer ustawia cookie zawierające JWT** zamiast zwracać token w response body

2. **Przy Każdym Żądaniu:**
   - Browser **automatycznie** wysyła cookie z JWT
   - Nie trzeba ręcznie dodawać headera `Authorization`
   - Serwer wyodrębnia JWT z cookies
   - Serwer waliduje token

3. **Podczas Wylogowania:**
   - Serwer wysyła cookie z `Max-Age=0`
   - Browser usuwa cookie
   - Token jest już nieprawidłowy

#### Cookies vs LocalStorage

| Cecha | Cookies | LocalStorage |
|-------|---------|--------------|
| **Automatyczne wysyłanie** | ✅ Tak | ❌ Nie |
| **HttpOnly flag** | ✅ Bezpieczne | ❌ Dostępne dla JS |
| **XSS Protection** | ✅ Wysoka | ❌ Niska |
| **CSRF Protection** | ❌ Wymaga tokenu | ✅ N/A |
| **Przechowywanie** | Serwer+Browser | Tylko Browser |

### Cookies HttpOnly

Nasze cookies są ustawiane z flagą **HttpOnly**, co oznacza:
- Nie są dostępne z JavaScript (`document.cookie`)
- Są wysyłane automatycznie z każdym żądaniem
- Są chronione przed XSS atakami
- Są wysyłane tylko na HTTPS (produkcja)

### Flagi Cookies

```
Set-Cookie: JWT_TOKEN=<token>; 
  Path=/;              # Dostępne na całej ścieżce /
  HttpOnly;            # Niedostępne z JS
  Secure;              # Tylko na HTTPS
  SameSite=Strict;     # Ochrona przed CSRF
  Max-Age=86400        # Wygaśnięcie za 24h
```

## Haszowanie Haseł (BCrypt)

Hasła są haszowane za pomocą **BCrypt**:
- Algorytm: bcrypt
- Strength: 10 (domyślne)
- Salt: Wygenerowany losowo dla każdego hasła
- Nigdy nie przechowujemy hasła w plaintext

## Baza Danych

### Tabela `users`
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Migracja Bazy Danych

Projekt używa Flyway do zarządzania migracjami:
- Pliki migracji: `src/main/resources/db/migration/`
- Format nazewnictwa: `V{version}__Description.sql`

## Uruchomienie

### Lokalnie
```bash
./gradlew bootRun
```

### W Docker-Compose
```bash
docker-compose up user-service
```

## Zmienne Środowiskowe

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cityfix_users
SPRING_DATASOURCE_USERNAME: cityfix_user
SPRING_DATASOURCE_PASSWORD: cityfix_password
JWT_SECRET: your-secret-key
JWT_EXPIRATION: 86400000
JWT_COOKIE_NAME: JWT_TOKEN
```

## Testowanie

```bash
./gradlew test
```

## Logi

Logi serwisu są dostępne na poziomie DEBUG dla pakietu `org.example.userservice`.

## Błędy i Obsługa Wyjątków

Serwis zwraca strukturyzowane odpowiedzi błędów:

```json
{
  "message": "Invalid username or password",
  "status": 400,
  "timestamp": "2024-01-13T10:30:00",
  "errors": {
    "field": "error message"
  }
}
```

## TODO (Przyszłe Ulepszenia)

- [ ] Refresh tokens (separate token dla odświeżania sesji)
- [ ] Two-factor authentication (2FA)
- [ ] Email verification
- [ ] Password reset functionality
- [ ] User roles and permissions
- [ ] Account lockout after failed attempts
- [ ] Audit logging

