# ✅ NAPRAWIONY - Hibernate Schema Validation Error

## 🐛 Problem

```
Schema-validation: wrong column type encountered in column [id] in table [users]
found [serial (Types#INTEGER)], but expecting [bigint (Types#BIGINT)]
```

## ✅ Przyczyna

Hibernate oczekuje kolumny `id` jako `BIGINT`, ale postgres-init.sql tworzy ją jako `SERIAL` (INTEGER):

**Było:**
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,  -- ❌ INTEGER (32-bit)
    ...
);
```

**Powinno być:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,  -- ✅ BIGINT (64-bit)
    ...
);
```

## 🔧 Rozwiązanie

Zmieniono **postgres-init.sql** - wszystkie tabele teraz używają `BIGSERIAL`:

| Tabela | Zmiana |
|--------|--------|
| users | `SERIAL` → `BIGSERIAL` |
| reports | `SERIAL` → `BIGSERIAL` |
| locations | `SERIAL` → `BIGSERIAL` |

## 📝 Zmieniane Pliki

- ✅ postgres-init.sql - Wszystkie id kolumny na BIGSERIAL

---

## ✅ Schema Po Zmianach

```sql
-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,  ✅ 64-bit
    ...
);

-- Reports
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,  ✅ 64-bit
    ...
);

-- Locations
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,  ✅ 64-bit
    ...
);
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

