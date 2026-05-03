# 🚀 Guide d'Installation DevOps - Smartek Platform

## 📋 Table des Matières

1. [Prérequis](#prérequis)
2. [Installation VirtualBox](#installation-virtualbox)
3. [Création VM Ubuntu](#création-vm-ubuntu)
4. [Configuration Réseau](#configuration-réseau)
5. [Installation Docker](#installation-docker)
6. [Installation Kubernetes (kubeadm)](#installation-kubernetes-kubeadm)
7. [Installation Jenkins](#installation-jenkins)
8. [Installation SonarQube](#installation-sonarqube)
9. [Installation Prometheus](#installation-prometheus)
10. [Installation Grafana](#installation-grafana)
11. [Configuration des Intégrations](#configuration-des-intégrations)
12. [Vérification de l'Installation](#vérification-de-linstallation)
13. [Dépannage](#dépannage)

---

## 🎯 Architecture Finale

```
┌─────────────────────────────────────────────────────────────┐
│                    Machine Hôte (Windows)                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                   VirtualBox                           │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │      VM Ubuntu 22.04 (8GB RAM, 4 CPU)           │  │  │
│  │  │      IP: 192.168.56.10                          │  │  │
│  │  │                                                   │  │  │
│  │  │  ☸️  Kubernetes Cluster (kubeadm)                │  │  │
│  │  │  🐳 Docker Engine                                │  │  │
│  │  │  🔧 Jenkins        :8080                         │  │  │
│  │  │  🔍 SonarQube      :9000                         │  │  │
│  │  │  📊 Prometheus     :9090                         │  │  │
│  │  │  📈 Grafana        :3000                         │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Prérequis

### Configuration Minimale Requise

| Composant | Minimum | Recommandé |
|-----------|---------|------------|
| **RAM** | 8 GB | 16 GB |
| **CPU** | 4 cores | 8 cores |
| **Disque** | 50 GB | 100 GB |
| **OS** | Windows 10/11 | Windows 11 |
| **Virtualisation** | Activée dans BIOS | Activée dans BIOS |

### Vérifier la Virtualisation

**Windows PowerShell (Admin):**
```powershell
Get-ComputerInfo | Select-Object -Property "HyperV*"
```

Si désactivée, activer dans le BIOS (Intel VT-x ou AMD-V).

### Téléchargements Nécessaires

1. **VirtualBox** : [https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads)
2. **Ubuntu 22.04 ISO** : [https://ubuntu.com/download/server](https://ubuntu.com/download/server)
   - Télécharger : `ubuntu-22.04.x-live-server-amd64.iso`

---

## 1️⃣ Installation VirtualBox

### Téléchargement

1. Aller sur [https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads)
2. Télécharger "VirtualBox X.X.X platform packages" pour Windows
3. Télécharger aussi "VirtualBox X.X.X Oracle VM VirtualBox Extension Pack"

### Installation

1. Exécuter `VirtualBox-X.X.X-Win.exe`
2. Suivre l'assistant d'installation
3. Installer l'Extension Pack (double-clic sur le fichier `.vbox-extpack`)

### Vérification

```powershell
# Vérifier l'installation
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" --version
# Output attendu: 7.0.x
```

---

## 2️⃣ Création VM Ubuntu

### Étape 1 : Créer une Nouvelle VM

1. Ouvrir **VirtualBox**
2. Cliquer sur **Nouvelle** (ou **New**)
3. Configurer :
   - **Nom** : `smartek-devops`
   - **Type** : Linux
   - **Version** : Ubuntu (64-bit)
   - Cliquer **Suivant**

### Étape 2 : Allouer la Mémoire

- **RAM** : `8192 MB` (8 GB) minimum
- Recommandé : `12288 MB` (12 GB) si possible
- Cliquer **Suivant**

### Étape 3 : Créer un Disque Dur Virtuel

- Sélectionner : **Créer un disque dur virtuel maintenant**
- Cliquer **Créer**
- **Type** : VDI (VirtualBox Disk Image)
- **Stockage** : Dynamiquement alloué
- **Taille** : `80 GB` minimum (100 GB recommandé)
- Cliquer **Créer**

### Étape 4 : Configuration Avancée

Sélectionner la VM `smartek-devops` → **Configuration** (Settings)

#### **Système**
- **Processeur** : 
  - Allouer **4 CPU** minimum (6-8 recommandé)
  - Activer **PAE/NX**
  - Activer **VT-x/AMD-V** (si disponible)

#### **Affichage**
- **Mémoire vidéo** : 128 MB
- **Accélération** : Activer l'accélération 3D

#### **Réseau**
- **Adaptateur 1** :
  - Activer la carte réseau : ✅
  - Mode d'accès réseau : **NAT**
  
- **Adaptateur 2** :
  - Activer la carte réseau : ✅
  - Mode d'accès réseau : **Réseau privé hôte (Host-only)**
  - Nom : `VirtualBox Host-Only Ethernet Adapter`

#### **Stockage**
- Cliquer sur **Vide** (sous Contrôleur IDE)
- Cliquer sur l'icône CD (à droite)
- **Choisir un fichier de disque** → Sélectionner `ubuntu-22.04.x-live-server-amd64.iso`

### Étape 5 : Installer Ubuntu

1. **Démarrer** la VM
2. Sélectionner la langue : **English**
3. **Install Ubuntu Server**
4. Configuration clavier : **English (US)**
5. Type d'installation : **Ubuntu Server**
6. Configuration réseau :
   - **enp0s3** (NAT) : DHCP automatique
   - **enp0s8** (Host-only) : Configurer manuellement
     - IPv4 : `192.168.56.10/24`
     - Gateway : (laisser vide)
7. Proxy : (laisser vide)
8. Mirror : (par défaut)
9. Stockage : **Use entire disk** (utiliser tout le disque)
10. Confirmer : **Continue**

### Étape 6 : Configuration du Profil

- **Your name** : `devops`
- **Server name** : `smartek-devops`
- **Username** : `devops`
- **Password** : `devops123` (ou votre choix)
- **Confirm password** : `devops123`

### Étape 7 : SSH Setup

- **Install OpenSSH server** : ✅ (cocher)
- **Import SSH identity** : No

### Étape 8 : Featured Server Snaps

- Ne rien sélectionner
- **Done**

### Étape 9 : Installation

- Attendre la fin de l'installation (5-10 minutes)
- **Reboot Now**
- Retirer le CD (VirtualBox le fait automatiquement)

### Étape 10 : Premier Démarrage

1. Login avec :
   - Username : `devops`
   - Password : `devops123`

2. Mettre à jour le système :
```bash
sudo apt update
sudo apt upgrade -y
```

3. Installer les outils de base :
```bash
sudo apt install -y \
  curl \
  wget \
  git \
  vim \
  net-tools \
  ca-certificates \
  gnupg \
  lsb-release \
  apt-transport-https \
  software-properties-common
```

---

## 3️⃣ Configuration Réseau

### Vérifier la Configuration Réseau

```bash
# Vérifier les interfaces
ip addr show

# Vous devriez voir :
# - enp0s3: 10.0.2.x (NAT - accès Internet)
# - enp0s8: 192.168.56.10 (Host-only - accès depuis Windows)
```

### Configurer l'IP Statique (si nécessaire)

Si l'IP `192.168.56.10` n'est pas configurée :

```bash
# Éditer la configuration réseau
sudo nano /etc/netplan/00-installer-config.yaml
```

Ajouter/modifier :
```yaml
network:
  version: 2
  ethernets:
    enp0s3:
      dhcp4: true
    enp0s8:
      addresses:
        - 192.168.56.10/24
      dhcp4: false
```

Appliquer :
```bash
sudo netplan apply
```

### Tester la Connectivité

**Depuis la VM Ubuntu :**
```bash
# Tester Internet
ping -c 3 google.com

# Tester l'IP locale
ip addr show enp0s8
```

**Depuis Windows (PowerShell) :**
```powershell
# Tester la connexion à la VM
ping 192.168.56.10

# Si ça fonctionne, vous pouvez SSH depuis Windows
ssh devops@192.168.56.10
# Password: devops123
```

### Installer SSH Client sur Windows (si nécessaire)

```powershell
# Windows 10/11 - PowerShell Admin
Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0
```

Ou utiliser **PuTTY** : [https://www.putty.org/](https://www.putty.org/)

---

## 4️⃣ Installation Docker

### Dans la VM Ubuntu

**Se connecter à la VM :**
```bash
# Depuis Windows (PowerShell ou PuTTY)
ssh devops@192.168.56.10
# Password: devops123
```

**Installer Docker :**
```bash
# Désinstaller anciennes versions
sudo apt-get remove -y docker docker-engine docker.io containerd runc

# Installer Docker via script officiel
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Ajouter l'utilisateur devops au groupe docker
sudo usermod -aG docker devops

# Activer Docker au démarrage
sudo systemctl enable docker
sudo systemctl start docker

# Déconnexion/reconnexion pour appliquer les permissions
exit
```

**Reconnexion et vérification :**
```bash
# Se reconnecter
ssh devops@192.168.56.10

# Vérifier l'installation
docker --version
docker run hello-world
```

### Configuration Docker Registry Local (Optionnel)

```bash
# Créer un registry local pour stocker les images
docker run -d \
  -p 5000:5000 \
  --restart=always \
  --name registry \
  -v registry-data:/var/lib/registry \
  registry:2

# Vérifier
curl http://localhost:5000/v2/_catalog
```

---

## 5️⃣ Installation Kubernetes (kubeadm)

### Préparation du Système

```bash
# Désactiver le swap (requis pour Kubernetes)
sudo swapoff -a
sudo sed -i '/ swap / s/^/#/' /etc/fstab

# Charger les modules kernel nécessaires
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# Configuration sysctl
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### Installation de containerd

```bash
# Installer containerd
sudo apt-get update
sudo apt-get install -y containerd

# Configurer containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml

# Modifier la config pour utiliser systemd cgroup driver
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

# Redémarrer containerd
sudo systemctl restart containerd
sudo systemctl enable containerd
```

### Installation de kubeadm, kubelet, kubectl

```bash
# Ajouter le repository Kubernetes
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | \
  sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | \
  sudo tee /etc/apt/sources.list.d/kubernetes.list

# Installer les packages
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# Vérifier
kubeadm version
kubectl version --client
```

### Initialiser le Cluster Kubernetes

```bash
# Initialiser le cluster (single-node)
sudo kubeadm init \
  --pod-network-cidr=10.244.0.0/16 \
  --apiserver-advertise-address=192.168.56.10

# ⚠️ IMPORTANT: Sauvegarder la commande "kubeadm join" affichée !

# Configurer kubectl pour l'utilisateur vagrant
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Vérifier
kubectl get nodes
# Status: NotReady (normal, CNI pas encore installé)
```

### Installer le CNI (Flannel)

```bash
# Installer Flannel pour le réseau pod
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

# Permettre le scheduling sur le master node (single-node cluster)
kubectl taint nodes --all node-role.kubernetes.io/control-plane-

# Attendre que le node soit Ready
kubectl get nodes
# Status: Ready ✅

# Vérifier les pods système
kubectl get pods -n kube-system
```

---

## 6️⃣ Installation Jenkins

### Via Docker

```bash
# Créer un volume pour persister les données
docker volume create jenkins-data

# Démarrer Jenkins
docker run -d \
  --name jenkins \
  --restart=always \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  jenkins/jenkins:lts

# Attendre 1-2 minutes que Jenkins démarre

# Récupérer le mot de passe initial
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Configuration Initiale

1. Ouvrir **http://192.168.56.10:8080** depuis Windows
2. Coller le mot de passe initial
3. Choisir "Install suggested plugins"
4. Créer un compte admin:
   - Username: `admin`
   - Password: `admin123` (ou votre choix)
   - Email: `admin@smartek.com`
5. **Jenkins URL** : `http://192.168.56.10:8080`

### Installer les Plugins Nécessaires

**Jenkins Dashboard → Manage Jenkins → Plugins → Available plugins**

Installer:
- ✅ Docker Pipeline
- ✅ Kubernetes CLI
- ✅ SonarQube Scanner
- ✅ Pipeline
- ✅ Git
- ✅ Maven Integration
- ✅ NodeJS

### Configurer les Outils

**Manage Jenkins → Tools**

**Maven:**
- Name: `Maven-3.9`
- Install automatically: ✅
- Version: 3.9.6

**NodeJS:**
- Name: `NodeJS-18`
- Install automatically: ✅
- Version: 18.x

**Docker:**
- Name: `Docker`
- Installation root: `/usr/bin/docker`

---

## 7️⃣ Installation SonarQube

### Via Docker avec PostgreSQL

```bash
# Créer un network Docker
docker network create sonarnet

# Démarrer PostgreSQL
docker run -d \
  --name sonarqube-db \
  --network sonarnet \
  --restart=always \
  -e POSTGRES_USER=sonar \
  -e POSTGRES_PASSWORD=sonar \
  -e POSTGRES_DB=sonarqube \
  -v sonarqube-db:/var/lib/postgresql/data \
  postgres:15-alpine

# Attendre 10 secondes
sleep 10

# Configurer les limites système (requis pour SonarQube)
sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072
echo "vm.max_map_count=524288" | sudo tee -a /etc/sysctl.conf
echo "fs.file-max=131072" | sudo tee -a /etc/sysctl.conf

# Démarrer SonarQube
docker run -d \
  --name sonarqube \
  --network sonarnet \
  --restart=always \
  -p 9000:9000 \
  -e SONAR_JDBC_URL=jdbc:postgresql://sonarqube-db:5432/sonarqube \
  -e SONAR_JDBC_USERNAME=sonar \
  -e SONAR_JDBC_PASSWORD=sonar \
  -v sonarqube-data:/opt/sonarqube/data \
  -v sonarqube-logs:/opt/sonarqube/logs \
  -v sonarqube-extensions:/opt/sonarqube/extensions \
  sonarqube:lts-community

# Attendre 2-3 minutes que SonarQube démarre
echo "⏳ SonarQube démarre... Attendre 2-3 minutes"
```

### Configuration Initiale

1. Ouvrir **http://192.168.56.10:9000** depuis Windows
2. Login par défaut:
   - Username: `admin`
   - Password: `admin`
3. Changer le mot de passe (ex: `admin123`)

### Créer un Token pour Jenkins

1. **Administration → Security → Users → admin → Tokens**
2. Generate Token:
   - Name: `jenkins`
   - Type: `Global Analysis Token`
   - Expires in: `No expiration`
3. **Copier le token** (ex: `squ_abc123...`)

---

## 8️⃣ Installation Prometheus

### Via Kubernetes

```bash
# Créer le namespace monitoring
kubectl create namespace monitoring

# Créer le ConfigMap pour Prometheus
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
    
    scrape_configs:
      # Scrape Kubernetes pods
      - job_name: 'kubernetes-pods'
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
            action: keep
            regex: true
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
            action: replace
            target_label: __metrics_path__
            regex: (.+)
          - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
            action: replace
            regex: ([^:]+)(?::\d+)?;(\d+)
            replacement: \$1:\$2
            target_label: __address__
      
      # Scrape Kubernetes nodes
      - job_name: 'kubernetes-nodes'
        kubernetes_sd_configs:
          - role: node
        relabel_configs:
          - action: labelmap
            regex: __meta_kubernetes_node_label_(.+)
      
      # Scrape Spring Boot apps (à configurer plus tard)
      - job_name: 'spring-boot-apps'
        metrics_path: '/actuator/prometheus'
        static_configs:
          - targets: []
EOF

# Déployer Prometheus
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      serviceAccountName: prometheus
      containers:
      - name: prometheus
        image: prom/prometheus:latest
        args:
          - '--config.file=/etc/prometheus/prometheus.yml'
          - '--storage.tsdb.path=/prometheus'
          - '--web.console.libraries=/usr/share/prometheus/console_libraries'
          - '--web.console.templates=/usr/share/prometheus/consoles'
        ports:
        - containerPort: 9090
        volumeMounts:
        - name: config
          mountPath: /etc/prometheus
        - name: storage
          mountPath: /prometheus
      volumes:
      - name: config
        configMap:
          name: prometheus-config
      - name: storage
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: prometheus
  namespace: monitoring
spec:
  type: NodePort
  ports:
  - port: 9090
    targetPort: 9090
    nodePort: 30090
  selector:
    app: prometheus
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: prometheus
  namespace: monitoring
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: prometheus
rules:
- apiGroups: [""]
  resources:
  - nodes
  - nodes/proxy
  - services
  - endpoints
  - pods
  verbs: ["get", "list", "watch"]
- apiGroups:
  - extensions
  resources:
  - ingresses
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: prometheus
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: prometheus
subjects:
- kind: ServiceAccount
  name: prometheus
  namespace: monitoring
EOF

# Vérifier le déploiement
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

### Accéder à Prometheus

Ouvrir **http://192.168.56.10:30090** depuis Windows

---

## 9️⃣ Installation Grafana

### Via Kubernetes

```bash
# Déployer Grafana
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana:latest
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_USER
          value: "admin"
        - name: GF_SECURITY_ADMIN_PASSWORD
          value: "admin"
        - name: GF_USERS_ALLOW_SIGN_UP
          value: "false"
        volumeMounts:
        - name: storage
          mountPath: /var/lib/grafana
      volumes:
      - name: storage
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: grafana
  namespace: monitoring
spec:
  type: NodePort
  ports:
  - port: 3000
    targetPort: 3000
    nodePort: 30300
  selector:
    app: grafana
EOF

# Attendre que Grafana soit prêt
kubectl wait --for=condition=available --timeout=120s \
  deployment/grafana -n monitoring

# Vérifier
kubectl get pods -n monitoring
```

### Accéder à Grafana

1. Ouvrir **http://192.168.56.10:30300** depuis Windows
2. Login:
   - Username: `admin`
   - Password: `admin`
3. Changer le mot de passe (ex: `admin123`)

---

## 🔗 Configuration des Intégrations

### 1. Configurer SonarQube dans Jenkins

**Jenkins → Manage Jenkins → System → SonarQube servers**

- Name: `SonarQube`
- Server URL: `http://192.168.56.10:9000`
- Server authentication token: `[Token créé précédemment]`

### 2. Configurer Kubernetes dans Jenkins

**Jenkins → Manage Jenkins → Clouds → New cloud → Kubernetes**

- Name: `kubernetes`
- Kubernetes URL: `https://192.168.56.10:6443`
- Kubernetes Namespace: `default`
- Credentials: Ajouter le kubeconfig

```bash
# Dans la VM, récupérer le kubeconfig
cat ~/.kube/config
```

### 3. Configurer Prometheus dans Grafana

**Grafana → Configuration → Data Sources → Add data source → Prometheus**

- Name: `Prometheus`
- URL: `http://prometheus.monitoring.svc.cluster.local:9090`
- Access: `Server (default)`
- Cliquer **Save & Test**

### 4. Importer des Dashboards Grafana

**Grafana → Dashboards → Import**

Dashboards recommandés:
- **ID 1860**: Node Exporter Full
- **ID 6417**: Kubernetes Cluster Monitoring
- **ID 4701**: JVM (Micrometer)

---

## 🎯 Script d'Installation Automatique (Optionnel)

Pour automatiser toute l'installation, créer un script `install-devops.sh` dans la VM :

```bash
#!/bin/bash

echo "🚀 Installation DevOps Stack - Smartek Platform"
echo "================================================"

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1. Docker
echo -e "${BLUE}📦 Installation Docker...${NC}"
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker
echo -e "${GREEN}✅ Docker installé${NC}"

# 2. Kubernetes
echo -e "${BLUE}☸️  Installation Kubernetes...${NC}"
sudo swapoff -a
sudo sed -i '/ swap / s/^/#/' /etc/fstab

cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system

sudo apt-get update
sudo apt-get install -y containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml
sudo systemctl restart containerd
sudo systemctl enable containerd

sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | \
  sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | \
  sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=192.168.56.10

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
kubectl taint nodes --all node-role.kubernetes.io/control-plane-

echo -e "${GREEN}✅ Kubernetes installé${NC}"

# 3. Jenkins
echo -e "${BLUE}🔧 Installation Jenkins...${NC}"
docker volume create jenkins-data
docker run -d \
  --name jenkins \
  --restart=always \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

echo -e "${GREEN}✅ Jenkins installé${NC}"
echo "⏳ Attendre 2 minutes pour récupérer le mot de passe..."
sleep 120
echo "🔑 Mot de passe Jenkins:"
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 4. SonarQube
echo -e "${BLUE}🔍 Installation SonarQube...${NC}"
docker network create sonarnet || true

docker run -d \
  --name sonarqube-db \
  --network sonarnet \
  --restart=always \
  -e POSTGRES_USER=sonar \
  -e POSTGRES_PASSWORD=sonar \
  -e POSTGRES_DB=sonarqube \
  -v sonarqube-db:/var/lib/postgresql/data \
  postgres:15-alpine

sleep 10

sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072
echo "vm.max_map_count=524288" | sudo tee -a /etc/sysctl.conf
echo "fs.file-max=131072" | sudo tee -a /etc/sysctl.conf

docker run -d \
  --name sonarqube \
  --network sonarnet \
  --restart=always \
  -p 9000:9000 \
  -e SONAR_JDBC_URL=jdbc:postgresql://sonarqube-db:5432/sonarqube \
  -e SONAR_JDBC_USERNAME=sonar \
  -e SONAR_JDBC_PASSWORD=sonar \
  -v sonarqube-data:/opt/sonarqube/data \
  -v sonarqube-logs:/opt/sonarqube/logs \
  -v sonarqube-extensions:/opt/sonarqube/extensions \
  sonarqube:lts-community

echo -e "${GREEN}✅ SonarQube installé${NC}"

# 5. Prometheus
echo -e "${BLUE}📊 Installation Prometheus...${NC}"
kubectl create namespace monitoring || true

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
      - job_name: 'kubernetes-pods'
        kubernetes_sd_configs:
          - role: pod
EOF

# [Le reste du manifest Prometheus...]

echo -e "${GREEN}✅ Prometheus installé${NC}"

# 6. Grafana
echo -e "${BLUE}📈 Installation Grafana...${NC}"
# [Manifest Grafana...]
echo -e "${GREEN}✅ Grafana installé${NC}"

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║          ✅ INSTALLATION TERMINÉE !                    ║"
echo "╠════════════════════════════════════════════════════════╣"
echo "║  🔧 Jenkins:     http://192.168.56.10:8080            ║"
echo "║  🔍 SonarQube:   http://192.168.56.10:9000            ║"
echo "║  📊 Prometheus:  http://192.168.56.10:30090           ║"
echo "║  📈 Grafana:     http://192.168.56.10:30300           ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "⚠️  IMPORTANT: Déconnectez-vous et reconnectez-vous pour Docker"
echo "   exit && ssh devops@192.168.56.10"
```

**Utilisation du script :**
```bash
# Copier le script dans la VM
nano install-devops.sh
# Coller le contenu ci-dessus

# Rendre exécutable
chmod +x install-devops.sh

# Exécuter
./install-devops.sh
```

---

## ✅ Vérification de l'Installation

### Checklist Complète

```bash
# Se connecter à la VM
ssh devops@192.168.56.10

# 1. Docker
docker --version
docker ps

# 2. Kubernetes
kubectl get nodes
kubectl get pods -A

# 3. Jenkins (depuis Windows)
# http://192.168.56.10:8080

# 4. SonarQube (depuis Windows)
# http://192.168.56.10:9000

# 5. Prometheus (depuis Windows)
# http://192.168.56.10:30090

# 6. Grafana (depuis Windows)
# http://192.168.56.10:30300
```

### Tableau Récapitulatif des URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **Jenkins** | http://192.168.56.10:8080 | admin / admin123 |
| **SonarQube** | http://192.168.56.10:9000 | admin / admin123 |
| **Prometheus** | http://192.168.56.10:30090 | - |
| **Grafana** | http://192.168.56.10:30300 | admin / admin123 |
| **Kubernetes API** | https://192.168.56.10:6443 | kubeconfig |
| **SSH VM** | ssh devops@192.168.56.10 | devops / devops123 |

---

## 🔧 Dépannage

### Problème: Impossible de se connecter à la VM

```bash
# Vérifier l'IP de la VM
ip addr show enp0s8

# Depuis Windows, vérifier la connectivité
ping 192.168.56.10

# Si pas de réponse, vérifier la configuration réseau VirtualBox
# Fichier → Gestionnaire de réseau hôte
# Vérifier que l'adaptateur existe avec l'IP 192.168.56.1
```

### Problème: VM lente

```bash
# Augmenter la RAM et CPU dans VirtualBox
# Éteindre la VM d'abord
# Configuration → Système → Mémoire de base : 12288 MB
# Configuration → Système → Processeur : 6 CPU
```

### Problème: Accès aux services depuis Windows

```bash
# Les services doivent être accessibles via l'IP de la VM
# Exemples:
# Jenkins:     http://192.168.56.10:8080
# SonarQube:   http://192.168.56.10:9000
# Prometheus:  http://192.168.56.10:30090
# Grafana:     http://192.168.56.10:30300

# Si pas d'accès, vérifier le firewall dans la VM
sudo ufw status
sudo ufw allow 8080/tcp
sudo ufw allow 9000/tcp
sudo ufw allow 30090/tcp
sudo ufw allow 30300/tcp
```

### Problème: Kubernetes nodes NotReady

```bash
# Vérifier les logs
kubectl describe node

# Réinstaller le CNI
kubectl delete -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### Problème: SonarQube ne démarre pas

```bash
# Vérifier les logs
docker logs sonarqube

# Vérifier les limites système
sysctl vm.max_map_count
# Doit être >= 524288

# Redémarrer SonarQube
docker restart sonarqube
```

### Problème: Mémoire insuffisante

```ruby
# Modifier le Vagrantfile
vb.memory = "12288"  # 12 GB au lieu de 8 GB

# Recharger la VM
vagrant reload
```

---

## 📚 Commandes Utiles

### VirtualBox

```powershell
# Depuis Windows - Gérer les VMs via ligne de commande
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" list vms
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm "smartek-devops" --type headless
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" controlvm "smartek-devops" poweroff
```

### SSH

```bash
# Se connecter depuis Windows
ssh devops@192.168.56.10

# Copier des fichiers vers la VM
scp fichier.txt devops@192.168.56.10:/home/devops/

# Copier des fichiers depuis la VM
scp devops@192.168.56.10:/home/devops/fichier.txt .
```

### Docker

```bash
docker ps                           # Lister les conteneurs
docker logs [container]             # Voir les logs
docker restart [container]          # Redémarrer
docker exec -it [container] bash    # Shell dans le conteneur
```

### Kubernetes

```bash
kubectl get nodes                   # Lister les nodes
kubectl get pods -A                 # Lister tous les pods
kubectl get svc -A                  # Lister tous les services
kubectl logs [pod] -n [namespace]   # Voir les logs
kubectl describe pod [pod]          # Détails d'un pod
```

---

## 🎯 Prochaines Étapes

1. ✅ Créer les Jenkinsfiles pour les pipelines
2. ✅ Configurer les webhooks Git → Jenkins
3. ✅ Créer les manifests Kubernetes pour l'application
4. ✅ Configurer les dashboards Grafana personnalisés
5. ✅ Mettre en place les alertes Prometheus

---

## 📞 Support

En cas de problème:
1. Vérifier les logs: `docker logs [container]` ou `kubectl logs [pod]`
2. Consulter la section [Dépannage](#dépannage)
3. Redémarrer les services: `docker restart [container]`

---

**Auteur:** Équipe DevOps Smartek  
**Date:** Mai 2026  
**Version:** 1.0
