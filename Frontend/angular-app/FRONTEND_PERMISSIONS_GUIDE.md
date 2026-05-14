# 🔐 Guide des Permissions Frontend - SMARTEK

## 📋 Vue d'ensemble

Le système de permissions frontend utilise:
- **Guards** pour protéger les routes
- **Directives** pour masquer/afficher des éléments selon le rôle
- **Service** pour vérifier les permissions dans le code TypeScript

---

## 🛡️ Guards

### 1. AuthGuard
Vérifie si l'utilisateur est authentifié.

```typescript
// Dans app.routes.ts
{ 
  path: 'learner-courses', 
  component: LearnerCoursesComponent, 
  canActivate: [authGuard]
}
```

### 2. RoleGuard
Vérifie si l'utilisateur a le bon rôle.

```typescript
// Dans app.routes.ts
{ 
  path: 'trainer/courses', 
  component: TrainerCoursesComponent, 
  canActivate: [authGuard, roleGuard],
  data: { roles: ['TRAINER', 'ADMIN'] }
}
```

---

## 🎯 Directive HasRole

Masque/affiche des éléments HTML selon le rôle de l'utilisateur.

### Utilisation dans les templates

```html
<!-- Bouton visible uniquement pour TRAINER et ADMIN -->
<button *appHasRole="['TRAINER', 'ADMIN']" (click)="createCourse()">
  Créer un cours
</button>

<!-- Section visible uniquement pour ADMIN -->
<div *appHasRole="'ADMIN'">
  <h2>Administration</h2>
  <!-- Contenu admin -->
</div>

<!-- Bouton visible pour LEARNER -->
<button *appHasRole="'LEARNER'" (click)="enrollInCourse()">
  S'inscrire
</button>
```

### Import dans le composant

```typescript
import { HasRoleDirective } from '../../core/directives/has-role.directive';

@Component({
  selector: 'app-my-component',
  standalone: true,
  imports: [CommonModule, HasRoleDirective],
  templateUrl: './my-component.html'
})
export class MyComponent { }
```

---

## 🔧 PermissionService

Service pour vérifier les permissions dans le code TypeScript.

### Injection du service

```typescript
import { PermissionService } from '../../core/services/permission.service';

export class MyComponent {
  constructor(private permissionService: PermissionService) {}
}
```

### Méthodes disponibles

#### Vérification de rôles spécifiques

```typescript
// Vérifier un rôle unique
if (this.permissionService.hasRole('TRAINER')) {
  // Code pour TRAINER
}

// Vérifier plusieurs rôles
if (this.permissionService.hasRole(['TRAINER', 'ADMIN'])) {
  // Code pour TRAINER ou ADMIN
}
```

#### Méthodes de commodité

```typescript
// Vérifier si peut créer/modifier/supprimer
if (this.permissionService.canCreate()) {
  this.showCreateButton = true;
}

if (this.permissionService.canEdit()) {
  this.enableEditMode();
}

if (this.permissionService.canDelete()) {
  this.showDeleteButton = true;
}

// Vérifier le type d'utilisateur
if (this.permissionService.isLearner()) {
  this.loadLearnerData();
}

if (this.permissionService.isTrainer()) {
  this.loadTrainerData();
}

if (this.permissionService.isAdmin()) {
  this.loadAdminData();
}

// Permissions spécifiques
if (this.permissionService.canApproveSkills()) {
  this.showApprovalButtons = true;
}

if (this.permissionService.canViewAnalytics()) {
  this.loadAnalytics();
}
```

---

## 📝 Exemples complets

### Exemple 1: Composant avec boutons conditionnels

```typescript
// training.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { PermissionService } from '../../core/services/permission.service';

@Component({
  selector: 'app-training',
  standalone: true,
  imports: [CommonModule, HasRoleDirective],
  template: `
    <div class="training-container">
      <h1>Formations</h1>
      
      <!-- Bouton créer (TRAINER et ADMIN uniquement) -->
      <button *appHasRole="['TRAINER', 'ADMIN']" 
              (click)="createTraining()"
              class="btn-primary">
        Créer une formation
      </button>
      
      <!-- Liste des formations -->
      <div *ngFor="let training of trainings" class="training-card">
        <h3>{{ training.title }}</h3>
        
        <!-- Boutons d'action (TRAINER et ADMIN uniquement) -->
        <div *appHasRole="['TRAINER', 'ADMIN']" class="actions">
          <button (click)="editTraining(training)">Modifier</button>
          <button (click)="deleteTraining(training)">Supprimer</button>
        </div>
        
        <!-- Bouton inscription (LEARNER uniquement) -->
        <button *appHasRole="'LEARNER'" 
                (click)="enrollInTraining(training)">
          S'inscrire
        </button>
      </div>
    </div>
  `
})
export class TrainingComponent implements OnInit {
  trainings: any[] = [];
  canManageTrainings = false;

  constructor(private permissionService: PermissionService) {}

  ngOnInit() {
    this.loadTrainings();
    this.canManageTrainings = this.permissionService.canCreate();
  }

  createTraining() {
    if (!this.permissionService.canCreate()) {
      alert('Vous n\'avez pas la permission de créer une formation');
      return;
    }
    // Logique de création
  }

  editTraining(training: any) {
    if (!this.permissionService.canEdit()) {
      alert('Vous n\'avez pas la permission de modifier');
      return;
    }
    // Logique de modification
  }

  deleteTraining(training: any) {
    if (!this.permissionService.canDelete()) {
      alert('Vous n\'avez pas la permission de supprimer');
      return;
    }
    // Logique de suppression
  }

  enrollInTraining(training: any) {
    if (!this.permissionService.isLearner()) {
      alert('Seuls les apprenants peuvent s\'inscrire');
      return;
    }
    // Logique d'inscription
  }

  loadTrainings() {
    // Charger les formations
  }
}
```

