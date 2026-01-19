# Testy Jednostkowe - Podsumowanie

Data utworzenia: 2026-01-13

## Przegląd

Dodałem kompleksowe testy jednostkowe dla wszystkich trzech usług CityFix:
- **user-service**: 12 testów
- **report-service**: 12 testów
- **log-service**: 14 testów

**RAZEM: 38 testów** - wszystkie przeszły pomyślnie ✅

---

## User Service Tests (UserServiceTest.java)

### Liczba testów: 12

#### Register (3 testy)
- `testRegisterSuccess()` - Rejestracja nowego użytkownika
- `testRegisterUsernameTaken()` - Błąd gdy username już istnieje
- `testRegisterEmailTaken()` - Błąd gdy email już istnieje

#### Login (3 testy)
- `testLoginSuccess()` - Pomyślne logowanie i generacja tokena JWT
- `testLoginUserNotFound()` - Błąd gdy użytkownik nie istnieje
- `testLoginInvalidPassword()` - Błąd przy złym haśle

#### Get User (3 testy)
- `testGetUserByIdSuccess()` - Pobieranie użytkownika po ID
- `testGetUserByIdNotFound()` - Błąd gdy użytkownik nie istnieje
- `testGetUserByUsernameSuccess()` - Pobieranie użytkownika po username
- `testGetUserByUsernameNotFound()` - Błąd gdy username nie istnieje

#### Update User (2 testy)
- `testUpdateUserSuccess()` - Aktualizacja profilu użytkownika
- `testUpdateUserWithDuplicateEmail()` - Błąd przy duplikacie email

---

## Report Service Tests (ReportServiceTest.java)

### Liczba testów: 12

#### Create Report (2 testy)
- `testCreateReportSuccess()` - Pomyślne utworzenie reportu
- `testCreateReportWithDefaultPriority()` - Raport z domyślnym priorytetem

#### Get Reports (3 testy)
- `testGetAllReportsSuccess()` - Pobieranie wszystkich raportów
- `testGetAllReportsEmpty()` - Zwracanie pustej listy
- `testGetReportByIdSuccess()` - Pobieranie raportu po ID
- `testGetReportByIdNotFound()` - Błąd gdy raport nie istnieje

#### Update Report (3 testy)
- `testUpdateReportSuccess()` - Pomyślna aktualizacja raportu
- `testUpdateReportUnauthorized()` - Błąd przy próbie aktualizacji cudzego raportu
- `testUpdateReportNotFound()` - Błąd gdy raport nie istnieje

#### Delete Report (3 testy)
- `testDeleteReportSuccess()` - Pomyślne usunięcie raportu
- `testDeleteReportUnauthorized()` - Błąd przy próbie usunięcia cudzego raportu
- `testDeleteReportNotFound()` - Błąd gdy raport nie istnieje

---

## Log Service Tests (AuditLogServiceTest.java)

### Liczba testów: 14

#### Log Event (2 testy)
- `testLogEventSuccess()` - Pomyślne zalogowanie zdarzenia
- `testLogEventWithoutTimestamp()` - Logowanie bez timestampa (użycie domyślnego)

#### Get All Logs (3 testy)
- `testGetAllLogsSuccess()` - Pobieranie wszystkich logów
- `testGetAllLogsEmpty()` - Zwracanie pustej listy
- `testGetAllLogsWithCustomLimit()` - Pobieranie z customowym limitem

#### Get Logs by User ID (3 testy)
- `testGetLogsByUserIdSuccess()` - Pobieranie logów dla użytkownika
- `testGetLogsByUserIdEmpty()` - Brak logów dla użytkownika
- `testGetLogsByUserIdMultipleRecords()` - Wiele logów dla użytkownika

#### Get Logs by Event Type (3 testy)
- `testGetLogsByEventTypeSuccess()` - Pobieranie logów po typie zdarzenia
- `testGetLogsByEventTypeEmpty()` - Brak logów dla typu zdarzenia
- `testGetLogsByEventTypeMultipleRecords()` - Wiele logów danego typu

#### Get Logs by Date Range (3 testy)
- `testGetLogsByDateRangeSuccess()` - Pobieranie logów z zakresu dat
- `testGetLogsByDateRangeEmpty()` - Brak logów w danym zakresie
- `testGetLogsByDateRangeMultipleRecords()` - Wiele logów w zakresie dat

---

## Zmiany w Build Configuration

### user-service/build.gradle.kts
- Dodano dependencję `mockito-core:5.2.0`
- Dodano dependencję `mockito-junit-jupiter:5.2.0`

### report-service/build.gradle.kts
- Dodano dependencję `mockito-core:5.2.0`
- Dodano dependencję `mockito-junit-jupiter:5.2.0`

