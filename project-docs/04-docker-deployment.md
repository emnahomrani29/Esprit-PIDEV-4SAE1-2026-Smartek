# Docker & Deployment Guide

## Docker Compose Overview

File: `docker-compose.yml` (version `3.8`)

### Services Defined

| Service | Container Name | Host Port | Container Port | Depends On |
|---------|---------------|-----------|----------------|------------|
| `mysql` | `smartek-mysql` | 3306 | 3306 | – |
| `eureka-server` | `smartek-eureka` | 8761 | 8761 | – |
| `config-server` | `smartek-config` | 8888 | 8888 | `eureka-server` (healthy) |
| `auth-service` | `smartek-auth` | 8081 | 8081 | `mysql`, `eureka-server` |
| `event-service` | `smartek-event` | 8082 | 8082 | `mysql`, `eureka-server` |
| `planning-service` | `smartek-planning` | 8083 | 8083 | `mysql`, `eureka-server` |
| `training-service` | `smartek-training` | 8084 | 8084 | `mysql`, `eureka-server` |
| `offers-service` | `smartek-offers` | 8085 | 8085 | `mysql`, `eureka-server` |
| `course-service` | `smartek-course` | 8086 | 8086 | `mysql`, `eureka-server` |
| `exam-service` | `smartek-exam` | 8087 | 8087 | `mysql`, `eureka-server` |
| `api-gateway` | `smartek-gateway` | 8090 | 8090 | `eureka-server`, all business services |
| `frontend` | `smartek-frontend` | 4200 | 80 | `api-gateway` |

### Network
- **Name:** `smartek-network` (bridge driver)
- All services attach to this network for inter-service DNS resolution.

### Volumes
- **`mysql_data`** (named volume) → persists MySQL data across restarts.
- `./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql` (bind mount) → *file currently missing from repo*.

### Health Checks
| Service | Check |
|---------|-------|
| MySQL | `mysqladmin ping -h localhost -u root -proot` |
| Eureka | `wget --quiet --tries=1 --spider http://localhost:8761/actuator/health` |

## Environment Variables (Typical)

### MySQL
```yaml
MYSQL_ROOT_PASSWORD: root
MYSQL_ROOT_HOST: '%'
```

### Business Services
```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/<db_name>?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: root
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

### API Gateway
```yaml
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
SPRING_CLOUD_GATEWAY_GLOBALCORS_CORSCONFIGURATIONS_[/**]_ALLOWEDORIGINS: "http://localhost:4200,http://frontend:80"
```

## Running the Full Stack

```bash
# Build images & start everything
docker-compose up --build

# Or detached
docker-compose up -d --build

# Scale / restart a single service
docker-compose up -d --build offers-service

# Tear down (keep volumes)
docker-compose down

# Tear down (delete volumes)
docker-compose down -v
```

## Running Select Services

```bash
# Only infrastructure
docker-compose up -d mysql eureka-server config-server

# Then add business services as needed
docker-compose up -d auth-service offers-service api-gateway frontend
```

## Deployment Checklist

- [ ] Ensure `docker-compose.yml` ports do not conflict with host services.
- [ ] Create `init-db.sql` if you need pre-seeded data or explicit schemas.
- [ ] Update `MYSQL_ROOT_PASSWORD` for production.
- [ ] Switch from `createDatabaseIfNotExist=true` to Flyway / Liquibase migrations for production.
- [ ] Add resource limits (`mem_limit`, `cpus`) to each service in `docker-compose.yml`.
- [ ] Use an external reverse proxy (Traefik / Nginx) with TLS termination instead of exposing the gateway directly.
