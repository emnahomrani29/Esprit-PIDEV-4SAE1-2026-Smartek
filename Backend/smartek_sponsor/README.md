# Smartek Sponsor Service - CI/CD Pipeline

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Pipeline CI/CD](#pipeline-cicd)
- [Déploiement](#déploiement)
- [Monitoring](#monitoring)
- [Maintenance](#maintenance)

## 🎯 Vue d'ensemble

Le service **Smartek Sponsor** est une application Spring Boot avec un pipeline CI/CD complet utilisant :

- **Jenkins** : Orchestration du pipeline
- **Docker** : Conteneurisation
- **Nexus** : Gestion des artefacts (Maven & Docker)
- **Kubernetes (kubeadm)** : Orchestration des conteneurs
- **SonarQube** : Analyse de qualité du code
- **Prometheus** : Collecte des métriques
- **Grafana** : Visualisation des métriques

## 🏗️ Architecture

```
┌─────────────┐
│   GitHub    │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌──────────────┐
│   Jenkins   │────▶│  SonarQube   │
└──────┬──────┘     └──────────────┘
       │
       ├──────────────┐
       ▼              ▼
┌─────────────┐  ┌──────────────┐
│    Maven    │  │    Docker    │
│   (Build)   │  │   (Image)    │
└──────┬──────┘  └──────┬───────┘
       │                │
       ▼                ▼
┌─────────────┐  ┌──────────────┐
│    Nexus    │  │    Nexus     │
│  (Maven)    │  │  (Docker)    │
└─────────────┘  └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │  Kubernetes  │
                 └──────┬───────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│  Prometheus  │ │   Pods   │ │   Grafana    │
└──────────────┘ └──────────┘ └──────────────┘
```

## 📦 Prérequis

### Serveurs requis

1. **Jenkins Server**
   - Jenkins 2.400+
   - Plugins : Pipeline, Docker, Kubernetes CLI, SonarQube Scanner
   - Maven 3.9.6
   - JDK 17

2. **Nexus Repository Manager**
   - Nexus OSS 3.x
   - Maven Repository (Hosted)
   - Docker Registry (Hosted)

3. **SonarQube Server**
   - SonarQube 9.x+
   - Java 17

4. **Kubernetes Cluster**
   - Kubernetes 1.27+
   - kubeadm
   - kubectl
   - Helm 3.x

5. **Monitoring Stack**
   - Prometheus Operator
   - Grafana

### Outils locaux

```bash
# Installer kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Installer Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Installer Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Installer Maven
sudo apt update
sudo apt install maven -y

# Installer Trivy (Security Scanner)
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo apt-key add -
echo "deb https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
sudo apt update
sudo apt install trivy -y
```

## 🚀 Installation

### 1. Configuration Nexus

#### Maven Repository

```bash
# Créer un repository Maven (hosted)
# URL: http://your-nexus-server:8081/repository/maven-releases/
```

#### Docker Registry

```bash
# Créer un Docker registry (hosted)
# Port: 8083
# URL: your-nexus-registry:8083

# Configurer Docker pour utiliser Nexus
sudo nano /etc/docker/daemon.json
```

```json
{
  "insecure-registries": ["your-nexus-registry:8083"]
}
```

```bash
sudo systemctl restart docker

# Login to Nexus Docker Registry
docker login your-nexus-registry:8083
```

### 2. Configuration SonarQube

```bash
# Créer un projet dans SonarQube
# Project Key: smartek-sponsor
# Générer un token d'authentification
```

### 3. Configuration Jenkins

```bash
# Exécuter le script de setup
cd Backend/smartek_sponsor/scripts
chmod +x setup-jenkins.sh
./setup-jenkins.sh
```

#### Credentials à créer dans Jenkins

1. **nexus-credentials**
   - Type: Username with password
   - ID: nexus-credentials
   - Username: admin
   - Password: <nexus-password>

2. **nexus-docker-credentials**
   - Type: Username with password
   - ID: nexus-docker-credentials
   - Username: admin
   - Password: <nexus-password>

3. **sonarqube-token**
   - Type: Secret text
   - ID: sonarqube-token
   - Secret: <sonarqube-token>

4. **kubeconfig-credentials**
   - Type: Secret file
   - ID: kubeconfig-credentials
   - File: ~/.kube/config

### 4. Configuration Kubernetes

```bash
# Créer le namespace
kubectl create namespace smartek-production

# Créer le secret pour Nexus Docker Registry
kubectl create secret docker-registry nexus-registry-secret \
  --docker-server=your-nexus-registry:8083 \
  --docker-username=admin \
  --docker-password=<password> \
  --namespace=smartek-production

# Mettre à jour les secrets
kubectl edit secret smartek-sponsor-secret -n smartek-production
```

### 5. Configuration Monitoring

```bash
# Installer Prometheus & Grafana
cd Backend/smartek_sponsor/scripts
chmod +x setup-monitoring.sh
./setup-monitoring.sh
```

## 🔄 Pipeline CI/CD

### Étapes du Pipeline

1. **Checkout** : Récupération du code source
2. **Build** : Compilation avec Maven
3. **Unit Tests** : Exécution des tests unitaires
4. **SonarQube Analysis** : Analyse de qualité du code
5. **Quality Gate** : Vérification des seuils de qualité
6. **Package** : Création du JAR
7. **Publish to Nexus** : Publication de l'artefact Maven
8. **Build Docker Image** : Construction de l'image Docker
9. **Security Scan** : Scan de sécurité avec Trivy
10. **Push to Registry** : Publication de l'image Docker
11. **Deploy to Kubernetes** : Déploiement sur K8s
12. **Health Check** : Vérification de la santé de l'application

### Déclenchement du Pipeline

```bash
# Le pipeline se déclenche automatiquement sur :
# - Push sur la branche main
# - Pull Request
# - Manuellement depuis Jenkins
```

### Variables d'environnement à configurer

Modifier le `Jenkinsfile` :

```groovy
DOCKER_REGISTRY = 'your-nexus-registry:8083'
NEXUS_URL = 'http://your-nexus-server:8081'
SONAR_HOST_URL = 'http://your-sonarqube-server:9000'
```

## 📊 Monitoring

### Prometheus

```bash
# Port-forward Prometheus
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090

# Accéder à Prometheus
http://localhost:9090
```

### Grafana

```bash
# Port-forward Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Accéder à Grafana
http://localhost:3000

# Credentials
Username: admin
Password: (voir script setup-monitoring.sh)
```

### Métriques disponibles

- **Request Rate** : Taux de requêtes par seconde
- **Response Time** : Temps de réponse (95e percentile)
- **Error Rate** : Taux d'erreurs 4xx/5xx
- **CPU Usage** : Utilisation CPU des pods
- **Memory Usage** : Utilisation mémoire des pods
- **JVM Metrics** : Heap, GC, Threads
- **Database Connection Pool** : Connexions actives/idle

### Alertes configurées

- High Error Rate (> 5%)
- High Response Time (> 2s)
- Pod Down (< 2 pods)
- High Memory Usage (> 90%)
- High CPU Usage (> 80%)
- Database Connection Pool Exhausted (> 90%)
- Frequent Restarts

## 🚀 Déploiement

### Déploiement manuel

```bash
# Déployer sur Kubernetes
cd Backend/smartek_sponsor/scripts
chmod +x deploy.sh
./deploy.sh
```

### Déploiement via Jenkins

1. Aller sur Jenkins
2. Sélectionner le job `smartek-sponsor-pipeline`
3. Cliquer sur "Build Now"

### Vérification du déploiement

```bash
# Vérifier les pods
kubectl get pods -n smartek-production -l app=smartek-sponsor

# Vérifier les logs
kubectl logs -f -n smartek-production -l app=smartek-sponsor

# Vérifier la santé
kubectl exec -n smartek-production -it $(kubectl get pod -n smartek-production -l app=smartek-sponsor -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:8080/actuator/health
```

### Rollback

```bash
# Rollback vers la version précédente
cd Backend/smartek_sponsor/scripts
chmod +x rollback.sh
./rollback.sh
```

## 🧪 Tests locaux

### Avec Docker Compose

```bash
# Démarrer l'environnement local
cd Backend/smartek_sponsor
docker-compose up -d

# Vérifier les logs
docker-compose logs -f smartek-sponsor

# Accéder à l'application
http://localhost:8080

# Accéder à Prometheus
http://localhost:9090

# Accéder à Grafana
http://localhost:3000

# Arrêter l'environnement
docker-compose down
```

### Build local

```bash
# Build avec Maven
cd Backend/smartek_sponsor
mvn clean package

# Build Docker image
docker build -t smartek-sponsor:local .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/smartek_sponsor \
  smartek-sponsor:local
```

## 🔧 Maintenance

### Mise à jour de l'application

```bash
# 1. Modifier le code
# 2. Commit et push
git add .
git commit -m "Update feature"
git push origin main

# 3. Le pipeline Jenkins se déclenche automatiquement
```

### Scaling

```bash
# Scale manuellement
kubectl scale deployment smartek-sponsor-deployment --replicas=5 -n smartek-production

# Le HPA scale automatiquement entre 3 et 10 replicas
```

### Logs

```bash
# Logs en temps réel
kubectl logs -f -n smartek-production -l app=smartek-sponsor

# Logs d'un pod spécifique
kubectl logs -n smartek-production <pod-name>

# Logs des 100 dernières lignes
kubectl logs --tail=100 -n smartek-production -l app=smartek-sponsor
```

### Debugging

```bash
# Accéder au shell d'un pod
kubectl exec -it -n smartek-production <pod-name> -- /bin/sh

# Port-forward pour accès local
kubectl port-forward -n smartek-production <pod-name> 8080:8080

# Décrire un pod
kubectl describe pod -n smartek-production <pod-name>
```

## 📝 Configuration des secrets

### Production

**IMPORTANT** : Avant le déploiement en production, mettez à jour les secrets :

```bash
# Éditer le secret
kubectl edit secret smartek-sponsor-secret -n smartek-production

# Ou recréer le secret
kubectl delete secret smartek-sponsor-secret -n smartek-production
kubectl create secret generic smartek-sponsor-secret \
  --from-literal=SPRING_DATASOURCE_USERNAME=prod_user \
  --from-literal=SPRING_DATASOURCE_PASSWORD=secure_password \
  --from-literal=SPRING_MAIL_USERNAME=noreply@smartek.com \
  --from-literal=SPRING_MAIL_PASSWORD=secure_mail_password \
  -n smartek-production
```

## 🔒 Sécurité

### Bonnes pratiques

1. **Secrets** : Ne jamais commiter de secrets dans Git
2. **Images** : Scanner les images avec Trivy
3. **RBAC** : Utiliser les rôles Kubernetes appropriés
4. **Network Policies** : Limiter le trafic réseau
5. **Pod Security** : Exécuter en tant que non-root
6. **TLS** : Utiliser HTTPS pour tous les endpoints

### Scan de sécurité

```bash
# Scanner l'image Docker
trivy image your-nexus-registry:8083/smartek-sponsor:latest

# Scanner le code source
trivy fs Backend/smartek_sponsor
```

## 📚 Ressources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

## 🤝 Support

Pour toute question ou problème :
- Email : team@smartek.com
- Slack : #smartek-sponsor

## 📄 License

Copyright © 2024 Smartek. All rights reserved.