### log-service/build.gradle.kts
- Dodano dependencję `mockito-core:5.2.0`
- Dodano dependencję `mockito-junit-jupiter:5.2.0`

---

## DTOs - Dodane @Builder

Dla ułatwienia tworzenia testowych objektów, dodałem anotacje `@Builder` do:
- `RegisterRequest.java` (user-service)
- `LoginRequest.java` (user-service)
- `UpdateUserRequest.java` (user-service)

---

## Uruchomienie testów

**Wszystkie poniższe polecenia uruchamiaj z głównego katalogu projektu** (gdzie znajduje się `gradlew.bat`)

### 🚀 Wszystkie testy jednocześnie (NAJŁATWIEJ!)
```bash
.\gradlew.bat test
```
To polecenie automatycznie uruchomi testy ze wszystkich podmodułów:
- user-service
- report-service
- log-service

### Indywidualne testy

#### User Service
```bash
.\gradlew.bat -p user-service test
```

#### Report Service
```bash
.\gradlew.bat -p report-service test
```

#### Log Service
```bash
.\gradlew.bat -p log-service test
```

### Czyszczenie cache i ponowny test

#### Wszystkie
```bash
.\gradlew.bat clean test
```

#### Poszczególne usługi
```bash
.\gradlew.bat -p user-service clean test
.\gradlew.bat -p report-service clean test
.\gradlew.bat -p log-service clean test
```

### Wynik ostatniego uruchomienia (test)

```
BUILD SUCCESSFUL in 20s

Log-Service:      14 tests ✅
Report-Service:   12 tests ✅
User-Service:     12 tests ✅

TOTAL:            38 tests PASSED ✅
```

---

## Framework i narzędzia

- **JUnit 5** - Test framework
- **Mockito 5.2.0** - Mocking framework
- **Spring Boot Test** - Spring Boot testing support
- **Lombok** - Annotations dla entity i DTO

---

## Coverage

Testy pokrywają:
- ✅ Wszystkie główne metody serwisów
- ✅ Scenariusze sukcesu
- ✅ Scenariusze błędów/exceptions
- ✅ Walidacja uprawnień (authorization)
- ✅ Edge cases (duplikaty, nie znalezione zasoby, itp.)

---

## Wyniki

```
user-service:     12/12 testów PASSOU ✅
report-service:   12/12 testów PASSOU ✅
log-service:      14/14 testów PASSOU ✅
─────────────────────────────────────
RAZEM:            38/38 testów PASSOU ✅
```

Wszystkie testy zostały pomyślnie uruchomione i przeszły bez błędów!

---

## FAQ - Jak odpalić testy?

### P: Gdzie są testy?
**O:** Testy znajdują się w:
- `user-service/src/test/java/org/example/userservice/service/UserServiceTest.java`
- `report-service/src/test/java/org/example/reportservice/service/ReportServiceTest.java`
- `log-service/src/test/java/org/example/logservice/service/AuditLogServiceTest.java`

### P: Jak je odpalić?
**O:** Wszystkie testy naraz (najłatwiej):
```bash
.\gradlew.bat test
```

Lub poszczególne usługi:
```bash
.\gradlew.bat -p user-service test
.\gradlew.bat -p report-service test
.\gradlew.bat -p log-service test
```

### P: Gdzie jest gradlew?
**O:** W głównym katalogu CityFix:
```
C:\Users\light\Desktop\zal\CityFix\gradlew.bat
```

### P: Jaki folder muszę otworzyć w terminalu?
**O:** Główny folder projektu:
```
C:\Users\light\Desktop\zal\CityFix
```

### P: Co oznacza `-p` w poleceniu?
**O:** To flaga path, która mówi Gradle'owi który podmoduł wykonać:
```bash
.\gradlew.bat -p user-service test
           ↑ flaga path
                      ↑ nazwa podmodułu
                                  ↑ zadanie
```

### P: Gdzie są wyniki testów?
**O:** Po uruchomieniu testów, wyniki są w:
- `user-service/build/test-results/test/`
- `report-service/build/test-results/test/`
- `log-service/build/test-results/test/`

Pliki XML zawierają szczegóły każdego testu.

### P: Jak mogę zobaczyć szczegóły testów?
**O:** Otwórz plik XML w przeglądarce:
- `user-service/build/test-results/test/TEST-org.example.userservice.service.UserServiceTest.xml`

### P: Czy mogę uruchomić testy z IDE?
**O:** Tak! W IntelliJ IDEA:
1. Otwórz plik testowy (np. UserServiceTest.java)
2. Kliknij zieloną strzałkę obok nazwy klasy lub metody
3. Wybierz "Run"

---

