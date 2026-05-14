# CI/CD Workflows

## GitHub Actions Location
`.github/workflows/`

---

## 1. `tests.yml` – Skill-Evidence & Learning Tests

**Triggers:**
- Push to `skill-evidence-learning-service`
- PR to `skill-evidence-learning-service`

**Jobs:**

### `test-skill-evidence`
- **Runner:** `ubuntu-latest`
- **JDK:** 17 (Temurin)
- **Working dir:** `Backend/skiil-evidence-service`
- **Command:** `./mvnw test -B`

### `test-learning`
- **Runner:** `ubuntu-latest`
- **JDK:** 17 (Temurin)
- **Working dir:** `Backend/learning`
- **Command:** `./mvnw test -B`

---

## 2. `offers-service-tests.yml` – Offers Service CI/CD

**Triggers:**
- Push to `main`, `develop`, `offers-service` (only if `Backend/offers-service/**` or workflow file changed)
- PR to `main`, `develop` (same path filter)

**Environment Variables:**
```yaml
SERVICE_DIR: Backend/offers-service
IMAGE_NAME:  smartek/offers-service
NEXUS_REPO:  nexus:8082
```

### Stage 1 – `detect-changes`
- Uses `git diff HEAD~1 HEAD` to see if `Backend/offers-service/` was modified.
- Outputs `service_changed: true/false`.

### Stage 2 – `build-and-test`
- **Needs:** `detect-changes`
- **Condition:** changes detected OR branch is `main`/`develop`
- **Steps:**
  1. Checkout (full history for SonarQube)
  2. Set up JDK 17
  3. `mvn clean package -DskipTests -B`
  4. `mvn verify -Dspring.profiles.active=test -B` *(unit tests + JaCoCo coverage)*
  5. **SonarQube analysis** (skips gracefully if `SONAR_TOKEN` is missing)
     - Coverage report path: `target/site/jacoco/jacoco.xml`
  6. **Upload artifacts:**
     - `offers-service-surefire-reports`
     - `offers-service-jacoco-report`
     - Retention: 7 days

### Stage 3 – `publish-artifact`
- **Needs:** `build-and-test`
- **Condition:** branch is `main` or `develop`
- **Steps:**
  - `mvn deploy -DskipTests` to Nexus repository `smartek-releases`
  - Uses secrets: `NEXUS_USER`, `NEXUS_PASS`, `NEXUS_URL`

### Stage 4 – `build-and-push-docker`
- **Needs:** `publish-artifact`
- **Condition:** branch is `main` or `develop`
- **Steps:**
  1. Docker Buildx setup
  2. Login to Nexus Docker Registry (`secrets.NEXUS_REGISTRY`)
  3. Build & push image with tags:
     - `<nexus-registry>/smartek/offers-service:<git-sha>`
     - `<nexus-registry>/smartek/offers-service:latest`
  4. Cache: `type=gha` (GitHub Actions cache)

### Stage 5 – `deploy`
- **Needs:** `build-and-push-docker`
- **Condition:** branch is `main` or `develop`
- **Steps:**
  1. Configure `kubectl` via `secrets.KUBECONFIG`
  2. `kubectl set image deployment/offers-service ... -n smartek`
  3. `kubectl rollout status deployment/offers-service -n smartek --timeout=120s`
  4. **Post-deploy probe:** curl `offers-service.smartek.svc.cluster.local:8085/actuator/health`

---

## Secrets Required (Repository Level)

| Secret | Used By |
|--------|---------|
| `SONAR_TOKEN` | SonarQube analysis |
| `SONAR_HOST_URL` | SonarQube analysis |
| `NEXUS_USER` | Artifact publish, Docker login |
| `NEXUS_PASS` | Artifact publish, Docker login |
| `NEXUS_URL` | Maven deploy URL |
| `NEXUS_REGISTRY` | Docker push target |
| `KUBECONFIG` | Kubernetes deployment |

---

## Current Gaps / Opportunities for Sprint 3

1. **No unified CI for all services:** Only `offers-service`, `learning`, and `skiil-evidence-service` have dedicated workflows.
2. **No frontend CI:** Angular build, lint, unit tests (Karma) are not automated.
3. **No integration tests:** Workflows stop at unit-test level; no service-to-service or contract testing.
4. **No security scanning:** No Trivy, Snyk, or OWASP dependency-check steps.
5. **No notification step:** No Slack / Teams / email alert on failure.
6. **Environment promotion:** `main` and `develop` share the same deploy logic; no staging vs. production separation.
7. **Missing `mvnw` wrapper:** Some services rely on system Maven; the `tests.yml` uses `./mvnw` which may not exist everywhere.
