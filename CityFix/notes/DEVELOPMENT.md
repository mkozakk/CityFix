# Przewodnik Rozwoju - CityFix

## Wstęp

Ten dokument opisuje, jak skonfigurować środowisko rozwojowe i pracować nad projektami CityFix.

## Wymagania Systemowe

- **JDK 17+**: [https://adoptium.net/](https://adoptium.net/)
- **Docker & Docker Compose**: [https://www.docker.com/](https://www.docker.com/)
- **Git**: [https://git-scm.com/](https://git-scm.com/)
- **IDE**: IntelliJ IDEA, Eclipse, lub Visual Studio Code
- **Gradle**: Jest dołączony (./gradlew)

## Konfiguracja Środowiska

### 1. Klonowanie Repozytorium

```bash
git clone <repository-url>
cd CityFix
```

### 2. Konfiguracja IDE (IntelliJ IDEA)

1. Otwórz projekt w IntelliJ IDEA
2. Gradle będzie automatycznie zaimportowany
3. Poczekaj na pobieranie zależności

### 3. Instalacja Docker Desktop

Upewnij się, że Docker Desktop jest zainstalowany i uruchomiony:

```bash
docker --version
docker-compose --version
```

## Uruchomienie Projektu

### Opcja 1: Docker Compose (Rekomendowane)

```bash
# Budowanie i uruchomienie wszystkich serwisów
docker-compose up --build

# Aplikacja będzie dostępna na http://localhost:8080
```

### Opcja 2: Uruchamianie Lokalnie (Development Mode)

```bash
# Terminal 1: Uruchomienie PostgreSQL i RabbitMQ
docker-compose up postgres rabbitmq

# Terminal 2: User Service
cd user-service
./gradlew bootRun

# Terminal 3: Report Service
cd report-service
./gradlew bootRun

# Terminal 4: Location Service
cd location-service
./gradlew bootRun

# Terminal 5: Gateway
cd gateway
./gradlew bootRun
```

## Struktura Projektu

```
CityFix/
├── user-service/              # Serwis użytkowników
│   ├── src/main/java/
│   │   └── org/example/userservice/
│   │       ├── UserServiceApplication.java
│   │       └── controller/
│   │           └── UserController.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── src/test/java/
│   ├── build.gradle.kts
│   └── Dockerfile
│
├── report-service/            # Serwis raportów
├── location-service/          # Serwis lokalizacji
├── gateway/                   # API Gateway
│
├── docker-compose.yml         # Orkestracja kontenerów
├── postgres-init.sql         # Inicjalizacja BD
├── README.md                 # Dokumentacja główna
├── ARCHITECTURE.md           # Dokumentacja architekturalnej
└── DEVELOPMENT.md            # Ten plik
```

## Praca z Serwisami

### Dodanie Nowej Klasy/Kontrolera

1. Przejdź do odpowiedniego serwisu
2. Utwórz nowy plik w `src/main/java/org/example/{servicename}/`
3. Przykład dla User Service:

```java
package org.example.userservice.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    // Implementacja
}
```

### Dodanie Nowego Endpoint'u

```java
@PostMapping("/users/register")
public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterUserRequest request) {
    log.info("Registering new user: {}", request.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
}
```

### Dodanie Nowej Zależności

1. Otwórz `build.gradle.kts` w serwisie
2. Dodaj zależność:

```kotlin
dependencies {
    implementation("com.google.guava:guava:32.1.3-jre")
}
```

3. Uruchom: `./gradlew refresh`

## Testy

### Uruchamianie Testów

```bash
# Testy w konkretnym serwisie
cd user-service
./gradlew test

# Testy ze szczegółowymi informacjami
./gradlew test --info

# Testy konkretnej klasy
./gradlew test --tests UserControllerTest
```

### Pisanie Testów

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("test@example.com");

        // Act
        UserDTO result = userService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }
}
```

## Build i Deployment

### Budowanie Projektu

```bash
# Budowanie jednego serwisu
cd user-service
./gradlew clean build

# Budowanie bez testów
./gradlew clean build -x test

# Budowanie wszystkich serwisów
./gradlew clean build --parallel
```

### Tworzenie Docker Image'u

```bash
# Ręczne budowanie image'u
cd user-service
docker build -t cityfix-user-service:latest .

# Uruchomienie kontenera
docker run -p 8081:8081 cityfix-user-service:latest
```

## Debugging

### Uruchamianie z Debug Mode

```bash
# Terminal
cd user-service
./gradlew bootRun --debug-jvm

# IDE: Ustaw breakpoint i attach debugger na port 5005
```

### Sprawdzenie Logów

```bash
# Logi Docker Compose
docker-compose logs -f user-service

# Logi konkretnej linii
docker-compose logs --tail=100 user-service
```

### Czyszczenie Danych

```bash
# Usunięcie kontenerów i volumów
docker-compose down -v

# Ponowne uruchomienie
docker-compose up --build
```

## Zmienne Środowiskowe

### Konfiguracja Lokalnego Development'u

Utwórz plik `.env.local` w głównym katalogu:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cityfix_users
SPRING_DATASOURCE_USERNAME=cityfix_user
SPRING_DATASOURCE_PASSWORD=cityfix_password
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

### Załadowanie Zmiennych w IDE

**IntelliJ IDEA**:
1. Run → Edit Configurations
2. Dodaj VM options: `-Dspring.profiles.active=local`
3. Ustaw Environment variables

## Konwencje Kodowania

### Nazewnictwo Klas

```java
// Controllers
UserController.java
ReportController.java
LocationController.java

// Services
UserService.java
ReportService.java

// Entities
User.java
Report.java
Location.java

// DTOs
UserDTO.java
CreateUserRequest.java
UpdateUserRequest.java
```

### Struktura Pakietów

```
org.example.userservice
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # Data Access
├── entity/          # JPA Entities
├── dto/             # Data Transfer Objects
├── exception/       # Custom Exceptions
└── config/          # Configuration Classes
```

### Formatowanie Kodu

Kod powinien być sformatowany zgodnie ze standardem Java:
- 4 spacje dla indentacji
- Linie max 120 znaków
- Używaj Lombok do zmniejszenia boilerplate'u

## Kontrola Wersji

### Git Workflow

```bash
# Utwórz nowy branch
git checkout -b feature/user-authentication

# Pracuj nad kodem
git add .
git commit -m "feat: Add user authentication"

# Wyślij do repozytorium
git push origin feature/user-authentication

# Utwórz Pull Request
```

### Konwencje Commit'ów

- `feat:` - Nowa funkcja
- `fix:` - Poprawka błędu
- `docs:` - Dokumentacja
- `style:` - Formatowanie
- `refactor:` - Refaktoryzacja
- `test:` - Testy
- `chore:` - Zmiany narzędziowe

## Przydatne Komendy

```bash
# Status projektu
docker-compose ps

# Sprawdzenie logów
docker-compose logs -f

# Restart serwisu
docker-compose restart user-service

# Shell w kontenerze
docker-compose exec user-service bash

# Usunięcie wszystkich danych
docker-compose down -v

# Rebuild image'u
docker-compose up --build --no-deps user-service
```

## Troubleshooting

### Problem: "Port already in use"

```bash
# Znalezienie procesu na porcie 8080
lsof -i :8080

# Zabicie procesu
kill -9 <PID>

# Lub zmiana portu w docker-compose.yml
```

### Problem: "Database connection refused"

```bash
# Sprawdzenie statusu PostgreSQL
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# Poczekaj 10 sekund na inicjalizację
```

### Problem: "Gradle build failed"

```bash
# Czyszczenie cache'u
./gradlew clean

# Refresh zależności
./gradlew refresh

# Pełna rebuild
./gradlew clean build --refresh-dependencies
```

## Recursos

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Docker Documentation](https://docs.docker.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

## Kontakt

Jeśli masz pytania, proszę otworzyć issue w repozytorium.

