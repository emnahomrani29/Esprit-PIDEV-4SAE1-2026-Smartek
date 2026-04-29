# Guide de Déploiement - Smartek Sponsor Service

## 🎯 Objectif

Ce guide détaille le processus complet de déploiement du service Smartek Sponsor sur un cluster Kubernetes avec monitoring Prometheus/Grafana.

## 📋 Prérequis

### Infrastructure

- [ ] Cluster Kubernetes opérationnel (kubeadm)
- [ ] kubectl configuré et connecté au cluster
- [ ] Helm 3.x installé
- [ ] Accès administrateur au cluster

### Services externes

- [ ] Jenkins configuré avec les credentials
- [ ] Nexus Repository Manager opérationnel
- [ ] SonarQube configuré
- [ ] Serveur de base de données MySQL

### Credentials

- [ ] Credentials Nexus (Maven + Docker)
- [ ] Token SonarQube
- [ ] Kubeconfig pour Jenkins
- [ ] Credentials base de données

## 🚀 Étapes de déploiement

### Étape 1 : Préparation de l'environnement

#### 1.1 Vérifier la connexion au cluster

```bash
kubectl cluster-info
kubectl get nodes
```

#### 1.2 Créer le namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

#### 1.3 Créer le secret Docker Registry

```bash
kubectl create secret docker-registry nexus-registry-secret \
  --docker-server=your-nexus-registry:8083 \
  --docker-username=admin \
  --docker-password=<NEXUS_PASSWORD> \
  --namespace=smartek-production
```

### Étape 2 : Configuration de la base de données

#### 2.1 Déployer MySQL (si nécessaire)

```bash
# Créer un PersistentVolume pour MySQL
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: smartek-production
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: smartek-production
spec:
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "rootpassword"
        - name: MYSQL_DATABASE
          value: "smartek_sponsor"
        - name: MYSQL_USER
          value: "smartek_user"
        - name: MYSQL_PASSWORD
          value: "smartek_password"
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: smartek-production
spec:
  selector:
    app: mysql
  ports:
  - port: 3306
    targetPort: 3306
EOF
```

#### 2.2 Vérifier MySQL

```bash
kubectl get pods -n smartek-production -l app=mysql
kubectl logs -n smartek-production -l app=mysql
```

### Étape 3 : Configuration des secrets et ConfigMaps

#### 3.1 Mettre à jour les secrets

```bash
# Éditer le fichier k8s/secret.yaml avec les vraies valeurs
nano k8s/secret.yaml

# Appliquer le secret
kubectl apply -f k8s/secret.yaml
```

#### 3.2 Appliquer le ConfigMap

```bash
# Vérifier et modifier si nécessaire
nano k8s/configmap.yaml

# Appliquer
kubectl apply -f k8s/configmap.yaml
```

### Étape 4 : Installation du monitoring

#### 4.1 Installer Prometheus Operator

```bash
# Ajouter le repo Helm
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Créer le namespace monitoring
kubectl create namespace monitoring

# Installer Prometheus Operator
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set grafana.adminPassword=admin
```

#### 4.2 Vérifier l'installation

```bash
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

#### 4.3 Appliquer les règles Prometheus

```bash
kubectl apply -f monitoring/prometheus-rules.yaml
```

#### 4.4 Créer le dashboard Grafana

```bash
kubectl create configmap smartek-sponsor-dashboard \
  --from-file=monitoring/grafana-dashboard.json \
  -n monitoring \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl label configmap smartek-sponsor-dashboard \
  grafana_dashboard=1 \
  -n monitoring \
  --overwrite
```

### Étape 5 : Déploiement de l'application

#### 5.1 Utiliser le script de déploiement

```bash
cd scripts
chmod +x deploy.sh
./deploy.sh
```

#### 5.2 OU Déploiement manuel

```bash
# Service
kubectl apply -f k8s/service.yaml

# Deployment
kubectl apply -f k8s/deployment.yaml

# HPA
kubectl apply -f k8s/hpa.yaml

