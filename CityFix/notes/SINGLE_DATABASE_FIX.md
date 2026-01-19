# ✅ NAPRAWIONY - Single Database Configuration

## 🐛 Problem

```
FATAL: database "cityfix_reports" does not exist
FATAL: database "cityfix_users" does not exist
FATAL: database "cityfix_locations" does not exist
```

## ✅ Przyczyna

Każdy serwis miał własną bazę danych, ale postgres-init.sql tworzy tylko `cityfix`:

**Było:**
- user-service → `cityfix_users`
- report-service → `cityfix_reports`
- location-service → `cityfix_locations`

**Powinno być:**
- Wszystkie serwisy → `cityfix` (JEDNA BAZA!)

## 🔧 Rozwiązanie

Zmieniono **docker-compose.yml** - wszystkie serwisy teraz używają `cityfix`:

```yaml
# user-service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cityfix

# report-service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cityfix

# location-service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cityfix
```

## 📝 Zmienione Pliki

| Plik | Zmiana |
|------|--------|
| docker-compose.yml | Wszystkie serwisy → `cityfix` |

---

## ✅ Architektura Bazy Danych

```
PostgreSQL Container (port 5432)
└─ POSTGRES_DB: cityfix (JEDNA BAZA!)
   ├─ users table (user-service)
   ├─ reports table (report-service)
   └─ locations table (location-service)
```

---

## 🚀 Następny Krok

```bash
# Wyczyść Docker
docker system prune -a --volumes

# Przebuduj i uruchom
docker-compose build --no-cache
docker-compose up
```

---

**Status:** ✅ Naprawione
**Data:** 2026-01-13