### Exemple 2: Navigation conditionnelle

```typescript
// navbar.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HasRoleDirective } from '../../core/directives/has-role.directive';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, HasRoleDirective],
  template: `
    <nav>
      <!-- Liens pour tous les utilisateurs authentifiés -->
      <a routerLink="/">Accueil</a>
      
      <!-- Liens LEARNER -->
      <ng-container *appHasRole="'LEARNER'">
        <a routerLink="/learner-courses">Mes Cours</a>
        <a routerLink="/learner-training">Mes Formations</a>
        <a routerLink="/learner-exams">Mes Examens</a>
      </ng-container>
      
      <!-- Liens TRAINER -->
      <ng-container *appHasRole="'TRAINER'">
        <a routerLink="/trainer/courses">Gérer les Cours</a>
        <a routerLink="/trainer/training-management">Gérer les Formations</a>
        <a routerLink="/trainer/learner-analytics">Analytics</a>
      </ng-container>
      
      <!-- Liens ADMIN -->
      <ng-container *appHasRole="'ADMIN'">
        <a routerLink="/dashboard">Dashboard Admin</a>
        <a routerLink="/dashboard/sponsors">Sponsors</a>
      </ng-container>
      
      <!-- Liens SPONSOR -->
      <ng-container *appHasRole="'SPONSOR'">
        <a routerLink="/sponsor">Mon Dashboard</a>
      </ng-container>
    </nav>
  `
})
export class NavbarComponent { }
```

### Exemple 3: Formulaire avec permissions

```typescript
// course-form.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { PermissionService } from '../../core/services/permission.service';

@Component({
  selector: 'app-course-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HasRoleDirective],
  template: `
    <form [formGroup]="courseForm" (ngSubmit)="onSubmit()">
      <input formControlName="title" placeholder="Titre du cours">
      <textarea formControlName="description" placeholder="Description"></textarea>
      
      <!-- Champs avancés (ADMIN uniquement) -->
      <div *appHasRole="'ADMIN'">
        <input formControlName="price" type="number" placeholder="Prix">
        <select formControlName="status">
          <option value="DRAFT">Brouillon</option>
          <option value="PUBLISHED">Publié</option>
          <option value="ARCHIVED">Archivé</option>
        </select>
      </div>
      
      <button type="submit" [disabled]="!canSubmit">
        {{ isEditMode ? 'Modifier' : 'Créer' }}
      </button>
    </form>
  `
})
export class CourseFormComponent implements OnInit {
  courseForm: FormGroup;
  isEditMode = false;
  canSubmit = false;

  constructor(
    private fb: FormBuilder,
    private permissionService: PermissionService
  ) {
    this.courseForm = this.fb.group({
      title: [''],
      description: [''],
      price: [0],
      status: ['DRAFT']
    });
  }

  ngOnInit() {
    this.canSubmit = this.permissionService.canCreate() || this.permissionService.canEdit();
    
    // Désactiver les champs si pas de permission
    if (!this.canSubmit) {
      this.courseForm.disable();
    }
  }

  onSubmit() {
    if (!this.canSubmit) {
      alert('Vous n\'avez pas la permission');
      return;
    }
    
    // Logique de soumission
  }
}
```

---

## 🎨 Matrice des permissions par rôle

| Fonctionnalité | LEARNER | TRAINER | ADMIN | RH_COMPANY | RH_SMARTEK | PARTNER | SPONSOR |
|----------------|---------|---------|-------|------------|------------|---------|---------|
| Créer formation | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Modifier formation | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Supprimer formation | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| S'inscrire formation | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Voir analytics | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Approuver compétences | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Gérer sponsors | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Créer offres emploi | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ |
| Postuler offres | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 🚀 Bonnes pratiques

### 1. Toujours vérifier côté backend ET frontend
```typescript
// ❌ Mauvais - Vérification uniquement frontend
deleteTraining(id: number) {
  this.trainingService.delete(id).subscribe();
}

// ✅ Bon - Vérification frontend + backend sécurisé
deleteTraining(id: number) {
  if (!this.permissionService.canDelete()) {
    alert('Permission refusée');
    return;
  }
  this.trainingService.delete(id).subscribe({
    error: (err) => {
      if (err.status === 403) {
        alert('Vous n\'avez pas la permission');
      }
    }
  });
}
```

### 2. Utiliser la directive pour l'UI, le service pour la logique
```typescript
// Dans le template - Masquer le bouton
<button *appHasRole="['TRAINER', 'ADMIN']" (click)="create()">
  Créer
</button>

// Dans le TypeScript - Vérifier avant l'action
create() {
  if (!this.permissionService.canCreate()) {
    return;
  }
  // Logique de création
}
```

### 3. Gérer les erreurs 403 du backend
```typescript
this.http.post('/api/trainings', data).subscribe({
  next: (response) => {
    // Succès
  },
  error: (error) => {
    if (error.status === 403) {
      alert('Vous n\'avez pas la permission d\'effectuer cette action');
      this.router.navigate(['/']);
    }
  }
});
```

---

## 📚 Ressources

- **Guards**: `Frontend/angular-app/src/app/core/guards/`
- **Directive**: `Frontend/angular-app/src/app/core/directives/has-role.directive.ts`
- **Service**: `Frontend/angular-app/src/app/core/services/permission.service.ts`
- **Routes**: `Frontend/angular-app/src/app/app.routes.ts`

---

**Dernière mise à jour**: 13 Mai 2026
