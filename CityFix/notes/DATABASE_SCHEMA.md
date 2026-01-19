# User Service Database Schema

## Przegląd

User Service przechowuje dane użytkowników w tabeli `users`. Baza danych jest inicjalizowana za pomocą skryptów SQL i migracji Flyway.

## Tabela `users`

### Struktura

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

### Kolumny

| Kolumna | Typ | Ograniczenia | Opis |
|---------|-----|--------------|------|
| `id` | SERIAL | PRIMARY KEY | Unikalny identyfikator użytkownika |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Nazwa użytkownika (login) |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Adres email użytkownika |
| `password` | VARCHAR(255) | NOT NULL | Haszowane hasło (BCrypt) |
| `first_name` | VARCHAR(100) | NULL | Imię użytkownika |
| `last_name` | VARCHAR(100) | NULL | Nazwisko użytkownika |
| `phone` | VARCHAR(20) | NULL | Numer telefonu |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Data utworzenia rekordu |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Data ostatniej aktualizacji |

## Indeksy

```sql
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### Indeksy

| Indeks | Kolumny | Cel |
|--------|---------|-----|
| `idx_users_username` | username | Przyspieszenie wyszukiwania po username |
| `idx_users_email` | email | Przyspieszenie wyszukiwania po email |

## Ograniczenia

### Unique Constraints
- `username`: Każda nazwa użytkownika musi być unikalna
- `email`: Każdy email musi być unikalny

### NOT NULL Constraints
- `username`: Wymagane
- `email`: Wymagane
- `password`: Wymagane

## Migracje (Flyway)

### Plik: `V1__Create_users_table.sql`

Zawiera:
- Utworzenie tabeli `users`
- Utworzenie indeksów
- Ustawienie domyślnych wartości

### Kolejne Migracje

Będą dodane w następnym formacie:
- `V2__Add_column_name.sql`
- `V3__Add_constraint_name.sql`

## Bezpieczeństwo

### Haszowanie Haseł

Hasła są haszowane za pomocą BCrypt przed przechowaniem:

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("plainTextPassword");
```

**Właściwości BCrypt:**
- Algorytm: bcrypt
- Strength: 10 (domyślne)
- Salt: Wygenerowany losowo dla każdego hasła

### Walidacja Email

Email jest walidowany:
- Format RFC 5322
- Unikalność w bazie danych
- Wymagane podczas rejestracji

## Przykłady Zapytań

### Pobranie Użytkownika po Username

```sql
SELECT * FROM users WHERE username = 'johndoe';
```

### Pobranie Użytkownika po Email

```sql
SELECT * FROM users WHERE email = 'john.doe@example.com';
```

### Sprawdzenie Istnienia Username

```sql
SELECT EXISTS(SELECT 1 FROM users WHERE username = 'johndoe');
```

### Aktualizacja Danych Użytkownika

```sql
UPDATE users 
SET first_name = 'Jonathan', 
    last_name = 'Smith',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 1;
```

### Usunięcie Użytkownika

```sql
DELETE FROM users WHERE id = 1;
```

## Statystyki Tabeli

```sql
-- Liczba użytkowników
SELECT COUNT(*) as total_users FROM users;

-- Ostatni zalogowani użytkownicy
SELECT id, username, email, updated_at 
FROM users 
ORDER BY updated_at DESC 
LIMIT 10;

-- Użytkownicy bez numeru telefonu
SELECT username, email FROM users WHERE phone IS NULL;
```

## Backup i Restore

### Backup

```bash
# Backup całej bazy danych
pg_dump -U cityfix_user -d cityfix > backup.sql

# Backup pojedynczej tabeli
pg_dump -U cityfix_user -d cityfix -t users > users_backup.sql
```

### Restore

```bash
# Restore całej bazy danych
psql -U cityfix_user -d cityfix < backup.sql

# Restore pojedynczej tabeli
psql -U cityfix_user -d cityfix < users_backup.sql
```

## Zmiana Schematu

### Dodanie Kolumny

1. Utwórz migrację `V2__Add_new_column.sql`:
```sql
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
```

2. Uruchom migrację:
```bash
./gradlew bootRun
```

### Zmiana Typu Kolumny

1. Utwórz migrację `V3__Alter_column_type.sql`:
```sql
ALTER TABLE users ALTER COLUMN phone TYPE VARCHAR(30);
```

## Monitoring

### Rozmiar Tabeli

```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE tablename = 'users';
```

### Liczba Wierszy na Sekund

```sql
SELECT 
    n_live_tup as live_rows,
    n_dead_tup as dead_rows,
    last_vacuum,
    last_autovacuum
FROM pg_stat_user_tables
WHERE relname = 'users';
```

## TODO

- [ ] Dodać pole `status` (ACTIVE/INACTIVE/BANNED)
- [ ] Dodać pole `last_login`
- [ ] Dodać pole `login_attempts`
- [ ] Dodać tabelę `audit_log` dla śledzenia zmian
- [ ] Dodać tabelę `password_reset_tokens`
- [ ] Implementować partycjonowanie dla dużych zbiorów danych

