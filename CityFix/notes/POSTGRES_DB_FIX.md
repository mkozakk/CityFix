# ✅ NAPRAWIONY - PostgreSQL Database Error

## 🐛 Problem

```
ERROR:  database "cityfix" already exists
STATEMENT:  CREATE DATABASE cityfix;
psql:/docker-entrypoint-initdb.d/init.sql:2: ERROR:  database "cityfix" already exists
```

## ✅ Przyczyna

W `docker-compose.yml` masz:
```yaml
postgres:
  environment:
    POSTGRES_DB: cityfix  # ← Tworzy bazę automatycznie!
```

A w `postgres-init.sql` miałeś:
```sql
CREATE DATABASE cityfix;  # ← Próbuje tworzyć ponownie!
```

**Konflikt:** Dwie operacje tworzą tę samą bazę.

## 🔧 Rozwiązanie

**Usunąłem** z `postgres-init.sql`:
- ❌ `CREATE DATABASE cityfix;`
- ❌ `\c cityfix;` (connection)

**Zostało:** Tylko tworzenie tabel, indeksów i constraints
- ✅ `CREATE TABLE IF NOT EXISTS users`
- ✅ `CREATE TABLE IF NOT EXISTS reports`
- ✅ `CREATE TABLE IF NOT EXISTS locations`
- ✅ Indeksy i Foreign Keys

## 📝 Zmieniony Plik

**postgres-init.sql**
- Linia 1-5: Komentarze wyjaśniające
- Linia 8+: Tworzenie tabel (bez CREATE DATABASE)

---

## 🚀 Następny Krok

```bash
# Wyczyść Docker i przebuduj
docker system prune -a --volumes
docker-compose build --no-cache
docker-compose up
```

---

**Status:** ✅ Naprawione
**Data:** 2024-01-13

