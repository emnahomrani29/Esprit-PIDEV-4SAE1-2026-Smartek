# 🚀 Quick Start Guide - Smartek Sponsor Service

## 📝 Résumé

Ce guide vous permet de démarrer rapidement avec le pipeline CI/CD du service Smartek Sponsor.

## ⚡ Démarrage rapide (5 minutes)

### 1. Prérequis

```bash
# Vérifier les outils installés
kubectl version --client
docker --version
mvn --version
helm version
```

### 2. Configuration initiale

```bash
# Cloner le repository
git clone <your-repo-url>
cd Backend/smartek_sponsor

# Rendre les scripts exécutables
chmod +x scripts/*.sh
```

### 3. Déploiement local avec Docker Compose

```bash
# Démarrer tous les services (App + MySQL + Prometheus + Grafana)
docker-compose up -d

# Vérifier les logs
docker-compose logs -f smartek-sponsor

# Accéder aux services
# Application: http://localhost:8080
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/admin)
```

### 4. Build local

```bash
# Build complet avec tests
./scripts/local-build.sh

# Ou manuellement
mvn clean package
docker build -t smartek-sponsor:local .
```

## 🎯 Déploiement sur Kubernetes

### Configuration rapide

```bash
# 1. Mettre à jour les variables dans Jenkinsfile
nano Jenkinsfile
# Modifier: DOCKER_REGISTRY, NEXUS_URL, SONAR_HOST_URL

# 2. Mettre à jour les secrets
nano k8s/secret.yaml
# Modifier les credentials de production

# 3. Déployer
./scripts/deploy.sh
```

### Vérification

```bash
# Vérifier les pods
kubectl get pods -n smartek-production -l app=smartek-sponsor

# Vérifier les logs
kubectl logs -f -n smartek-production -l app=smartek-sponsor

# Health check
kubectl exec -n smartek-production -it \
  $(kubectl get pod -n smartek-production -l app=smartek-sponsor -o jsonpath='{.items[0].metadata.name}') \
  -- wget -qO- http://localhost:8080/actuator/health
```

## 🔧 Configuration Jenkins

### Étapes minimales

1. **Créer le pipeline**
   - Jenkins → New Item → Pipeline
   - Nom: `smartek-sponsor-pipeline`
   - Pipeline script from SCM
   - Repository: `<your-git-repo>`
   - Script Path: `Backend/smartek_sponsor/Jenkinsfile`

2. **Ajouter les credentials**
   ```
   - nexus-credentials (Username/Password)
   - nexus-docker-credentials (Username/Password)
   - sonarqube-token (Secret Text)
   - kubeconfig-credentials (Secret File)
   ```

3. **Configurer les outils**
   ```
   - Maven-3.9.6
   - JDK-17
   ```

4. **Lancer le build**
   - Build Now

## 📊 Monitoring

### Accès rapide

```bash
# Prometheus
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
# http://localhost:9090

# Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# http://localhost:3000 (admin/admin)
```

### Installation monitoring

```bash
./scripts/setup-monitoring.sh
```

## 🔄 Commandes utiles

### Développement

```bash
# Build local
mvn clean package

# Run tests
mvn test

# Run avec Spring Boot
mvn spring-boot:run

# SonarQube local
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### Docker

```bash
# Build image
docker build -t smartek-sponsor:local .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  smartek-sponsor:local

# Push to Nexus
docker tag smartek-sponsor:local your-nexus-registry:8083/smartek-sponsor:latest
docker push your-nexus-registry:8083/smartek-sponsor:latest
```

### Kubernetes

```bash
# Deploy
kubectl apply -f k8s/

# Scale
kubectl scale deployment smartek-sponsor-deployment --replicas=5 -n smartek-production

# Rollback
./scripts/rollback.sh

# Logs
kubectl logs -f -n smartek-production -l app=smartek-sponsor

# Port-forward
kubectl port-forward -n smartek-production svc/smartek-sponsor-service 8080:8080

# Delete
kubectl delete -f k8s/
```

## 🐛 Troubleshooting rapide

### Application ne démarre pas

```bash
# Vérifier les pods
kubectl get pods -n smartek-production

# Voir les événements
kubectl describe pod <pod-name> -n smartek-production

# Voir les logs
kubectl logs <pod-name> -n smartek-production
```

### Problème de connexion DB

```bash
# Tester la connexion
kubectl exec -it <pod-name> -n smartek-production -- nc -zv mysql-service 3306

# Vérifier les secrets
kubectl get secret smartek-sponsor-secret -n smartek-production -o yaml
```

### Pipeline Jenkins échoue

```bash
# Vérifier les credentials Jenkins
# Vérifier la connectivité avec Nexus
curl http://your-nexus-server:8081

# Vérifier la connectivité avec SonarQube
curl http://your-sonarqube-server:9000

# Vérifier la connectivité avec K8s
kubectl cluster-info
```

## 📁 Structure des fichiers

```
Backend/smartek_sponsor/
├── Dockerfile                    # Image Docker
├── Jenkinsfile                   # Pipeline Jenkins
├── docker-compose.yml            # Environnement local
├── pom.xml                       # Configuration Maven
├── sonar-project.properties      # Configuration SonarQube
├── .gitlab-ci.yml               # Pipeline GitLab (alternatif)
│
├── k8s/                         # Manifestes Kubernetes
│   ├── namespace.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── hpa.yaml
│   ├── servicemonitor.yaml
│   └── kustomization.yaml
│
├── monitoring/                   # Configuration monitoring
│   ├── prometheus.yml
│   ├── prometheus-rules.yaml
│   └── grafana-dashboard.json
│
├── scripts/                      # Scripts utilitaires
│   ├── deploy.sh
│   ├── rollback.sh
│   ├── local-build.sh
│   ├── setup-jenkins.sh
│   └── setup-monitoring.sh
│
└── src/                         # Code source
    ├── main/
    └── test/
```

## 🔗 Liens utiles

### Documentation
- [README.md](README.md) - Documentation complète
- [DEPLOYMENT.md](DEPLOYMENT.md) - Guide de déploiement détaillé

### Services
- Jenkins: `http://your-jenkins-server:8080`
- Nexus: `http://your-nexus-server:8081`
- SonarQube: `http://your-sonarqube-server:9000`
- Application: `https://api.smartek.com/api/sponsor`

### Endpoints de l'application
- Health: `/actuator/health`
- Metrics: `/actuator/prometheus`
- Info: `/actuator/info`
- Swagger: `/swagger-ui.html`

## 📞 Support

- Email: team@smartek.com
- Slack: #smartek-sponsor
- Documentation: [README.md](README.md)

## ✅ Checklist de démarrage

- [ ] Outils installés (kubectl, docker, maven, helm)
- [ ] Repository cloné
- [ ] Scripts rendus exécutables
- [ ] Variables d'environnement configurées
- [ ] Secrets mis à jour
- [ ] Jenkins configuré
- [ ] Application déployée
- [ ] Monitoring installé
- [ ] Tests de bout en bout réussis

## 🎓 Prochaines étapes

1. Lire la [documentation complète](README.md)
2. Configurer les alertes Prometheus
3. Personnaliser les dashboards Grafana
4. Configurer les notifications (Slack, Email)
5. Mettre en place les tests d'intégration
6. Configurer le backup de la base de données
7. Mettre en place la stratégie de rollback automatique

---

**Bon déploiement ! 🚀**
