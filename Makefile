# =============================================================
#  SMARTEK - Makefile - Point d'entree unique
# =============================================================
.PHONY: help dev prod stop build test deploy logs clean backup health sonar

help:
	@echo "====================================================="
	@echo "           SMARTEK - Commandes disponibles"
	@echo "====================================================="
	@echo "  Developpement"
	@echo "    make dev          - Demarrer en mode developpement"
	@echo "    make stop         - Arreter tous les services"
	@echo "    make logs         - Voir les logs (tous)"
	@echo "    make logs s=auth-service - Logs d'un service"
	@echo ""
	@echo "  Build & Tests"
	@echo "    make build        - Builder toutes les images Docker"
	@echo "    make test         - Lancer tous les tests"
	@echo "    make test-back    - Tests backend uniquement"
	@echo "    make test-front   - Tests frontend uniquement"
	@echo ""
	@echo "  Production"
	@echo "    make prod         - Demarrer en mode production"
	@echo "    make deploy       - Deploiement ordonne complet"
	@echo "    make health       - Verifier la sante des services"
	@echo ""
	@echo "  Base de donnees"
	@echo "    make backup       - Sauvegarder les bases de donnees"
	@echo ""
	@echo "  Qualite"
	@echo "    make sonar        - Lancer l'analyse SonarQube"
	@echo "    make clean        - Nettoyer les artefacts de build"
	@echo "====================================================="

# ── Developpement ─────────────────────────────────────────────

dev:
	@echo "Starting development environment..."
	docker compose up -d
	@echo "Frontend:  http://localhost:4200"
	@echo "API:       http://localhost:8080"
	@echo "Eureka:    http://localhost:8761"
	@echo "Grafana:   http://localhost:3000"
	@echo "Prometheus:http://localhost:9090"

stop:
	docker compose down

restart:
	docker compose restart $(s)

logs:
	@bash scripts/logs.sh $(s)

# ── Build ──────────────────────────────────────────────────────

build:
	@bash scripts/docker-build-all.sh $(tag)

build-service:
	docker compose build $(s)

# ── Tests ──────────────────────────────────────────────────────

test:
	@bash scripts/run-tests.sh

test-back:
	@for service in auth-service event-service planning-service training-service offers-service course-service exam-service; do \
		echo "Testing $$service..."; \
		mvn clean test -f Backend/$$service/pom.xml; \
	done

test-front:
	cd Frontend/angular-app && npm run test -- --watch=false --browsers=ChromeHeadless

# ── Production ─────────────────────────────────────────────────

prod:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

deploy:
	@bash scripts/deploy.sh

health:
	@bash scripts/check-services.sh

# ── Base de donnees ────────────────────────────────────────────

backup:
	@bash scripts/backup.sh

# ── Qualite ────────────────────────────────────────────────────

sonar:
	@for service in auth-service event-service planning-service training-service offers-service course-service exam-service certification-badge-service; do \
		echo "Analyzing $$service..."; \
		mvn sonar:sonar -f Backend/$$service/pom.xml -Dsonar.token=$(SONAR_TOKEN); \
	done

lint-front:
	cd Frontend/angular-app && npm run lint

# ── Nettoyage ──────────────────────────────────────────────────

clean:
	@find Backend -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	@find Frontend -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
	docker system prune -f
	@echo "Cleaned up"

clean-volumes:
	@echo "WARNING: This will delete all data volumes!"
	docker compose down -v
