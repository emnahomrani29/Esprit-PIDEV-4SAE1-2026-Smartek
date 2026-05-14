# SMARTEK Auth Service

Microservice d'authentification pour la plateforme SMARTEK.

## Technologies

- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- MySQL
- JWT (JSON Web Tokens)
- Spring Cloud (Eureka Client, Config Client)
- Lombok

## Configuration

### Base de données MySQL

```sql
CREATE DATABASE smartek_db;
```

Modifier les credentials dans `application.yml` si nécessaire:
```yaml
spring:
  datasource:
    username: root
    password: root
```

## Entité User

L'entité User contient les champs suivants:

- `userId` (Long) - ID auto-généré
- `firstName` (String) - Prénom
- `email` (String) - Email unique
- `password` (String) - Mot de passe hashé
- `phone` (String) - Numéro de téléphone
- `profilePicture` (String) - URL de la photo de profil
- `experience` (Integer) - Points d'expérience
- `role` (RoleType) - Rôle de l'utilisateur
- `active` (Boolean) - Compte actif ou non
- `emailVerified` (Boolean) - Email vérifié
- `createdAt` (LocalDateTime) - Date de création
- `updatedAt` (LocalDateTime) - Date de mise à jour
- `lastLogin` (LocalDateTime) - Dernière connexion

## Rôles (RoleType)

- `LEARNER` - Apprenant
- `ADMIN` - Administrateur
- `TRAINER` - Formateur
- `RH_COMPANY` - RH Entreprise
- `RH_SMARTEK` - RH SMARTEK
- `PARTNER` - Partenaire

## API Endpoints

### Inscription
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "email": "john@example.com",
  "password": "password123",
  "phone": "0612345678",
  "role": "LEARNER"
}
```

### Connexion
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Health Check
```http
GET /api/auth/health
```

## Réponse d'authentification

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "role": "LEARNER",
  "message": "Connexion réussie"
}
```

## Démarrage

```bash
# Compiler le projet
mvn clean install

# Lancer l'application
mvn spring-boot:run
```

Le service sera accessible sur `http://localhost:8081`

## Intégration avec Eureka

Le service s'enregistre automatiquement auprès d'Eureka Server sur `http://localhost:8761`

## Sécurité

- Les mots de passe sont hashés avec BCrypt
- Les tokens JWT expirent après 24 heures
- CORS activé pour permettre les requêtes depuis le frontend Angular
- Les endpoints `/api/auth/**` sont publics
- Tous les autres endpoints nécessitent une authentification

## Tests avec cURL

### Inscription
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test User",
    "email": "test@smartek.com",
    "password": "password123",
    "role": "LEARNER"
  }'
```

### Connexion
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@smartek.com",
    "password": "password123"
  }'
```
