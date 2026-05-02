# SMARTEK — offers-service · Guide DevOps

Pipeline CI/CD complet : **Jenkins · Docker · Nexus · kubeadm/Ubuntu · Prometheus · Grafana · SonarQube**

---

## Architecture du pipeline

```
Code Push
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  JENKINS (CI/CD)                                                │
│                                                                 │
│  1. Checkout          → git clone / fetch                       │
│  2. Detect Changes    → git diff HEAD~1 (skip si pas de modif)  │
│  3. Build             → mvn clean package -DskipTests           │
│  4. Tests unitaires   → mvn verify + JaCoCo (seuil 60%)         │
│  5. SonarQube         → analyse qualité + Quality Gate          │
│  6. Publish JAR       → Nexus Maven Repository (main/develop)   │
│  7. Docker Build      → image multi-stage (JRE Alpine)          │
│  8. Docker Push       → Nexus Registry nexus:8082               │
│  9. Deploy K8s        → kubectl set image + rollout             │
│  10. Health Check     → /actuator/health + /actuator/prometheus │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  KUBERNETES (kubeadm / Ubuntu)                                  │
│  Namespace : smartek                                            │
│                                                                 │
│  Deployment  → 2 replicas, RollingUpdate (zéro downtime)        │
│  Service     → ClusterIP:8085                                   │
│  HPA         → 2–6 pods (CPU 70% / Mémoire 80%)                 │
│  Ingress     → NGINX → api.smartek.local/api/offers             │
│  ServiceMonitor → Prometheus Operator scrape /actuator/prometheus│
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  OBSERVABILITÉ                                                  │
│                                                                 │
│  Prometheus  → scrape toutes les 15s via ServiceMonitor         │
│  Grafana     → dashboard "SMARTEK — Offers Service"             │
│               (requêtes/s, latence P95, heap JVM, CPU, HikariCP)│
│  Alertes     → ServiceDown, HighErrorRate, HighLatency,         │
│               HighHeapUsage, DBPoolExhausted                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prérequis Jenkins

### Plugins requis
| Plugin | Usage |
|--------|-------|
| `Pipeline` | Jenkinsfile déclaratif |
| `Git` | Checkout du code |
| `JUnit` | Publication des rapports de tests |
| `JaCoCo` | Couverture de code |
| `SonarQube Scanner` | Analyse qualité |
| `Kubernetes CLI` | `withKubeConfig` |
| `Docker Pipeline` | Build/push Docker |
| `AnsiColor` | Logs colorés |
| `Timestamper` | Horodatage des logs |
| `Build Discarder` | Rotation des builds |

### Credentials Jenkins à configurer
| ID | Type | Description |
|----|------|-------------|
| `nexus-credentials` | Username/Password | Accès Nexus (JAR + Docker) |
| `kubeconfig-smartek` | Secret file | kubeconfig du cluster |

### Configuration SonarQube
Dans **Manage Jenkins → Configure System → SonarQube servers** :
- Name : `SonarQube`
- URL  : `http://sonarqube:9000`
- Token : (créer dans SonarQube → My Account → Security)

---

## Nexus Repository Manager

### Dépôts à créer
| Nom | Type | Usage |
|-----|------|-------|
| `smartek-releases` | maven2 (hosted) | JARs de production |
| `smartek-snapshots` | maven2 (hosted) | JARs de développement |
| `smartek-docker` | docker (hosted) | Images Docker (port 8082) |

### Secret Kubernetes pour le pull d'images
```bash
kubectl create secret docker-registry nexus-registry-secret \
  --docker-server=nexus:8082 \
  --docker-username=<user> \
  --docker-password=<pass> \
  --docker-email=devops@smartek.com \
  -n smartek
```

---

## Kubernetes (kubeadm / Ubuntu)

### Déploiement initial
```bash
# 1. Namespace
kubectl apply -f k8s/namespace.yml

# 2. Secrets (adapter les valeurs base64 avant d'appliquer)
kubectl apply -f k8s/secret.yml

# 3. ConfigMap
kubectl apply -f k8s/configmap.yml

# 4. Deployment + Service + HPA
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/hpa.yml

# 5. Ingress (si NGINX Ingress Controller installé)
kubectl apply -f k8s/ingress.yml

# 6. ServiceMonitor (si kube-prometheus-stack installé)
kubectl apply -f k8s/servicemonitor.yml
```

