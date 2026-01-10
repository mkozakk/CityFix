# GitHub Actions CI/CD Pipeline

## Workflows

### 1. CI/CD Pipeline (`ci-cd.yml`)

Główny pipeline CI/CD uruchamiany przy każdym push'u do `main` i `develop`.

**Joby:**
- ✅ **Test** - Uruchamia testy jednostkowe dla wszystkich serwisów
- 🔒 **CodeQL Analysis** - Analiza bezpieczeństwa kodu (ML + SAST)
- 🐳 **Build and Push** - Buduje i pushuje obrazy Docker do GHCR
- 🚀 **Deploy** - Wdraża aplikację za pomocą docker-compose

**Triggery:**
- Push do `main` lub `develop`
- Push tagów `v*`
- Pull requesty do `main` lub `develop`

### 2. CodeQL Advanced Security Scanning (`codeql-analysis.yml`)

Rozszerzona analiza bezpieczeństwa kodu z użyciem GitHub CodeQL.

**Features:**
- 🔍 Machine Learning + SAST analysis
- 📊 Security-extended queries
- 🎯 Analiza każdego serwisu osobno
- 📅 Scheduled scans (co poniedziałek o 2:00)

**Wykrywane problemy:**
- SQL Injection
- XSS vulnerabilities
- Path traversal
- Command injection
- Authentication bypass
- Sensitive data exposure

### 3. Release (`release.yml`)

Automatyczne tworzenie release'ów i budowanie obrazów.

**Triggery:**
- Push tagów w formacie `v*.*.*` (np. `v1.0.0`)

**Proces:**
1. Utworzenie GitHub Release
2. Automatyczne generowanie changelog
3. Budowanie obrazów Docker z tagiem wersji
4. Tagowanie jako `latest`

### 4. Pull Request Checks (`pr-checks.yml`)

Walidacja pull requestów przed merge'em.

**Checks:**
- ✅ Walidacja tytułu PR (conventional commits)
- 🧪 Uruchomienie testów
- 🐳 Budowanie obrazów Docker
- 🔒 Security scan (Trivy)
- 💬 Automatyczny komentarz z podsumowaniem

## Wersjonowanie

### Semantyczne wersjonowanie (SemVer)

Używamy formatu: `v<major>.<minor>.<patch>`

**Przykłady:**
- `v1.0.0` - Major release
- `v1.1.0` - Minor release (nowe features)
- `v1.1.1` - Patch release (bugfixy)

### Tworzenie release'a

```bash
# 1. Tag lokalnie
git tag -a v1.0.0 -m "Release version 1.0.0"

# 2. Push taga
git push origin v1.0.0

# 3. GitHub Actions automatycznie:
#    - Utworzy release
#    - Zbuduje obrazy
#    - Oznakuje je jako v1.0.0 i latest
```

### Zmienne VERSION

**W docker-compose.yml:**
```yaml
image: ${REGISTRY:-ghcr.io}/${IMAGE_PREFIX:-cityfix}/user-service:${VERSION:-latest}
```

**W .env:**
```env
REGISTRY=ghcr.io
IMAGE_PREFIX=cityfix
VERSION=latest  # lub v1.0.0
```

**W GitHub Actions:**
```bash
# Automatycznie ustawiane na podstawie gita:
VERSION=v1.0.0           # dla tagów
VERSION=main-abc1234     # dla commitów na main
```

## Secrets Configuration

Dodaj następujące secrets w GitHub Settings → Secrets (opcjonalnie, dla wdrażania):

### Optional Secrets

```
POSTGRES_DB              # Nazwa bazy danych (dla deploy job)
POSTGRES_USER            # Użytkownik bazy danych (dla deploy job)
POSTGRES_PASSWORD        # Hasło bazy danych (dla deploy job)
RABBITMQ_USER           # Użytkownik RabbitMQ (dla deploy job)
RABBITMQ_PASS           # Hasło RabbitMQ (dla deploy job)
LOG_ACCESS_PASSWORD     # Hasło dostępu do logów (dla deploy job)
JWT_SECRET              # Secret dla JWT tokens (dla deploy job)
GRAFANA_USER            # Użytkownik Grafana (dla deploy job)
GRAFANA_PASSWORD        # Hasło Grafana (dla deploy job)
```

