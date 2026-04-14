# Module RH Company

## Structure

Ce module est basé sur la structure de PI avec des améliorations pour SMARTEK.

### Composants

1. **Dashboard** (`/rh-company/dashboard`)
   - Vue d'ensemble avec statistiques
   - Actions rapides vers Offres et Participation
   - Activités récentes
   - Cartes de navigation

2. **Offers** (`/rh-company/offers`)
   - Gestion des offres d'emploi
   - Gestion des candidatures
   - Composant: `JobOffersComponent` (partagé avec admin)

3. **Participation** (`/rh-company/participation`)
   - Gestion des participations aux formations
   - Inscription des employés

### Navigation

Le layout RH Company (`rh-company-layout`) affiche une navigation horizontale avec :
- 📊 Dashboard
- 💼 Offres
- 🎯 Participation

### Routes

```typescript
/rh-company
  ├── /dashboard (par défaut)
  ├── /offers
  └── /participation
```

### Configuration

Les routes sont définies dans :
- `app.routes.ts` - Routes Angular
- `core/config/role-routes.config.ts` - Configuration des routes par rôle

### Permissions

Accès réservé au rôle `RH_COMPANY` via le guard `permissionGuard`.

## Différences avec PI

SMARTEK ajoute un dashboard dédié pour RH_COMPANY qui n'existe pas dans PI, offrant :
- Vue d'ensemble des statistiques
- Accès rapide aux fonctionnalités principales
- Suivi des activités récentes

## Backend

Le module utilise le service `offers-service` pour :
- Gestion des offres d'emploi
- Gestion des candidatures
- Gestion des entretiens

Voir `Backend/offers-service/INTEGRATION.md` pour plus de détails.
