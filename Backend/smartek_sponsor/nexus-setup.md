# Configuration Nexus Repository Manager

## 📦 Vue d'ensemble

Ce guide explique comment configurer Nexus Repository Manager pour le service Smartek Sponsor.

## 🚀 Installation de Nexus

### Option 1 : Docker (Recommandé pour dev/test)

```bash
# Créer un volume pour les données
docker volume create nexus-data

# Démarrer Nexus
docker run -d \
  --name nexus \
  -p 8081:8081 \
  -p 8082:8082 \
  -p 8083:8083 \
  -v nexus-data:/nexus-data \
  sonatype/nexus3:latest

# Attendre le démarrage (2-3 minutes)
docker logs -f nexus

# Récupérer le mot de passe admin initial
docker exec nexus cat /nexus-data/admin.password
```

### Option 2 : Installation sur Ubuntu

```bash
# Installer Java
sudo apt update
sudo apt install openjdk-8-jdk -y

# Télécharger Nexus
cd /opt
sudo wget https://download.sonatype.com/nexus/3/latest-unix.tar.gz
sudo tar -xvzf latest-unix.tar.gz
sudo mv nexus-3* nexus

# Créer un utilisateur nexus
sudo adduser nexus
sudo chown -R nexus:nexus /opt/nexus
sudo chown -R nexus:nexus /opt/sonatype-work

# Configurer comme service
sudo nano /etc/systemd/system/nexus.service
```

Contenu du fichier `nexus.service` :

```ini
[Unit]
Description=Nexus Repository Manager
After=network.target

[Service]
Type=forking
LimitNOFILE=65536
ExecStart=/opt/nexus/bin/nexus start
ExecStop=/opt/nexus/bin/nexus stop
User=nexus
Restart=on-abort

[Install]
WantedBy=multi-user.target
```

```bash
# Démarrer Nexus
sudo systemctl daemon-reload
sudo systemctl start nexus
sudo systemctl enable nexus

# Vérifier le statut
sudo systemctl status nexus
```

## 🔧 Configuration initiale

### 1. Accès à Nexus

```
URL: http://your-server:8081
Username: admin
Password: (voir /nexus-data/admin.password)
```

### 2. Configuration du mot de passe

1. Se connecter avec le mot de passe initial
2. Suivre l'assistant de configuration
3. Changer le mot de passe admin
4. Activer "Anonymous Access" (optionnel)

## 📦 Configuration des repositories

### 1. Maven Repository (Hosted)

**Pour stocker les artefacts Maven**

1. Aller dans **Settings** → **Repositories** → **Create repository**
2. Sélectionner **maven2 (hosted)**
3. Configuration :
   ```
   Name: maven-releases
   Version policy: Release
   Layout policy: Strict
   Deployment policy: Allow redeploy
   ```
4. Cliquer sur **Create repository**

### 2. Maven Snapshots Repository

1. **Create repository** → **maven2 (hosted)**
2. Configuration :
   ```
   Name: maven-snapshots
   Version policy: Snapshot
   Layout policy: Strict
   Deployment policy: Allow redeploy
   ```

### 3. Docker Registry (Hosted)

**Pour stocker les images Docker**

1. **Create repository** → **docker (hosted)**
2. Configuration :
   ```
   Name: docker-hosted
   HTTP: 8083 (cocher)
   Enable Docker V1 API: Non
   Deployment policy: Allow redeploy
   ```
3. Cliquer sur **Create repository**

### 4. Docker Proxy (Optionnel)

**Pour cacher Docker Hub**

1. **Create repository** → **docker (proxy)**
2. Configuration :
   ```
   Name: docker-proxy
   HTTP: 8082 (cocher)
   Remote storage: https://registry-1.docker.io
   Docker Index: Use Docker Hub
   ```

### 5. Docker Group (Optionnel)

**Pour grouper hosted et proxy**

1. **Create repository** → **docker (group)**
2. Configuration :
   ```
   Name: docker-group
   HTTP: 8084 (cocher)
   Member repositories:
     - docker-hosted
     - docker-proxy
   ```

## 👤 Configuration des utilisateurs

### 1. Créer un utilisateur de déploiement

1. **Settings** → **Security** → **Users** → **Create local user**
2. Configuration :
   ```
   ID: jenkins-deploy
   First name: Jenkins
   Last name: Deploy
   Email: jenkins@smartek.com
   Password: <secure-password>
   Status: Active
   Roles: nx-admin (ou créer un rôle personnalisé)
   ```

### 2. Créer un rôle personnalisé (Recommandé)

1. **Settings** → **Security** → **Roles** → **Create role**
2. Configuration :
   ```
   Role ID: deploy-role
   Role name: Deploy Role
   Privileges:
     - nx-repository-view-maven2-*-*
     - nx-repository-view-docker-*-*
     - nx-repository-admin-maven2-maven-releases-*
     - nx-repository-admin-docker-docker-hosted-*
   ```

## 🔐 Configuration Maven

### 1. Configurer settings.xml

