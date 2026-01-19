# ✅ DOCKER CACHE CLEANUP & REBUILD

## 🔴 Problem
Docker cache zawiera starą wersję ReportController z błędami.

## ✅ Rozwiązanie

### Krok 1: Zatrzymaj kontenery
```bash
docker-compose down
```

### Krok 2: Wyczyść Docker system cache
```bash
docker system prune -a --volumes
```

Po poproszeniu o potwierdzenie wpisz: **y**

### Krok 3: Przebuduj bez cache
```bash
docker-compose build --no-cache
```

### Krok 4: Uruchom
```bash
docker-compose up
```

---

## 📝 Pełne polecenia (skopiuj i wklej)

### Windows PowerShell:
```powershell
docker-compose down
docker system prune -a --volumes
docker-compose build --no-cache
docker-compose up
```

### Linux/Mac:
```bash
docker-compose down
docker system prune -a --volumes
docker-compose build --no-cache
docker-compose up
```

---

## 🔍 Weryfikacja

Po uruchomieniu sprawdzaj logi:
```bash
docker logs cityfix-report-service
```

Szukaj:
- ✅ "Report Service started"
- ❌ Brak błędów kompilacji

---

## ⚠️ Czego Docker system prune robi

```
-a  : Usuwa wszystkie obrazy bez znacznika (unused images)
--volumes : Usuwa wszystkie nienazwane volumes
```

To **bezpieczne** - usuwa tylko nieużywane zasoby.

---

## 🎯 Po rebuild

```bash
# Testuj health endpoint
curl http://localhost:8082/reports/health

# Powinna być odpowiedź:
# Report Service is running
```

---

**Status:** Kod jest poprawny, Docker cache powoduje problem.
**Rozwiązanie:** Przebuduj wszystko bez cache.