### Auto-generated Secrets

```
GITHUB_TOKEN            # Automatycznie dostępny w Actions
```

**Notatka:** Secrets są potrzebne tylko jeśli deployujesz do środowiska produkcyjnego. Dla lokalnej pracy wystarczy `.env` file.

## Docker Images

Obrazy są publikowane do GitHub Container Registry (GHCR):

```
ghcr.io/<username>/cityfix-gateway:latest
ghcr.io/<username>/cityfix-user-service:latest
ghcr.io/<username>/cityfix-report-service:latest
ghcr.io/<username>/cityfix-log-service:latest
```

### Image Tags

**Dla commitów:**
- `main-abc1234` - commit SHA na main
- `develop-xyz5678` - commit SHA na develop

**Dla tagów:**
- `v1.0.0` - pełna wersja
- `v1.0` - major.minor
- `v1` - tylko major
- `latest` - najnowsza wersja

## Deployment

### Manual Deployment

```bash
# 1. Ustaw wersję
export VERSION=v1.0.0

# 2. Pull obrazów
docker-compose pull

# 3. Uruchom
docker-compose up -d
```

### Automatic Deployment

Deployment jest automatyczny po merge'u do `main`:

1. Testy przechodzą ✅
2. CodeQL analysis OK ✅
3. Obrazy są budowane ✅
4. Deploy na środowisko ✅

## Test Reports

Po każdym uruchomieniu testów:
- 📊 Raporty w GitHub Actions Artifacts
- 📈 Coverage upload do Codecov (opcjonalnie)
- 🎯 Test results summary w PR comments

## Security Scanning

### CodeQL (ML + SAST)
- Analiza kodu źródłowego
- Machine learning detection
- Security patterns matching
- Scheduled weekly scans

### Trivy
- Container image scanning
- Dependency vulnerabilities
- License compliance
- Configuration issues

## Monitoring

### GitHub Actions Dashboard
- Status wszystkich workflow'ów
- Historia uruchomień
- Logi i artifacts

### Security Tab
- CodeQL alerts
- Dependabot alerts
- Secret scanning

## Best Practices

### Commit Messages
Używaj conventional commits:
```
feat: add new feature
fix: resolve bug
docs: update documentation
style: format code
refactor: restructure code
test: add tests
chore: update dependencies
```

### Pull Requests
- Tytuł w formacie conventional commits
- Opis zmian
- Linkowanie do issues
- Czekanie na checks przed merge'em

### Versioning
- Major: Breaking changes
- Minor: New features (backwards compatible)
- Patch: Bug fixes

## Troubleshooting

### Tests failing
```bash
# Run locally
cd user-service
./gradlew test --info
```

### Build failing
```bash
# Check Dockerfile
docker build -t test ./user-service
```

### CodeQL issues
- Przejrzyj Security → Code scanning alerts
- Fix zgłoszonych problemów
- Re-run workflow

### Deployment failing
- Sprawdź secrets configuration
- Zweryfikuj .env file
- Check docker-compose logs

## Status Badges

Dodaj do README.md:

```markdown
![CI/CD](https://github.com/<username>/CityFix/workflows/CI%2FCD%20Pipeline/badge.svg)
![CodeQL](https://github.com/<username>/CityFix/workflows/CodeQL/badge.svg)
![Release](https://github.com/<username>/CityFix/workflows/Release/badge.svg)
```

---

**Documentation:** [GitHub Actions Docs](https://docs.github.com/en/actions)  
**CodeQL:** [CodeQL Documentation](https://codeql.github.com/docs/)

