# ✅ NAPRAWIONY - PostgreSQL Syntax Error

## 🐛 Problem

```
ERROR:  syntax error at or near "NOT" at character 39
STATEMENT:  ALTER TABLE reports ADD CONSTRAINT IF NOT EXISTS fk_reports_user
```

## ✅ Przyczyna

PostgreSQL **nie obsługuje** `IF NOT EXISTS` w `ALTER TABLE ADD CONSTRAINT`.

Ta składnia nie istnieje:
```sql
-- ❌ BŁĄD - PostgreSQL nie obsługuje
ALTER TABLE reports ADD CONSTRAINT IF NOT EXISTS fk_reports_user ...
```

## 🔧 Rozwiązanie

Zmieniono na **DO block z exception handling**:

```sql
-- ✅ POPRAWNIE - PostgreSQL obsługuje
DO $$
BEGIN
    ALTER TABLE reports ADD CONSTRAINT fk_reports_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
```

**Jak działa:**
1. Próbuje dodać constraint
2. Jeśli już istnieje (duplicate_object) → Ignoruj błąd
3. W innym wypadku → Dodaj constraint

## 📝 Zmieniony Plik

**postgres-init.sql** (linie 73-90)
- ❌ Usunięto: `ALTER TABLE ... ADD CONSTRAINT IF NOT EXISTS`
- ✅ Dodano: `DO $$ BEGIN ... EXCEPTION WHEN duplicate_object THEN NULL; END $$;`

---

## ✅ Struktura Constraints w postgres-init.sql

```sql
-- Foreign key: reports.user_id -> users.id
DO $$
BEGIN
    ALTER TABLE reports ADD CONSTRAINT fk_reports_user ...
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Foreign key: locations.report_id -> reports.id
DO $$
BEGIN
    ALTER TABLE locations ADD CONSTRAINT fk_locations_report ...
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
```

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
**Data:** 2026-01-13

