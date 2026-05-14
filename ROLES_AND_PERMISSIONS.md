# 🔐 Rôles et Permissions - Plateforme SMARTEK

## 📋 Table des matières
- [Vue d'ensemble des rôles](#vue-densemble-des-rôles)
- [Détail des permissions par rôle](#détail-des-permissions-par-rôle)
- [Matrice d'accès par microservice](#matrice-daccès-par-microservice)

---

## 🎭 Vue d'ensemble des rôles

La plateforme SMARTEK dispose de **7 rôles** distincts:

| Rôle | Code | Description | Accès Principal |
|------|------|-------------|-----------------|
| 🎓 **Apprenant** | `LEARNER` | Utilisateur suivant des formations | Consulter et suivre les formations |
| 👨‍🏫 **Formateur** | `TRAINER` | Créateur et gestionnaire de contenu pédagogique | Créer et gérer formations, cours, examens |
| 👑 **Administrateur** | `ADMIN` | Gestionnaire de la plateforme | Accès complet à toutes les fonctionnalités |
| 💼 **RH Entreprise** | `RH_COMPANY` | Responsable RH d'une entreprise partenaire | Gérer les employés et leurs formations |
| 🏢 **RH SMARTEK** | `RH_SMARTEK` | Responsable RH interne SMARTEK | Gérer les utilisateurs et valider les compétences |
| 🤝 **Partenaire** | `PARTNER` | Entreprise ou organisation partenaire | Proposer des offres d'emploi |
| 💰 **Sponsor** | `SPONSOR` | Financeur de formations | Gérer les budgets et sponsorships |

---

## 📊 Détail des permissions par rôle

### 🎓 LEARNER (Apprenant)

#### Accès Frontend
- **Navbar**: Analytics (My Trainings, My Courses, My Exams), Planning, Events
- **Routes**:
  - `/learner-courses` - Mes cours
  - `/learner-training` - Mes formations
  - `/learner-planning` - Mon planning
  - `/learner-events` - Mes événements
  - `/learner-exams` - Mes examens

#### Permissions par Microservice

**📚 Training Service**
- ✅ Consulter toutes les formations disponibles
- ✅ S'inscrire à une formation
- ✅ Voir ses inscriptions
- ✅ Voir sa progression
- ✅ Consulter ses statistiques de formation

**📖 Course Service**
- ✅ Consulter les cours d'une formation
- ✅ Voir le contenu des cours (PDF, vidéos)
- ✅ Marquer un cours comme terminé
- ✅ Participer aux sessions live
- ✅ Consulter ses statistiques de cours

**📝 Exam Service**
- ✅ Consulter les examens disponibles
- ✅ S'inscrire à un examen
- ✅ Passer un examen
- ✅ Voir ses résultats d'examens
- ✅ Consulter ses statistiques d'examens

**📅 Planning Service**
- ✅ Consulter les plannings publiés
- ✅ Voir les événements planifiés

**🎉 Event Service**
- ✅ Consulter les événements disponibles
- ✅ S'inscrire à un événement (physique/online)
- ✅ Voir ses inscriptions
- ✅ Annuler une inscription
- ✅ Payer pour un événement payant

**🏆 Certification & Badge Service**
- ✅ Consulter ses certifications obtenues
- ✅ Consulter ses badges obtenus
- ✅ Télécharger ses certificats
- ✅ Vérifier l'authenticité d'un certificat

**💼 Offers Service**
- ✅ Consulter les offres d'emploi
- ✅ Postuler à une offre
- ✅ Voir ses candidatures
- ✅ Sauvegarder des offres

**🎯 Skill Evidence Service**
- ✅ Soumettre des preuves de compétences
- ✅ Consulter ses preuves soumises
- ✅ Voir le statut de validation

**📚 Learning Service**
- ✅ Consulter son parcours d'apprentissage
- ✅ Définir ses préférences d'apprentissage

---

### 👨‍🏫 TRAINER (Formateur)

#### Accès Frontend
- **Navbar**: Analytics (My Trainings, My Courses, My Exams), Learner Analytics, Planning, Events
- **Routes**:
  - `/trainer/courses` - Gestion des cours
  - `/trainer/training-management` - Gestion des formations
  - `/trainer/planning` - Gestion du planning
  - `/trainer/events` - Gestion des événements
  - `/trainer/exams` - Gestion des examens
  - `/trainer/learner-analytics` - Analytics des apprenants

#### Permissions par Microservice

**📚 Training Service**
- ✅ Créer une formation
- ✅ Modifier ses formations
- ✅ Supprimer ses formations
- ✅ Consulter toutes les formations
- ✅ Voir les inscriptions à ses formations
- ✅ Consulter les analytics des apprenants

**📖 Course Service**
- ✅ Créer un cours
- ✅ Modifier un cours
- ✅ Supprimer un cours
- ✅ Uploader du contenu (PDF, vidéos)
- ✅ Créer des chapitres
- ✅ Créer des sessions live
- ✅ Gérer les sessions live
- ✅ Consulter les statistiques des cours

**📝 Exam Service**
- ✅ Créer un examen
- ✅ Modifier un examen
- ✅ Supprimer un examen
- ✅ Créer des questions/exercices
- ✅ Corriger les examens
- ✅ Voir les résultats des apprenants
- ✅ Consulter les statistiques d'examens

**📅 Planning Service**
- ✅ Créer un planning
- ✅ Modifier un planning
- ✅ Supprimer un planning
- ✅ Publier/Dépublier un planning
- ✅ Publier/Dépublier une semaine complète

**🎉 Event Service**
- ✅ Créer un événement
- ✅ Modifier un événement
- ✅ Supprimer un événement
- ✅ Changer le statut d'un événement
- ✅ Voir les inscriptions
- ✅ Consulter les revenus d'un événement

**🏆 Certification & Badge Service**
- ✅ Créer un template de badge
- ✅ Modifier un template de badge
- ✅ Supprimer un template de badge
- ✅ Créer un template de certification
- ✅ Attribuer des badges
- ✅ Attribuer des certifications
- ✅ Consulter les statistiques

**💼 Offers Service**
- ✅ Consulter les offres
- ✅ Voir les candidatures (si créateur de l'offre)

**🎯 Skill Evidence Service**
- ✅ Consulter les preuves soumises
- ✅ Recevoir des notifications

**📚 Learning Service**
- ✅ Consulter les parcours d'apprentissage
- ✅ Créer des parcours personnalisés

---

### 👑 ADMIN (Administrateur)

#### Accès Frontend
- **Dashboard**: `/dashboard` - Dashboard administrateur complet
- **Accès**: Toutes les fonctionnalités de tous les rôles

#### Permissions par Microservice

**🔐 Auth Service**
- ✅ Gérer tous les utilisateurs
- ✅ Créer/Modifier/Supprimer des utilisateurs
- ✅ Changer les rôles
- ✅ Réinitialiser les mots de passe

**📚 Training Service**
- ✅ Accès complet à toutes les formations
- ✅ Créer/Modifier/Supprimer n'importe quelle formation
- ✅ Gérer toutes les inscriptions
- ✅ Consulter toutes les analytics

**📖 Course Service**
- ✅ Accès complet à tous les cours
- ✅ Créer/Modifier/Supprimer n'importe quel cours
- ✅ Gérer tout le contenu
- ✅ Gérer toutes les sessions live

**📝 Exam Service**
- ✅ Accès complet à tous les examens
- ✅ Créer/Modifier/Supprimer n'importe quel examen
- ✅ Voir tous les résultats
- ✅ Corriger tous les examens

**📅 Planning Service**
- ✅ Accès complet à tous les plannings
- ✅ Créer/Modifier/Supprimer n'importe quel planning

**🎉 Event Service**
- ✅ Accès complet à tous les événements
- ✅ Créer/Modifier/Supprimer n'importe quel événement
- ✅ Gérer toutes les inscriptions
- ✅ Consulter tous les revenus

**🏆 Certification & Badge Service**
- ✅ Accès complet à tous les templates
- ✅ Créer/Modifier/Supprimer n'importe quel template
- ✅ Attribuer/Révoquer des certifications et badges
- ✅ Gérer les renouvellements
- ✅ Consulter toutes les statistiques

**💼 Offers Service**
- ✅ Accès complet à toutes les offres
- ✅ Créer/Modifier/Supprimer n'importe quelle offre
- ✅ Gérer toutes les candidatures
- ✅ Gérer les entretiens

**🎯 Skill Evidence Service**
- ✅ Consulter toutes les preuves
- ✅ Approuver/Rejeter des preuves
- ✅ Réviser des preuves

**📚 Learning Service**
- ✅ Accès complet à tous les parcours
- ✅ Créer/Modifier/Supprimer des parcours

**💰 Sponsor Service**
- ✅ Consulter tous les sponsors
- ✅ Gérer tous les budgets
- ✅ Gérer tous les contrats

---

### 💼 RH_COMPANY (RH Entreprise)

#### Accès Frontend
- **Dashboard**: Gestion des employés de l'entreprise
- **Routes**: Gestion des formations des employés

#### Permissions par Microservice

**🔐 Auth Service**
- ✅ Consulter les employés de son entreprise
- ✅ Créer des comptes employés
- ✅ Modifier les profils employés

**📚 Training Service**
- ✅ Inscrire des employés à des formations
- ✅ Consulter la progression des employés
- ✅ Voir les statistiques de formation de l'entreprise

**📝 Exam Service**
- ✅ Consulter les résultats des employés
- ✅ Voir les statistiques d'examens de l'entreprise

**🏆 Certification & Badge Service**
- ✅ Consulter les certifications des employés
- ✅ Consulter les badges des employés

**💼 Offers Service**
- ✅ Créer des offres d'emploi
- ✅ Modifier ses offres
- ✅ Supprimer ses offres
- ✅ Gérer les candidatures
- ✅ Planifier des entretiens

**🎯 Skill Evidence Service**
- ✅ Consulter les preuves des employés

---

### 🏢 RH_SMARTEK (RH SMARTEK)

#### Accès Frontend
- **Dashboard**: Gestion globale des utilisateurs
- **Routes**: Validation des compétences

#### Permissions par Microservice

**🔐 Auth Service**
- ✅ Consulter tous les utilisateurs
- ✅ Créer des comptes
- ✅ Modifier les profils
- ✅ Gérer les rôles (sauf ADMIN)

**📚 Training Service**
- ✅ Consulter toutes les formations
- ✅ Voir toutes les inscriptions
- ✅ Consulter toutes les analytics

**📝 Exam Service**
- ✅ Consulter tous les examens
- ✅ Voir tous les résultats

**🏆 Certification & Badge Service**
- ✅ Consulter toutes les certifications
- ✅ Consulter tous les badges
- ✅ Valider des certifications

**💼 Offers Service**
- ✅ Consulter toutes les offres
- ✅ Modérer les offres

**🎯 Skill Evidence Service**
- ✅ Consulter toutes les preuves
- ✅ Approuver/Rejeter des preuves
- ✅ Réviser des preuves

---

### 🤝 PARTNER (Partenaire)

#### Accès Frontend
- **Dashboard**: Gestion des offres et candidatures
- **Routes**: Offres d'emploi

#### Permissions par Microservice

**💼 Offers Service**
- ✅ Créer des offres d'emploi
- ✅ Modifier ses offres
- ✅ Supprimer ses offres
- ✅ Consulter les candidatures
- ✅ Gérer les entretiens
- ✅ Accepter/Rejeter des candidatures

**📚 Training Service**
- ✅ Consulter les formations disponibles
- ✅ Recommander des formations

**🏆 Certification & Badge Service**
- ✅ Consulter les certifications des candidats
- ✅ Vérifier l'authenticité des certificats

---

### 💰 SPONSOR (Sponsor)

#### Accès Frontend
- **Dashboard**: `/sponsor` - Dashboard sponsor
- **Routes**: Gestion des budgets et sponsorships

#### Permissions par Microservice

**💰 Sponsor Service**
- ✅ Créer un sponsorship
- ✅ Modifier ses sponsorships
- ✅ Supprimer ses sponsorships
- ✅ Gérer son budget
- ✅ Consulter ses contrats
- ✅ Voir les statistiques de ses sponsorships

**📚 Training Service**
- ✅ Consulter les formations sponsorisées
- ✅ Voir les statistiques des formations sponsorisées

**🎉 Event Service**
- ✅ Sponsoriser des événements
- ✅ Consulter les événements sponsorisés

**🏆 Certification & Badge Service**
- ✅ Sponsoriser des certifications
- ✅ Consulter les certifications sponsorisées

---

## 🗂️ Matrice d'accès par microservice

### 📚 Training Service (Port 8084)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter formations | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Créer formation | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Modifier formation | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Supprimer formation | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |
| S'inscrire | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Inscrire employés | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Voir analytics | ✅ (ses) | ✅ (ses) | ✅ | ✅ (entreprise) | ✅ | ❌ | ✅ (sponsorisées) |

### 📖 Course Service (Port 8086)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter cours | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| Créer cours | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Modifier cours | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Uploader contenu | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Marquer terminé | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Sessions live | ✅ (participer) | ✅ (créer/gérer) | ✅ | ❌ | ❌ | ❌ | ❌ |

### 📝 Exam Service (Port 8087)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter examens | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| Créer examen | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Passer examen | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Corriger examen | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Voir résultats | ✅ (ses) | ✅ (ses examens) | ✅ | ✅ (employés) | ✅ | ❌ | ❌ |

### 🎉 Event Service (Port 8082)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter événements | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Créer événement | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| S'inscrire | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Gérer inscriptions | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Paiement | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Sponsoriser | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

### 📅 Planning Service (Port 8083)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter planning | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| Créer planning | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Publier planning | ❌ | ✅ (ses) | ✅ | ❌ | ❌ | ❌ | ❌ |

### 🏆 Certification & Badge Service (Port 8094)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter certifications | ✅ (ses) | ✅ | ✅ | ✅ (employés) | ✅ | ✅ (candidats) | ✅ (sponsorisées) |
| Créer template | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Attribuer certification | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Télécharger certificat | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Vérifier authenticité | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### 💼 Offers Service (Port 8085)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter offres | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Créer offre | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ |
| Postuler | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Gérer candidatures | ❌ | ❌ | ✅ | ✅ (ses offres) | ✅ | ✅ (ses offres) | ❌ |
| Planifier entretiens | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ |

### 🎯 Skill Evidence Service (Port 8091)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Soumettre preuve | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Consulter preuves | ✅ (ses) | ✅ | ✅ | ✅ (employés) | ✅ | ❌ | ❌ |
| Approuver/Rejeter | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Réviser | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |

### 📚 Learning Service (Port 8092)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Consulter parcours | ✅ (ses) | ✅ | ✅ | ✅ (employés) | ✅ | ❌ | ❌ |
| Créer parcours | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Préférences apprentissage | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |

### 💰 Sponsor Service (Port 8093)

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Créer sponsorship | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Gérer budget | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Consulter contrats | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Statistiques | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |

---

## 🔒 Sécurité et Authentification

### JWT Token
- Tous les apprenants doivent être authentifiés via JWT
- Le token contient: `userId`, `email`, `role`, `firstName`
- Durée de validité: 24 heures

### API Gateway (Port 8080)
- Point d'entrée unique pour tous les microservices
- Gestion CORS centralisée
- Routage vers les microservices appropriés

### Validation des rôles
- Validation côté backend via `@PreAuthorize`
- Validation côté frontend via guards Angular
- Vérification périodique de la validité du token (toutes les 30 secondes)

---

## 📝 Notes importantes

1. **Principe du moindre privilège**: Chaque rôle n'a accès qu'aux fonctionnalités nécessaires
2. **Séparation des données**: Les utilisateurs ne peuvent accéder qu'à leurs propres données (sauf ADMIN et RH)
3. **Audit trail**: Toutes les actions sensibles sont loggées
4. **Validation multi-niveaux**: Frontend + API Gateway + Microservice
5. **Évolutivité**: Nouveaux rôles et permissions peuvent être ajoutés facilement

---

**Dernière mise à jour**: 13 Mai 2026
**Version**: 1.0
