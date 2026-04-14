# Troubleshooting - Offers Service

## Problèmes courants et solutions

### 1. Erreur "Impossible de se connecter au serveur"

**Symptôme:** Le frontend affiche "Impossible de se connecter au serveur"

**Causes possibles:**
- Le backend n'est pas démarré
- L'API Gateway n'est pas démarré
- Problème de CORS

**Solutions:**

1. Vérifier que tous les services sont démarrés dans l'ordre:
```bash
# Terminal 1 - Eureka Server
cd Backend/eureka-server
mvn spring-boot:run

# Terminal 2 - Offers Service
cd Backend/offers-service
mvn spring-boot:run

# Terminal 3 - API Gateway
cd Backend/api-gateway
mvn spring-boot:run
```

2. Vérifier que les services sont enregistrés dans Eureka:
```
http://localhost:8761
```
Vous devriez voir:
- OFFERS-SERVICE
- API-GATEWAY

3. Tester directement le service (sans passer par le Gateway):
```bash
curl http://localhost:8082/api/offers/health
```
Réponse attendue: "Offers Service is running!"

4. Tester via l'API Gateway:
```bash
curl http://localhost:8080/api/offers/health
```

### 2. Erreur 404 "Service non trouvé"

**Symptôme:** Erreur 404 lors de l'appel API

**Causes possibles:**
- Le service n'est pas enregistré dans Eureka
- La route n'est pas configurée dans l'API Gateway
- Le service est démarré mais pas encore enregistré (attendre 30 secondes)

**Solutions:**

1. Vérifier les logs du service offers-service:
```
Registering application OFFERS-SERVICE with eureka
```

2. Vérifier la configuration de l'API Gateway (`Backend/api-gateway/src/main/resources/application.yml`):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: offers-service
          uri: lb://offers-service
          predicates:
            - Path=/api/offers/**
          filters:
            - StripPrefix=1
```

3. Redémarrer l'API Gateway après avoir vérifié la configuration

### 3. Erreur 400 "Données invalides"

**Symptôme:** Erreur 400 lors de la création d'une offre

**Causes possibles:**
- Champs obligatoires manquants
- Format de données incorrect
- Validation échouée

**Solutions:**

1. Vérifier que tous les champs obligatoires sont remplis:
   - title (obligatoire)
   - description (obligatoire)
   - companyName (obligatoire)
   - location (obligatoire)
   - contractType (obligatoire)
   - companyId (obligatoire)

2. Vérifier les logs du backend pour voir le message d'erreur exact

3. Tester avec curl pour isoler le problème:
```bash
curl -X POST http://localhost:8080/api/offers \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Offer",
    "description": "Test Description",
    "companyName": "Test Company",
    "location": "Paris",
    "contractType": "CDI",
    "companyId": 1,
    "status": "ACTIVE"
  }'
```

### 4. Erreur 500 "Erreur serveur"

**Symptôme:** Erreur 500 lors de l'appel API

**Causes possibles:**
- Base de données non accessible
- Erreur dans le code backend
- Configuration incorrecte

**Solutions:**

1. Vérifier que MySQL est démarré:
```bash
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl start mysql
```

2. Vérifier que la base de données existe:
```sql
SHOW DATABASES;
USE smartek_db;
SHOW TABLES;
```

3. Vérifier la configuration de la base de données (`Backend/offers-service/src/main/resources/application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartek_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password: 
```

4. Consulter les logs du service pour l'erreur exacte:
```bash
cd Backend/offers-service
mvn spring-boot:run
```

### 5. Problème CORS

**Symptôme:** Erreur CORS dans la console du navigateur

**Solution:**

Vérifier que le CORS est configuré dans le controller:
```java
@CrossOrigin(origins = "http://localhost:4200")
```

Et dans l'API Gateway:
```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: "http://localhost:4200"
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
      allow-credentials: true
```

## Checklist de démarrage

Avant de tester l'application, vérifier:

- [ ] MySQL est démarré
- [ ] La base de données `smartek_db` existe
- [ ] Eureka Server est démarré (port 8761)
- [ ] Offers Service est démarré (port 8082)
- [ ] API Gateway est démarré (port 8080)
- [ ] Les services sont enregistrés dans Eureka (http://localhost:8761)
- [ ] Le frontend Angular est démarré (port 4200)

## Tests de validation

### Test 1: Health Check Direct
```bash
curl http://localhost:8082/api/offers/health
```
Attendu: "Offers Service is running!"

### Test 2: Health Check via Gateway
```bash
curl http://localhost:8080/api/offers/health
```
Attendu: "Offers Service is running!"

### Test 3: Récupérer toutes les offres
```bash
curl http://localhost:8080/api/offers
```
Attendu: `[]` ou liste d'offres

### Test 4: Créer une offre
```bash
curl -X POST http://localhost:8080/api/offers \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Développeur Full Stack",
    "description": "Nous recherchons un développeur Full Stack expérimenté",
    "companyName": "SMARTEK",
    "location": "Paris",
    "contractType": "CDI",
    "salary": "45K-55K",
    "companyId": 1,
    "status": "ACTIVE"
  }'
```
Attendu: Objet JSON avec l'offre créée et un ID

## Logs utiles

### Voir les logs du service
```bash
cd Backend/offers-service
mvn spring-boot:run
```

### Activer les logs de debug
Dans `application.yml`:
```yaml
logging:
  level:
    com.smartek: DEBUG
    org.springframework.web: DEBUG
```

## Support

Si le problème persiste:
1. Vérifier les logs de tous les services
2. Vérifier la console du navigateur (F12)
3. Tester les endpoints avec Postman ou curl
4. Vérifier que toutes les dépendances Maven sont installées
