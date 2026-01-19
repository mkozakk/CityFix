# Contributing to User Service

## Wstęp

Dziękujemy za zainteresowanie wkładem w User Service! Poniżej znajduje się przewodnik jak wnieść swój wkład w projekt.

## Kod Postępowania

- Bądź szanujący wobec innych
- Unikaj ableistycznego języka
- Traktuj wszystkich uczestników sprawiedliwie

## Jak Zacząć

### 1. Forking i Klonowanie

```bash
git clone <repository-url>
cd user-service
```

### 2. Tworzenie Gałęzi

Twórz gałęzie z opisową nazwą:

```bash
git checkout -b feature/add-email-verification
git checkout -b fix/jwt-token-validation
git checkout -b docs/update-api-docs
```

### 3. Ustawienie Środowiska

```bash
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Standardy Kodowania

### Java Code Style

- Używaj 4 spacji do indentacji
- Maksymalnie 120 znaków na linię
- Nazwy klas w PascalCase
- Nazwy metod w camelCase
- Konstanencje w UPPER_SNAKE_CASE

### Przykład:

```java
@Service
public class UserService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    private final UserRepository userRepository;
    
    public UserResponse registerUser(RegisterRequest request) {
        // Implementation
    }
}
```

### Komentarze i Dokumentacja

- Pisz komentarze po angielsku
- Dokumentuj publiczne metody za pomocą JavaDoc
- Wyjaśniaj "dlaczego", nie "co"

```java
/**
 * Registers a new user in the system.
 *
 * @param request The registration request containing user data
 * @return The created user response
 * @throws IllegalArgumentException if username or email already exists
 */
public UserResponse registerUser(RegisterRequest request) {
    // Implementation
}
```

## Testy

### Uruchomienie Testów

```bash
# Wszystkie testy
./gradlew test

# Konkretne testy
./gradlew test --tests UserServiceTest

# Z pokryciem kodu
./gradlew test jacocoTestReport
```

### Pisanie Testów

- Testuj jedną rzecz na test
- Używaj descriptive nazw
- Stosuj pattern AAA (Arrange, Act, Assert)

```java
@Test
void testRegisterUserWithValidData() {
    // Arrange
    RegisterRequest request = createValidRequest();
    
    // Act
    UserResponse response = userService.register(request);
    
    // Assert
    assertNotNull(response);
    assertEquals("testuser", response.getUsername());
}
```

## Commit Messages

Używaj konwencji Conventional Commits:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Typy:

- `feat`: Nowa funkcjonalność
- `fix`: Naprawa błędu
- `docs`: Dokumentacja
- `style`: Formatowanie kodu
- `refactor`: Refaktoryzacja
- `test`: Dodanie testów
- `chore`: Konfiguracja build-u

### Przykłady:

```
feat(auth): add email verification

fix(jwt): handle token expiration correctly

docs(api): update API documentation

refactor(user): simplify registration logic
```

## Pull Requests

### Przed Submisją

1. **Zupdate gałąź:**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Uruchom testy:**
   ```bash
   ./gradlew test
   ```

3. **Sprawdź kod:**
   ```bash
   ./gradlew checkstyleMain
   ```

4. **Kompiluj projekt:**
   ```bash
   ./gradlew clean build
   ```

### Opis PR

Utwórz opis zawierający:

```markdown
## Description
Krótki opis zmian

## Related Issues
Closes #123

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Opisz jak przetestowałeś zmiany

## Checklist
- [ ] Testy przechodzą
- [ ] Dokumentacja zaktualizowana
- [ ] Brak zmian w API bez uzasadnienia
- [ ] Kod odpowiednio skomentowany
```

## Dokumentacja

### Aktualizacja Dokumentacji

Jeśli twoje zmiany wpływają na:
- API endpoints → update `API_DOCUMENTATION.md`
- Bazę danych → update `DATABASE_SCHEMA.md`
- JWT → update `JWT_CONFIGURATION.md`
- Instalację → update `RUNNING.md`

## Database Migrations

Jeśli dodajesz migrację:

1. Utwórz nowy plik w `src/main/resources/db/migration/`
2. Używaj konwencji: `V{version}__Description.sql`
3. Numeracja musi być sekwencyjna
4. Testuj migrację lokalnie

```sql
-- V2__Add_user_status_column.sql
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
```

## Review Process

1. **Code Review**: Przynajmniej jeden reviewer musi zatwierdzić
2. **Tests**: Wszystkie testy muszą przechodzić
3. **Documentation**: Dokumentacja musi być zaktualizowana
4. **Security**: Brak problemów bezpieczeństwa
5. **Performance**: Brak regresji wydajności

## Zgłaszanie Błędów

### Koniecznie Dołącz:

```markdown
**Environment:**
- Java version: 17
- OS: Windows/Linux/Mac
- Branch: main/develop

**Steps to Reproduce:**
1. Register user
2. Try to login
3. See error

**Expected Behavior:**
Powinno się zalogować

**Actual Behavior:**
Wyświetla się błąd 401

**Logs:**
[paste relevant logs]
```

## Sugestie Ulepszeń

Otwórz Issue z etykietą `enhancement` zawierającą:

1. **Problem**: Czego ci brakuje?
2. **Rozwiązanie**: Twoja propozycja
3. **Alternatywy**: Inne opcje
4. **Kontekst**: Dlaczego jest to ważne?

## Wdrażanie

- `main` branch: Wersja produkcyjna
- `develop` branch: Wersja development
- PR→develop są mergeowane po review
- Releases from `main` tylko po testowaniu

## Setup IDE

### IntelliJ IDEA

1. Otwórz projekt
2. Gradle sync
3. Konfiguracja Run Configuration:
   ```
   Main class: org.example.userservice.UserServiceApplication
   Active profiles: local
   ```

### VS Code

```json
{
  "launch": {
    "version": "0.2.0",
    "configurations": [
      {
        "type": "java",
        "name": "UserService",
        "request": "launch",
        "mainClass": "org.example.userservice.UserServiceApplication",
        "args": "--spring.profiles.active=local"
      }
    ]
  }
}
```

## Helpful Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JWT Introduction](https://jwt.io/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://flywaydb.org/)

## Questions?

Otwórz diskusję w Issues lub skontaktuj się z maintainerami.

Dziękujemy za wkład! 🎉

