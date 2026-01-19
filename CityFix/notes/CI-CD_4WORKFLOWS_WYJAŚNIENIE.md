# GitHub Actions Workflows - Wyjaśnienie 4 Plików

## Dlaczego są 4 pliki workflow'ów?

Są **4 NIEZALEŻNE workflow'i**, każdy robi coś innego:

```
.github/workflows/
├── ci-cd.yml              ← GŁÓWNY PIPELINE (testy, build, deploy)
├── codeql-analysis.yml    ← SECURITY SCANNING (szybsza wersja)
├── release.yml            ← AUTOMATYCZNE RELEASE'I (release notes + build)
└── pr-checks.yml          ← WALIDACJA PR (testy + security + build)
```

---

## 1️⃣ ci-cd.yml - GŁÓWNY PIPELINE

### Za co odpowiada?

```yaml
on:
  push:
    branches: [ main, develop ]
    tags: [ 'v*' ]
  pull_request:
    branches: [ main, develop ]
```

**Uruchamia się przy:**
- ✅ Push do `main`
- ✅ Push do `develop`
- ✅ Push tagu `v1.0.0`
- ✅ Pull Request do `main` lub `develop`

**Robi:**
```
1. Test (user-service, report-service, log-service)
   ↓
2. CodeQL Security Analysis
   ↓
3. Build Docker images (TYLKO jeśli push do main/tag)
   ↓
4. docker-compose up (TYLKO jeśli push do main)
```

**Czas:** ~20-25 minut

**Output:**
- ✅ Test reports jako artifacts
- ✅ Security scan results
- ✅ Docker images (zbudowane lokalnie)

---

## 2️⃣ codeql-analysis.yml - ZAAWANSOWANY SECURITY SCAN

### Za co odpowiada?

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * 1'  ← CO PONIEDZIAŁEK O 2:00!
```

**Uruchamia się przy:**
- ✅ Push do `main` lub `develop`
- ✅ Pull Request do `main`
- ✅ Co poniedziałek automatycznie (3 am UTC)

**Robi:**
```
Analiza każdego serwisu OSOBNO:
- user-service
- report-service
- log-service
- gateway

Dla każdego:
1. Setup Java
2. Build projekt
3. CodeQL analysis (ML + SAST)
4. Upload SARIF results
```

**Czas:** ~30-40 minut (bo analizuje wszystko bardziej dogłębnie)

**Output:**
- 🔒 Detailed security scan
- 📊 SARIF reports (kompatybilne z GitHub Security)
- 🎯 Per-service analysis

**Różnica od ci-cd.yml:**
- Bardziej szczegółowa analiza
- Analizuje każdy serwis OSOBNO (nie razem)
- Scheduled runs (periodyczne skanowanie)

---

## 3️⃣ release.yml - AUTOMATYCZNE RELEASE'I

### Za co odpowiada?

```yaml
on:
  push:
    tags:
      - 'v*.*.*'  ← TYLKO tagi w formacie v1.0.0!
```

**Uruchamia się TYLKO przy:**
- ✅ Push tagu `v1.0.0`, `v2.3.4`, itd.

**Robi:**
```
1. Tworzy GitHub Release automatycznie
   - Pobiera poprzedni tag
   - Generuje changelog
   - Czyta commit messages między tagami

2. Buduje Docker images z wersją
   - cityfix-gateway:v1.0.0
   - cityfix-user-service:v1.0.0
   - itd.

3. Tworzy release notes:
   - 🚀 CityFix Release v1.0.0
   - Lista zmian (changelog)
   - Instrukcja deploymentu
```

**Czas:** ~15-20 minut

**Output:**
- 📝 GitHub Release (widoczna na stronie repozytorium)
- 📦 Docker images (named v1.0.0)
- 📋 Changelog (z commit messages)

**Przykład Release Notes:**
```
## 🚀 CityFix Release v1.0.0

### Changes
- feat: add monitoring with Prometheus and Grafana
- fix: resolve RabbitMQ authentication issue
- docs: update deployment guide

### Docker Images
- ghcr.io/user/cityfix-gateway:v1.0.0
- ghcr.io/user/cityfix-user-service:v1.0.0
- ...

### Deployment
```bash
export VERSION=v1.0.0
docker-compose pull
docker-compose up -d
```
```

---

## 4️⃣ pr-checks.yml - WALIDACJA PULL REQUESTÓW

### Za co odpowiada?

```yaml
on:
  pull_request:
    branches: [ main, develop ]
    types: [opened, synchronize, reopened]
```

**Uruchamia się TYLKO na:**
- ✅ Stworzenie PR
- ✅ Push nowych commitów do PR
- ✅ Reopening PR

**Robi:**
```
1. Validate PR title
   - Czeka conventional commits format
   - Np: "feat: add new feature"

2. Run tests
   - Parallel testy 3 serwisów
   - Upload coverage

3. Build Docker images
   - Tylko lokalne (do testowania)
   - Nie pushuje nigdzie

4. Security scan (Trivy)
   - Skanuje vulnerabilities

5. Comment PR
   - Dodaje komentarz z wynikami
   - ✅ lub ❌ status
