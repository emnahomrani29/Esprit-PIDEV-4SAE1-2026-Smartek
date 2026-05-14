# Job Offers Component - CRUD Interface

Interface complète de gestion des offres d'emploi accessible depuis le dashboard.

## URL d'accès

```
http://localhost:4200/dashboard/job-offers
```

## Fonctionnalités

### ✅ Créer une offre (CREATE)
- Formulaire modal avec tous les champs requis
- Validation des champs obligatoires
- Types de contrat: CDI, CDD, Stage, Alternance, Freelance
- Statuts: ACTIVE, CLOSED, DRAFT

### ✅ Lire les offres (READ)
- Affichage en grille responsive (1/2/3 colonnes)
- Filtrage par statut (Tous, Actif, Fermé, Brouillon)
- Compteur d'offres
- Badges de statut colorés
- Affichage des détails: titre, entreprise, localisation, salaire, description

### ✅ Modifier une offre (UPDATE)
- Formulaire pré-rempli avec les données existantes
- Mise à jour en temps réel après sauvegarde

### ✅ Supprimer une offre (DELETE)
- Confirmation avant suppression
- Suppression immédiate de la liste

## Structure des fichiers

```
dashboard/job-offers/
├── job-offers.component.ts       # Logique du composant
├── job-offers.component.html     # Template avec modal CRUD
├── job-offers.component.css      # Styles personnalisés
└── README.md                     # Documentation
```

## Services utilisés

- `OfferService` - Service HTTP pour les appels API
  - `getAllOffers()` - Récupère toutes les offres
  - `getOfferById(id)` - Récupère une offre par ID
  - `getOffersByCompanyId(companyId)` - Filtre par entreprise
  - `getOffersByStatus(status)` - Filtre par statut
  - `createOffer(offer)` - Crée une nouvelle offre
  - `updateOffer(id, offer)` - Met à jour une offre
  - `deleteOffer(id)` - Supprime une offre

## Modèles de données

### Offer
```typescript
{
  id?: number;
  title: string;
  description: string;
  companyName: string;
  location: string;
  contractType: string;
  salary?: string;
  companyId: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}
```

### OfferRequest
```typescript
{
  title: string;
  description: string;
  companyName: string;
  location: string;
  contractType: string;
  salary?: string;
  companyId: number;
  status?: string;
}
```

## API Endpoints utilisés

- `GET /api/offers` - Liste toutes les offres
- `GET /api/offers/{id}` - Détails d'une offre
- `GET /api/offers/company/{companyId}` - Offres par entreprise
- `GET /api/offers/status/{status}` - Offres par statut
- `POST /api/offers` - Créer une offre
- `PUT /api/offers/{id}` - Modifier une offre
- `DELETE /api/offers/{id}` - Supprimer une offre

## Prérequis

1. Backend offers-service démarré sur le port 8082
2. API Gateway démarré sur le port 8080
3. Eureka Server démarré sur le port 8761
4. Base de données MySQL avec la table `offers`

## Démarrage

1. Démarrer le backend:
```bash
cd Backend/offers-service
mvn spring-boot:run
```

2. Démarrer le frontend:
```bash
cd Frontend/angular-app
ng serve
```

3. Accéder à l'interface:
```
http://localhost:4200/dashboard/job-offers
```

## Tests manuels

### Test CREATE
1. Cliquer sur "Nouvelle Offre"
2. Remplir tous les champs obligatoires
3. Cliquer sur "Créer"
4. Vérifier que l'offre apparaît dans la liste

### Test READ
1. Vérifier que toutes les offres s'affichent
2. Tester le filtre par statut
3. Vérifier le compteur d'offres

### Test UPDATE
1. Cliquer sur "Modifier" sur une offre
2. Modifier les champs
3. Cliquer sur "Mettre à jour"
4. Vérifier que les modifications sont visibles

### Test DELETE
1. Cliquer sur "Supprimer" sur une offre
2. Confirmer la suppression
3. Vérifier que l'offre disparaît de la liste

## Améliorations futures

- [ ] Pagination pour grandes listes
- [ ] Recherche par mots-clés
- [ ] Tri par date, titre, entreprise
- [ ] Export des offres (CSV, PDF)
- [ ] Statistiques des offres
- [ ] Gestion des candidatures
- [ ] Notifications
- [ ] Upload de logo d'entreprise