# ServiceMonitor
kubectl apply -f k8s/servicemonitor.yaml

# Ingress
kubectl apply -f k8s/ingress.yaml
```

#### 5.3 Attendre le déploiement

```bash
kubectl rollout status deployment/smartek-sponsor-deployment -n smartek-production
```

### Étape 6 : Vérification du déploiement

#### 6.1 Vérifier les pods

```bash
kubectl get pods -n smartek-production -l app=smartek-sponsor
```

Sortie attendue :
```
NAME                                          READY   STATUS    RESTARTS   AGE
smartek-sponsor-deployment-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
smartek-sponsor-deployment-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
smartek-sponsor-deployment-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
```

#### 6.2 Vérifier les logs

```bash
kubectl logs -f -n smartek-production -l app=smartek-sponsor --tail=50
```

#### 6.3 Vérifier la santé de l'application

```bash
# Health check
kubectl exec -n smartek-production -it \
  $(kubectl get pod -n smartek-production -l app=smartek-sponsor -o jsonpath='{.items[0].metadata.name}') \
  -- wget -qO- http://localhost:8080/actuator/health

# Metrics
kubectl exec -n smartek-production -it \
  $(kubectl get pod -n smartek-production -l app=smartek-sponsor -o jsonpath='{.items[0].metadata.name}') \
  -- wget -qO- http://localhost:8080/actuator/prometheus
```

#### 6.4 Vérifier le service

```bash
kubectl get svc -n smartek-production smartek-sponsor-service
```

#### 6.5 Vérifier l'ingress

```bash
kubectl get ingress -n smartek-production
```

### Étape 7 : Configuration Jenkins

#### 7.1 Créer le pipeline Jenkins

1. Ouvrir Jenkins : `http://your-jenkins-server:8080`
2. Cliquer sur "New Item"
3. Nom : `smartek-sponsor-pipeline`
4. Type : "Pipeline"
5. Configuration :
   - **Pipeline** → **Definition** : Pipeline script from SCM
   - **SCM** : Git
   - **Repository URL** : `<your-git-repo>`
   - **Branch** : `*/main`
   - **Script Path** : `Backend/smartek_sponsor/Jenkinsfile`

#### 7.2 Configurer les credentials

Aller dans "Manage Jenkins" → "Manage Credentials" :

1. **nexus-credentials**
   - Kind: Username with password
   - ID: `nexus-credentials`
   - Username: `admin`
   - Password: `<nexus-password>`

2. **nexus-docker-credentials**
   - Kind: Username with password
   - ID: `nexus-docker-credentials`
   - Username: `admin`
   - Password: `<nexus-password>`

3. **sonarqube-token**
   - Kind: Secret text
   - ID: `sonarqube-token`
   - Secret: `<sonarqube-token>`

4. **kubeconfig-credentials**
   - Kind: Secret file
   - ID: `kubeconfig-credentials`
   - File: Upload `~/.kube/config`

#### 7.3 Configurer les outils

Aller dans "Manage Jenkins" → "Global Tool Configuration" :

1. **JDK**
   - Name: `JDK-17`
   - JAVA_HOME: `/usr/lib/jvm/java-17-openjdk-amd64`

2. **Maven**
   - Name: `Maven-3.9.6`
   - Install automatically : Cocher
   - Version: `3.9.6`

#### 7.4 Tester le pipeline

```bash
# Déclencher le build
# Aller sur Jenkins → smartek-sponsor-pipeline → Build Now
```

### Étape 8 : Accès aux services

#### 8.1 Grafana

```bash
# Port-forward
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Accéder à Grafana
# URL: http://localhost:3000
# Username: admin
# Password: admin (ou celui configuré)
```

#### 8.2 Prometheus

```bash
# Port-forward
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090

# Accéder à Prometheus
# URL: http://localhost:9090
```

#### 8.3 Application

