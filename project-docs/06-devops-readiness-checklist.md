# DevOps Readiness Checklist (Pre-Sprint-3)

> **Goal:** Identify what is already in place vs. what needs to be built to satisfy the Sprint 3 DevOps evaluation grid (`Grille_Evaluation_DevOps_Sprint 3.pdf`).

## Legend
- ✅ Done / Partially done
- ⚠️ Partial / Needs improvement
- ❌ Missing

---

## 1. Containerization

| Item | Status | Notes |
|------|--------|-------|
| All backend services have Dockerfiles | ⚠️ | `learning` & `skiil-evidence-service` have simpler non-build Dockerfiles. `certification-badge-service` may lack one. |
| Frontend has Dockerfile | ✅ | Multi-stage Node → Nginx. |
| Docker Compose orchestrates full stack | ✅ | 12 services defined with health checks. |
| Base images are pinned / secure | ⚠️ | Uses `eclipse-temurin:17-jre-alpine`, `mysql:8.0`, `nginx:alpine` — acceptable but should scan regularly. |
| Image size optimization | ⚠️ | JRE alpine is good; could add `--no-transfer-progress` to Maven. |

---

## 2. CI / Continuous Integration

| Item | Status | Notes |
|------|--------|-------|
| GitHub Actions configured | ✅ | Two workflow files exist. |
| Unit tests run on push/PR | ✅ | For `offers-service`, `learning`, `skiil-evidence-service`. |
| Code coverage (JaCoCo) | ✅ | Only in `offers-service` workflow. |
| Static analysis (SonarQube) | ⚠️ | Only in `offers-service` workflow; may be skipped if token missing. |
| Artifact publishing (Nexus) | ✅ | Only for `offers-service`. |
| Frontend CI (build, lint, test) | ❌ | No Angular workflow exists. |
| Maven wrapper consistency | ❌ | Not all modules have `mvnw`. |

---

## 3. CD / Continuous Deployment

| Item | Status | Notes |
|------|--------|-------|
| Docker image build & push | ✅ | `offers-service` pushes to Nexus Registry. |
| Kubernetes deployment | ✅ | `offers-service` has a `kubectl set image` step. |
| Rollout status check | ✅ | `kubectl rollout status` with 120s timeout. |
| Post-deploy health probe | ⚠️ | Only for `offers-service`; `continue-on-error: true` weakens it. |
| Environment separation (dev / staging / prod) | ❌ | `main` and `develop` trigger the same deploy logic. |
| Blue-green or canary strategy | ❌ | Not implemented. |

---

## 4. Monitoring & Observability

| Item | Status | Notes |
|------|--------|-------|
| Spring Boot Actuator | ✅ | Present in all standard services. |
| Prometheus metrics endpoint | ⚠️ | Actuator is there, but `micrometer-registry-prometheus` dependency should be verified in each POM. |
| Grafana dashboards | ❌ | Not present in repo. |
| Centralized logging (ELK / Loki) | ❌ | Not present. |
| Distributed tracing (Zipkin / Jaeger) | ❌ | Not present. |

---

## 5. Security

| Item | Status | Notes |
|------|--------|-------|
| JWT-based auth | ✅ | Implemented in `auth-service` and API Gateway filter. |
| HTTPS / TLS | ❌ | Not configured in Docker Compose or Nginx. |
| Secrets management | ⚠️ | GitHub Secrets used, but no external vault (HashiCorp Vault, Sealed Secrets). |
| Dependency vulnerability scanning | ❌ | No OWASP Dependency-Check or Trivy in CI. |
| Container image scanning | ❌ | No image scan before push. |

---

## 6. Documentation & Scripts

| Item | Status | Notes |
|------|--------|-------|
| Service-specific READMEs | ✅ | `auth-service`, `offers-service` have detailed READMEs. |
| Troubleshooting guide | ✅ | `offers-service/TROUBLESHOOTING.md` exists. |
| Root-level project docs | ✅ | You are reading them (`project-docs/`). |
| Automated start/stop scripts | ⚠️ | `start-all.bat` / `stop-all.bat` referenced in root `package.json` but not documented here. |

---

## 7. Database & Persistence

| Item | Status | Notes |
|------|--------|-------|
| Schema initialization script | ❌ | `init-db.sql` is missing. |
| Migration tool (Flyway / Liquibase) | ❌ | Not used. |
| Dedicated DB per service | ✅ | Each microservice has its own MySQL database. |

---

## Recommended Next Steps (for Sprint 3)

1. **Unify CI:** Create a reusable workflow template so every service gets build, test, SonarQube, and Docker push.
2. **Add Frontend CI:** Build Angular, run unit tests (`ng test --watch=false --browsers=ChromeHeadless`), and build/push the Docker image.
3. **Security Hardening:** Add Trivy container scan and OWASP dependency-check to the pipeline.
4. **Monitoring:** Add `micrometer-registry-prometheus` to all services and provide a `docker-compose.monitoring.yml` with Prometheus + Grafana.
5. **Fix Tech Debt:** Resolve port collision (`learning` vs `offers-service`), add missing modules to parent POM, and create `init-db.sql` or adopt Flyway.
6. **Environment Promotion:** Split deploy job into `deploy-staging` (on `develop`) and `deploy-prod` (on `main`).
