# JWT Configuration Guide

## Przegląd

User Service wykorzystuje JWT (JSON Web Token) do autentykacji i autoryzacji. Tokeny są generowane podczas logowania i muszą być przesyłane w każdym żądaniu do zabezpieczonych zasobów.

## Konfiguracja

### application.yml

```yaml
jwt:
  secret: your-secret-key-change-in-production-at-least-256-bits-long-for-security
  expiration: 86400000 # 24 hours in milliseconds
```

**Parametry:**
- `secret`: Tajny klucz używany do podpisania tokenów (ZMIEŃ W PRODUKCJI!)
- `expiration`: Czas wygaśnięcia tokenu w milisekundach

## Klucz Tajny (Secret Key)

### Wymagania
- Minimum 256 bitów dla HS512
- Powinien być losowy i bezpieczny
- NIE powinien być przechowywany w kodzie źródłowym

### Generowanie Bezpiecznego Klucza

#### Opcja 1: Bash/Linux
```bash
openssl rand -base64 32
```

#### Opcja 2: PowerShell (Windows)
```powershell
$bytes = New-Object byte[] 32
(New-Object Random).NextBytes($bytes)
[Convert]::ToBase64String($bytes)
```

#### Opcja 3: Java
```java
SecureRandom random = new SecureRandom();
byte[] key = new byte[64];
random.nextBytes(key);
String secretKey = Base64.getEncoder().encodeToString(key);
```

## Struktura JWT

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "johndoe",
  "iat": 1673612000,
  "exp": 1673698400
}
```

- `sub`: Subject (username użytkownika)
- `iat`: Issued At (czas wydania tokenu)
- `exp`: Expiration (czas wygaśnięcia tokenu)

### Signature
Hash generowany z użyciem sekretu i algorytmu HS512

## Użycie JWT

### 1. Uzyskanie Tokenu

**Request:**
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

**Response:**
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

### 2. Używanie Tokenu

Dodaj header `Authorization` z tokenem:

```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{
    "firstName": "Jonathan",
    "email": "jonathan@example.com"
  }'
```

## Walidacja JWT

### JwtTokenProvider

Serwis wykorzystuje `JwtTokenProvider` do:
- **Generowania** tokenów: `generateToken(username)`
- **Ekstrakcji** username: `getUsernameFromToken(token)`
- **Walidacji** tokenów: `validateToken(token)`

### JwtAuthenticationFilter

Filter automatycznie:
1. Wyodrębnia token z headera `Authorization`
2. Waliduje token
3. Ustawia Authentication context
4. Zezwala na dostęp do zabezpieczonych zasobów

## Bezpieczeństwo

### Best Practices

1. **Tajny Klucz**
   - Przechowuj w zmiennych środowiskowych
   - NIE umieszczaj w kodzie
   - Używaj minimum 256 bitów
   - Zmieniaj regularnie w produkcji

2. **Transmisja**
   - Zawsze używaj HTTPS w produkcji
   - NIE przesyłaj tokenu w URL
   - Umieść w headerie `Authorization`

3. **Przechowywanie Klienta**
   - Przechowuj w pamięci (nie w cookies bez flagi HttpOnly)
   - Nie przechowuj w localStorage jeśli to możliwe
   - Usuń po wylogowaniu

4. **Ekspiracja**
   - Ustaw krótką ekspirację (24h lub mniej)
   - Implementuj refresh tokens dla dłuższych sesji
   - Obsłużyć revokation tokenów

## Troubleshooting

### "Invalid token signature"
- Sprawdź czy secret key jest identyczny w konfiguracji
- Upewnij się że token nie został zmieniony

### "Token expired"
- Zaloguj się ponownie aby uzyskać nowy token
- Zwiększ czas expiration jeśli jest za krótki

### "Missing Authorization header"
- Upewnij się że przesyłasz header `Authorization`
- Format: `Authorization: Bearer <token>`

## Zmienne Środowiskowe

```bash
# .env
JWT_SECRET=your-generated-secure-key-here
JWT_EXPIRATION=86400000
```

```yaml
# docker-compose.yml
environment:
  JWT_SECRET: ${JWT_SECRET}
  JWT_EXPIRATION: ${JWT_EXPIRATION}
```

## Przyszłe Ulepszenia

- [ ] Refresh tokens
- [ ] Token blacklist/revocation
- [ ] Role-based access control (RBAC)
- [ ] API key authentication
- [ ] OAuth2 integration