### Commandes utiles
```bash
# Statut du déploiement
kubectl get pods -n smartek -l app=offers-service

# Logs en temps réel
kubectl logs -f deployment/offers-service -n smartek

# Rollback manuel
kubectl rollout undo deployment/offers-service -n smartek

# Historique des déploiements
kubectl rollout history deployment/offers-service -n smartek

# Scaling manuel
kubectl scale deployment/offers-service --replicas=3 -n smartek
```

---

## Prometheus & Grafana

### Installation (kube-prometheus-stack)
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.adminPassword=smartek123
```

### Import du dashboard Grafana
1. Ouvrir Grafana → **Dashboards → Import**
2. Uploader `monitoring/grafana-dashboard.json`
3. Sélectionner la source de données Prometheus
4. Cliquer **Import**

### Métriques exposées par le service
| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Liveness + Readiness probes |
| `/actuator/health/liveness` | Probe Kubernetes liveness |
| `/actuator/health/readiness` | Probe Kubernetes readiness |
| `/actuator/prometheus` | Métriques Micrometer (scrape Prometheus) |
| `/actuator/metrics` | Métriques JSON |
| `/actuator/info` | Informations de build |

### Alertes configurées
| Alerte | Seuil | Sévérité |
|--------|-------|----------|
| `OffersServiceDown` | pod down > 1 min | critical |
| `OffersServiceHighErrorRate` | > 5% erreurs 5xx | warning |
| `OffersServiceHighLatency` | P95 > 2s | warning |
| `OffersServiceHighHeapUsage` | heap > 85% | warning |
| `OffersServiceHighCpuUsage` | CPU > 80% | warning |
| `OffersServiceDBPoolExhausted` | HikariCP > 90% | critical |

---

## SonarQube

### Quality Gate (seuils minimaux)
| Métrique | Seuil |
|----------|-------|
| Couverture de code | ≥ 60% |
| Duplications | ≤ 3% |
| Bugs | 0 (nouveaux) |
| Vulnérabilités | 0 (nouveaux) |
| Code Smells | ≤ 10 (nouveaux) |

### Analyse locale
```bash
# Depuis Backend/offers-service/
mvn sonar:sonar \
  -Dsonar.projectKey=offers-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<token>
```

---

## Variables d'environnement

| Variable | Défaut | Description |
|----------|--------|-------------|
| `SERVER_PORT` | `8085` | Port du service |
| `DB_HOST` | `localhost` | Hôte MySQL |
| `DB_PORT` | `3306` | Port MySQL |
| `DB_USER` | `root` | Utilisateur MySQL |
| `DB_PASS` | `` | Mot de passe MySQL |
| `JWT_SECRET` | (valeur par défaut) | Clé secrète JWT |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | URL Eureka |
| `LOG_LEVEL` | `INFO` | Niveau de log |
| `SPRING_PROFILES_ACTIVE` | `default` | Profil Spring |

---

## Structure des fichiers DevOps

```
Backend/offers-service/
├── Jenkinsfile                    # Pipeline Jenkins (11 stages)
├── Dockerfile                     # Multi-stage build (Maven → JRE Alpine)
├── sonar-project.properties       # Configuration SonarQube
├── k8s/
│   ├── namespace.yml              # Namespace smartek
│   ├── configmap.yml              # Variables d'environnement
│   ├── secret.yml                 # Secrets (DB, JWT)
│   ├── deployment.yml             # Deployment (2 replicas, probes, HPA)
│   ├── service.yml                # Service ClusterIP:8085
│   ├── hpa.yml                    # Autoscaling (2–6 pods)
│   ├── ingress.yml                # Ingress NGINX
│   └── servicemonitor.yml         # Prometheus Operator scrape
└── monitoring/
    ├── prometheus-rules.yml       # Alertes Prometheus
    └── grafana-dashboard.json     # Dashboard Grafana
```
