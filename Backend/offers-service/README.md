# Offers Service

Microservice de gestion des offres d'emploi pour la plateforme SMARTEK.

## Fonctionnalités

- Création d'offres d'emploi
- Consultation des offres (toutes, par ID, par entreprise, par statut)
- Modification d'offres
- Suppression d'offres
- Filtrage par entreprise et statut

## Configuration

- Port: 8082
- Base de données: smartek_db (MySQL)
- Enregistrement Eureka: Activé

## API Endpoints

### Créer une offre
```
POST /api/offers
Content-Type: application/json

{
  "title": "Développeur Full Stack",
  "description": "Nous recherchons un développeur Full Stack...",
  "companyName": "SMARTEK",
  "location": "Paris",
  "contractType": "CDI",
  "salary": "45K-55K",
  "companyId": 1,
  "status": "ACTIVE"
}
```

### Récupérer toutes les offres
```
GET /api/offers
```

### Récupérer une offre par ID
```
GET /api/offers/{id}
```

### Récupérer les offres d'une entreprise
```
GET /api/offers/company/{companyId}
```

### Récupérer les offres par statut
```
GET /api/offers/status/{status}
```
Statuts possibles: ACTIVE, CLOSED, DRAFT

### Modifier une offre
```
PUT /api/offers/{id}
Content-Type: application/json

{
  "title": "Développeur Full Stack Senior",
  "description": "Description mise à jour...",
  "companyName": "SMARTEK",
  "location": "Paris",
  "contractType": "CDI",
  "salary": "50K-60K",
  "companyId": 1,
  "status": "ACTIVE"
}
```

### Supprimer une offre
```
DELETE /api/offers/{id}
```

### Health Check
```
GET /api/offers/health
```

## Démarrage

```bash
cd Backend/offers-service
mvn spring-boot:run
```

## Ordre de démarrage

1. Eureka Server (8761)
2. Config Server (8888) - optionnel
3. Offers Service (8082)
4. API Gateway (8080)

## Intégration avec API Gateway

Ajouter cette route dans l'API Gateway:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: offers-service
          uri: lb://offers-service
          predicates:
            - Path=/api/offers/**
```

## Structure de la base de données

Table: `offers`
- id (BIGINT, PK, AUTO_INCREMENT)
- title (VARCHAR)
- description (TEXT)
- company_name (VARCHAR)
- location (VARCHAR)
- contract_type (VARCHAR)
- salary (VARCHAR)
- company_id (BIGINT)
- status (VARCHAR)
- created_at (DATETIME)
- updated_at (DATETIME)
