# Smartek - Stack de Monitoring

## Vue d'ensemble

| Outil | Rôle | Port | URL |
|-------|------|------|-----|
| **Prometheus** | Collecte des métriques | 9090 | http://localhost:9090 |
| **Grafana** | Visualisation & dashboards | 3000 | http://localhost:3000 |
| **Loki** | Agrégation des logs | 3100 | - |
| **Promtail** | Collecte des logs Docker | - | - |
| **Jaeger** | Tracing distribué | 16686 | http://localhost:16686 |
| **Alertmanager** | Gestion des alertes | 9093 | http://localhost:9093 |
| **Node Exporter** | Métriques système (host) | 9100 | - |
| **cAdvisor** | Métriques containers Docker | 8099 | http://localhost:8099 |
| **MySQL Exporter** | Métriques base de données | 9104 | - |
| **Uptime Kuma** | Monitoring de disponibilité | 3001 | http://localhost:3001 |

---

## Détail des outils ajoutés

### Alertmanager (port 9093)
Reçoit les alertes de Prometheus et les route vers les bons canaux (email).
- Config : `monitoring/alertmanager/alertmanager.yml`
- Variables requises : `ALERT_EMAIL_USER`, `ALERT_EMAIL_PASSWORD`
- Intégré dans Grafana comme datasource

### Node Exporter (port 9100)
Expose les métriques système du serveur host :
- CPU, RAM, disque, réseau
- Alertes configurées dans `alert-rules.yml` (groupe `smartek-system`)

### cAdvisor (port 8099)
Expose les métriques de chaque container Docker :
- CPU/RAM par container
- Filtré sur les containers du projet Smartek
- Alertes configurées dans `alert-rules.yml` (groupe `smartek-containers`)

### MySQL Exporter (port 9104)
Expose les métriques de la base de données MySQL :
- Connexions actives, slow queries, statut InnoDB
- Alertes configurées dans `alert-rules.yml` (groupe `smartek-mysql`)

### Uptime Kuma (port 3001)
Dashboard de disponibilité avec interface web simple.
- Configurer manuellement les monitors via l'UI : http://localhost:3001
- Ajouter un monitor pour chaque service via `/actuator/health`

---

## Configuration des alertes email

Dans le fichier `.env` (copié depuis `Backend/.env.example`) :

```env
ALERT_EMAIL_USER=votre-email@gmail.com
ALERT_EMAIL_PASSWORD=votre-app-password-gmail
```

> Pour Gmail, utiliser un "App Password" (pas le mot de passe principal).
> Paramètres Google → Sécurité → Mots de passe des applications

---

## Démarrage

```bash
# Démarrer toute la stack
docker-compose up -d

# Démarrer seulement le monitoring
docker-compose up -d prometheus grafana alertmanager node-exporter cadvisor mysql-exporter uptime-kuma loki promtail jaeger
```

---

## Dashboards Grafana recommandés (import par ID)

| Dashboard | ID | Description |
|-----------|-----|-------------|
| Node Exporter Full | 1860 | Métriques système complets |
| Docker cAdvisor | 14282 | Métriques containers |
| MySQL Overview | 7362 | Métriques MySQL |
| Spring Boot | 12900 | Métriques JVM Spring Boot |
| JVM Micrometer | 4701 | Métriques JVM détaillées |

Pour importer : Grafana → Dashboards → Import → entrer l'ID
