# ✅ INSTRUKCJA - Przebudowanie Docker bez cache

## Problem
Docker cache'uje starą wersję kodu. Pliki są już naprawione, ale Docker nie wie o zmianach.

## Rozwiązanie

### Opcja 1: Wyczyść Docker cache i przebuduj

```bash
# Wyczyść Docker cache
docker system prune -a --volumes

# Przebuduj
docker-compose build --no-cache user-service

# Uruchom
docker-compose up user-service
```

### Opcja 2: Szybka przebudowa (mniej agresywna)

```bash
# Przebuduj bez cache
docker-compose build --no-cache

# Uruchom
docker-compose up
```

### Opcja 3: Wyczyść tylko specific service cache

```bash
docker-compose build --no-cache user-service
docker-compose up
```

---

## ✅ Potwierdzenie Napraw

| Plik | Status |
|------|--------|
| JwtTokenProvider.java | ✅ Naprawiony (parser() API) |
| SecurityConfig.java | ✅ Naprawiony (lambda expressions) |
| JwtAuthenticationFilter.java | ✅ Poprawiony import Cookie |

---

## 🚀 Kolejne Kroki

1. Wyczyść Docker cache: `docker system prune -a --volumes`
2. Przebuduj: `docker-compose build --no-cache`
3. Uruchom: `docker-compose up`
4. Kompilacja powinna przejść ✅

---

**Status:** ✅ Kod jest już naprawiony
**Wymagane:** Wyczyszczenie Docker cache