```

**Czas:** ~15-20 minut

**Output:**
- ✅ GitHub PR checks status
- 💬 Automatyczny komentarz z wynikami
- 🔒 Security alerts

---

## Podsumowanie - CO ROBI KAŻDY WORKFLOW

| Workflow | Trigger | Testy | CodeQL | Build | Deploy | Czas |
|----------|---------|-------|--------|-------|--------|------|
| **ci-cd.yml** | push main/dev/tag, PR | ✅ | ✅ | ✅ (main/tag) | ✅ (main) | 20-25 min |
| **codeql-analysis.yml** | push main/dev, PR, weekly | ❌ | ✅✅✅ (detailed) | ❌ | ❌ | 30-40 min |
| **release.yml** | tag v* | ❌ | ❌ | ✅ | ❌ | 15-20 min |
| **pr-checks.yml** | PR | ✅ | ❌ | ✅ | ❌ | 15-20 min |

---

## PRAKTYCZNE SCENARIUSZE

### Scenariusz 1: Zwykły push do main

```bash
git push origin main
```

**Uruchamiają się:**
- ✅ ci-cd.yml (testy → build → deploy)
- ❌ codeql-analysis.yml (NIE - bo już jest w ci-cd)
- ❌ release.yml (NIE - to nie jest tag)
- ❌ pr-checks.yml (NIE - to nie jest PR)

**Wynik:**
- Testy
- Build images
- docker-compose up na GitHub servers
- Health checks

---

### Scenariusz 2: Tworzenie release'a

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

**Uruchamiają się:**
- ✅ ci-cd.yml (testy → build, ale BEZ deploy bo to tag)
- ✅ release.yml (tworzy Release Notes, buduje images)
- ❌ codeql-analysis.yml (NIE - kod już przetestowany w ci-cd)
- ❌ pr-checks.yml (NIE - to nie jest PR)

**Wynik:**
- Testy przechodzą
- Docker images z tagiem v1.0.0
- GitHub Release z changelog
- Release Notes na stronie repozytorium

---

### Scenariusz 3: Pull Request

```bash
git push origin feature-branch
# Twórz PR na GitHub
```

**Uruchamiają się:**
- ✅ ci-cd.yml (testy + CodeQL, BEZ build/deploy)
- ✅ pr-checks.yml (validation + build + security)
- ❌ codeql-analysis.yml (NIE - już jest w ci-cd)
- ❌ release.yml (NIE - to nie jest tag)

**Wynik:**
- Testy przechodzą
- Security scan OK
- Komentarz w PR
- Merge button unlocked (jeśli wszystko OK)

---

## WERSJE OBRAZÓW DOCKER

### Skąd pochodzą wersje?

```yaml
# W ci-cd.yml:
Extract version from git:
  if [[ $GITHUB_REF == refs/tags/* ]]; then
    VERSION=${GITHUB_REF#refs/tags/}      ← v1.0.0 z tagu
  else
    VERSION=${GITHUB_REF#refs/heads/}-${GITHUB_SHA::8}  ← main-abc1234
  fi
```

**Dla push do main:**
```
VERSION=main-abc1234

Obrazy:
- cityfix-gateway:main-abc1234
- cityfix-user-service:main-abc1234
- cityfix-report-service:main-abc1234
- cityfix-log-service:main-abc1234
```

**Dla tagu v1.0.0:**
```
VERSION=v1.0.0

Obrazy:
- cityfix-gateway:v1.0.0
- cityfix-user-service:v1.0.0
- cityfix-report-service:v1.0.0
- cityfix-log-service:v1.0.0
```

**Dla PR:**
```
Obrazy SĄ BUDOWANE (do testowania)
Ale tylko lokalnie, nie pushują się nigdzie!
```

---

## CZY MOGĘ MNIEJ WORKFLOW'ÓW?

**TAK!** Możesz:**

### OPCJA 1: Tylko ci-cd.yml (rekomendowana dla początkujących)
- Usunąć `codeql-analysis.yml`
- Usunąć `release.yml`
- Usunąć `pr-checks.yml`
- Zostanie tylko główny pipeline

### OPCJA 2: Minimalna konfiguracja
```
TRZYMAĆ:
- ci-cd.yml (testy + build + deploy)
- release.yml (automatyczne release'i)

USUNĄĆ:
- codeql-analysis.yml (duplikuje CodeQL z ci-cd)
- pr-checks.yml (duplikuje testy z ci-cd)
```

### OPCJA 3: Maksymalna bezpieczeństwo (obecna)
```
- ci-cd.yml (szybki pipeline)
- codeql-analysis.yml (zaawansowana security)
- release.yml (automatyczne releases)
- pr-checks.yml (walidacja PR)
```

---

## NAJLEPSZA KONFIGURACJA

**Moja rekomendacja (dla Ciebie):**

Usunąć `codeql-analysis.yml` i `pr-checks.yml`, bo:
- ❌ Duplikują logikę z `ci-cd.yml`
- ❌ Robią się 4 razy dłużej
- ❌ Zbyt skomplikowane na początek

**Pozostać z:**
1. **ci-cd.yml** - główny pipeline (zawiera CodeQL!)
2. **release.yml** - automatyczne release'i

**Całkowity czas: 20-25 minut zamiast 50+!**

---

## PODSUMOWANIE

| Plik | Kiedy | Po co | Czasu |
|------|-------|-------|-------|
| ci-cd.yml | push main/dev/tag | Build + test + deploy | 20-25 min |
| release.yml | tag v* | Release notes + images | 15-20 min |
| codeql-analysis.yml | push/PR/weekly | Detailed security | 30-40 min ⚠️ |
| pr-checks.yml | PR | PR validation | 15-20 min ⚠️ |

⚠️ = Opcjonalne, mogą zostać usunięte

---

**Chcesz że ja uprościę strukturę workflow'ów?** 🚀