```bash
# Via Ingress (si configuré)
curl https://api.smartek.com/api/sponsor/actuator/health

# Via Port-forward
kubectl port-forward -n smartek-production svc/smartek-sponsor-service 8080:8080
curl http://localhost:8080/actuator/health
```

## 🔄 Mise à jour de l'application

### Via Jenkins (Recommandé)

```bash
# 1. Commit et push le code
git add .
git commit -m "Update feature"
git push origin main

# 2. Le pipeline Jenkins se déclenche automatiquement
# 3. Vérifier le build sur Jenkins
# 4. Vérifier le déploiement
kubectl get pods -n smartek-production -l app=smartek-sponsor
```

### Mise à jour manuelle

```bash
# 1. Build et push l'image
docker build -t your-nexus-registry:8083/smartek-sponsor:v1.0.1 .
docker push your-nexus-registry:8083/smartek-sponsor:v1.0.1

# 2. Mettre à jour le deployment
kubectl set image deployment/smartek-sponsor-deployment \
  smartek-sponsor=your-nexus-registry:8083/smartek-sponsor:v1.0.1 \
  -n smartek-production

# 3. Vérifier le rollout
kubectl rollout status deployment/smartek-sponsor-deployment -n smartek-production
```

## 🔙 Rollback

### Rollback automatique

```bash
cd scripts
chmod +x rollback.sh
./rollback.sh
```

### Rollback manuel

```bash
# Voir l'historique
kubectl rollout history deployment/smartek-sponsor-deployment -n smartek-production

# Rollback vers la version précédente
kubectl rollout undo deployment/smartek-sponsor-deployment -n smartek-production

# Rollback vers une version spécifique
kubectl rollout undo deployment/smartek-sponsor-deployment --to-revision=2 -n smartek-production
```

## 🧹 Nettoyage

### Supprimer l'application

```bash
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/service.yaml
kubectl delete -f k8s/ingress.yaml
kubectl delete -f k8s/hpa.yaml
kubectl delete -f k8s/servicemonitor.yaml
kubectl delete -f k8s/configmap.yaml
kubectl delete -f k8s/secret.yaml
```

### Supprimer le namespace

```bash
kubectl delete namespace smartek-production
```

## 🐛 Troubleshooting

### Les pods ne démarrent pas

```bash
# Vérifier les événements
kubectl describe pod <pod-name> -n smartek-production

# Vérifier les logs
kubectl logs <pod-name> -n smartek-production

# Problèmes courants :
# - Image pull error : Vérifier le secret docker-registry
# - CrashLoopBackOff : Vérifier les logs et la configuration
# - Pending : Vérifier les ressources disponibles
```

### Problèmes de connexion à la base de données

```bash
# Vérifier que MySQL est accessible
kubectl exec -it <pod-name> -n smartek-production -- nc -zv mysql-service 3306

# Vérifier les credentials
kubectl get secret smartek-sponsor-secret -n smartek-production -o yaml
```

### Métriques Prometheus non disponibles

```bash
# Vérifier le ServiceMonitor
kubectl get servicemonitor -n smartek-production

# Vérifier les targets dans Prometheus
# Aller sur Prometheus → Status → Targets
```

### Pipeline Jenkins échoue

```bash
# Vérifier les logs Jenkins
# Vérifier les credentials
# Vérifier la connectivité avec Nexus, SonarQube, K8s
```

## 📞 Support

Pour toute question ou problème :
- Documentation : Voir README.md
- Email : team@smartek.com
- Slack : #smartek-sponsor

## ✅ Checklist de déploiement

- [ ] Cluster Kubernetes opérationnel
- [ ] Namespace créé
- [ ] MySQL déployé et accessible
- [ ] Secrets configurés
- [ ] ConfigMap appliqué
- [ ] Prometheus Operator installé
- [ ] Application déployée
- [ ] Pods en état Running
- [ ] Health checks OK
- [ ] Métriques visibles dans Prometheus
- [ ] Dashboard Grafana configuré
- [ ] Pipeline Jenkins configuré
- [ ] Tests de bout en bout réussis
