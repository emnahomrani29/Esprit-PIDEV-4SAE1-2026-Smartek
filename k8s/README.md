# Kubernetes — Smartek Platform

Déploiement des microservices Smartek sur Kubernetes avec kubeadm.

## Structure

```
k8s/
├── namespace.yml              # Namespace smartek
├── configmap.yml              # Variables de configuration communes
├── secret.yml                 # Secrets (mots de passe DB)
├── infrastructure/
│   ├── mysql.yml              # MySQL + PVC
│   ├── eureka-server.yml      # Service Discovery
│   └── api-gateway.yml        # API Gateway (NodePort 30090)
├── services/
│   ├── event-service.yml      # Port 8082
│   ├── planning-service.yml   # Port 8083
│   ├── training-service.yml   # Port 8084
│   ├── course-service.yml     # Port 8086
│   └── exam-service.yml       # Port 8087
├── monitoring/
│   ├── prometheus.yml         # Prometheus (NodePort 30090)
│   └── grafana.yml            # Grafana (NodePort 30300)
└── deploy.sh                  # Script de déploiement
```

## Prérequis

- Cluster Kubernetes initialisé avec kubeadm
- `kubectl` configuré
- Images Docker buildées et disponibles

## Déploiement complet

```bash
# Rendre le script exécutable
chmod +x k8s/deploy.sh

# Déployer tout
./k8s/deploy.sh all
```

## Déploiement par étape

```bash
# 1. Infrastructure (MySQL, Eureka, API Gateway)
./k8s/deploy.sh infra

# 2. Microservices
./k8s/deploy.sh services

# 3. Monitoring
./k8s/deploy.sh monitoring
```

## Commandes utiles

```bash
# Voir l'état des pods
kubectl get pods -n smartek

# Voir les logs d'un service
kubectl logs -f deployment/course-service -n smartek

# Voir les services exposés
kubectl get services -n smartek

# Accéder à un pod
kubectl exec -it deployment/course-service -n smartek -- sh

# Supprimer tout
./k8s/deploy.sh delete
```

## Accès aux services

| Service     | NodePort | URL                        |
|-------------|----------|----------------------------|
| API Gateway | 30090    | http://NODE_IP:30090       |
| Prometheus  | 30090    | http://NODE_IP:30090       |
| Grafana     | 30300    | http://NODE_IP:30300       |

Grafana login : `admin` / `smartek123`

## Initialisation kubeadm (rappel)

```bash
# Sur le nœud master
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# Configurer kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Installer le réseau (Flannel)
kubectl apply -f https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml
```