Créer/éditer `~/.m2/settings.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <servers>
    <server>
      <id>nexus</id>
      <username>jenkins-deploy</username>
      <password>your-password</password>
    </server>
  </servers>
  
  <mirrors>
    <mirror>
      <id>nexus</id>
      <mirrorOf>*</mirrorOf>
      <url>http://your-nexus-server:8081/repository/maven-public/</url>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>nexus</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>http://your-nexus-server:8081/repository/maven-public/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>central</id>
          <url>http://your-nexus-server:8081/repository/maven-public/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>nexus</activeProfile>
  </activeProfiles>
</settings>
```

### 2. Configurer pom.xml

Ajouter dans `pom.xml` :

```xml
<distributionManagement>
    <repository>
        <id>nexus</id>
        <name>Releases</name>
        <url>http://your-nexus-server:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus</id>
        <name>Snapshots</name>
        <url>http://your-nexus-server:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

## 🐳 Configuration Docker

### 1. Configurer Docker daemon

Éditer `/etc/docker/daemon.json` :

```json
{
  "insecure-registries": [
    "your-nexus-server:8083",
    "your-nexus-server:8082",
    "your-nexus-server:8084"
  ]
}
```

```bash
# Redémarrer Docker
sudo systemctl restart docker
```

### 2. Login Docker

```bash
# Login au registry hosted
docker login your-nexus-server:8083
Username: jenkins-deploy
Password: your-password

# Tester
docker pull alpine
docker tag alpine your-nexus-server:8083/alpine:latest
docker push your-nexus-server:8083/alpine:latest
```

## 🔒 Configuration HTTPS (Production)

### 1. Générer un certificat SSL

```bash
# Avec Let's Encrypt
sudo apt install certbot -y
sudo certbot certonly --standalone -d nexus.smartek.com

# Ou auto-signé (dev/test)
keytool -genkeypair -keystore keystore.jks -storepass password \
  -keypass password -alias jetty -keyalg RSA -keysize 2048 \
  -validity 5000 -dname "CN=*.smartek.com, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" \
  -ext "SAN=DNS:nexus.smartek.com,IP:192.168.1.100"
```

### 2. Configurer Nexus pour HTTPS

Éditer `/opt/nexus/etc/nexus-default.properties` :

```properties
application-port-ssl=8443
nexus-args=${jetty.etc}/jetty.xml,${jetty.etc}/jetty-https.xml,${jetty.etc}/jetty-requestlog.xml
ssl.etc=${karaf.data}/etc/ssl
```

## 📊 Monitoring Nexus

### 1. Activer les métriques

1. **Settings** → **System** → **Capabilities**
2. **Create capability** → **Metrics Health Check**
3. Activer

### 2. Endpoints disponibles

```
Health Check: http://your-nexus-server:8081/service/rest/v1/status
Metrics: http://your-nexus-server:8081/service/metrics/healthcheck
```

## 🧹 Maintenance

### Cleanup Policies

1. **Settings** → **Repository** → **Cleanup Policies**
2. **Create cleanup policy**
3. Configuration :
   ```
   Name: docker-cleanup
   Format: docker
   Criteria:
     - Last downloaded: 30 days
     - Last blob updated: 60 days
   ```

### Scheduled Tasks

1. **Settings** → **System** → **Tasks**
2. **Create task** → **Docker - Delete unused manifests and images**
3. Schedule: Daily at 2:00 AM

## 🔍 Vérification

### Tester Maven

```bash
cd Backend/smartek_sponsor
mvn clean deploy
```

### Tester Docker

```bash
docker build -t your-nexus-server:8083/smartek-sponsor:test .
docker push your-nexus-server:8083/smartek-sponsor:test
```

## 🐛 Troubleshooting

### Problème de connexion Maven

```bash
# Vérifier la connectivité
curl http://your-nexus-server:8081/repository/maven-public/

# Vérifier les credentials
cat ~/.m2/settings.xml

# Tester avec verbose
mvn deploy -X
```

### Problème Docker push

```bash
# Vérifier le login
docker login your-nexus-server:8083

# Vérifier les insecure-registries
cat /etc/docker/daemon.json

# Vérifier les logs Nexus
docker logs nexus
# ou
tail -f /opt/sonatype-work/nexus3/log/nexus.log
```

### Nexus ne démarre pas

```bash
# Vérifier les logs
docker logs nexus
# ou
tail -f /opt/sonatype-work/nexus3/log/nexus.log

# Vérifier l'espace disque
df -h

# Vérifier la mémoire
free -h
```

## 📚 Ressources

- [Nexus Documentation](https://help.sonatype.com/repomanager3)
- [Docker Registry Guide](https://help.sonatype.com/repomanager3/nexus-repository-administration/formats/docker-registry)
- [Maven Repository Guide](https://help.sonatype.com/repomanager3/nexus-repository-administration/formats/maven-repositories)

## 📞 Support

Pour toute question :
- Email: team@smartek.com
- Documentation: README.md
