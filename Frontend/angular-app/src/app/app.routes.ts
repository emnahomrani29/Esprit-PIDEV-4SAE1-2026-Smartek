import { Routes } from '@angular/router';
import { HomePageComponent } from './features/home/home-page/home-page.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout/dashboard-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page/dashboard-page.component';
import { MyCertificationsComponent } from './features/certifications-badges/my-certifications/my-certifications.component';
import { MyBadgesComponent } from './features/certifications-badges/my-badges/my-badges.component';
import { CertificateViewerComponent } from './features/certifications-badges/certificate-viewer/certificate-viewer.component';
import { SignUpComponent } from './features/auth/sign-up/sign-up.component';
import { SignInComponent } from './features/auth/sign-in/sign-in.component';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';
import { Permission } from './core/enums/permission.enum';
import { Role } from './core/enums/role.enum';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'auth/sign-in', component: SignInComponent },
  { path: 'auth/sign-up', component: SignUpComponent },
  { 
    path: 'dashboard', 
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { 
        path: '', 
        component: DashboardPageComponent 
      },
      { 
        path: 'profile', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.PROFILE_VIEW] }
      },
      // Course Management - RH_SMARTEK
      { 
        path: 'courses', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.COURSES_VIEW, Permission.COURSES_CREATE] }
      },
      // My Courses - LEARNER
      { 
        path: 'my-courses', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.COURSES_VIEW] }
      },
      // Exam Management - RH_SMARTEK
      { 
        path: 'exams', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.EXAMS_VIEW, Permission.EXAMS_CREATE] }
      },
      // My Exams - LEARNER
      { 
        path: 'my-exams', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.EXAMS_TAKE] }
      },
      // Training Management
      { 
        path: 'training', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.TRAINING_VIEW, Permission.TRAINING_CREATE] }
      },
      // My Training - LEARNER
      { 
        path: 'my-training', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.TRAINING_VIEW] }
      },
      // Badges Management
      { 
        path: 'badges', 
        loadChildren: () => import('./features/certifications-badges/badges.module').then(m => m.BadgesModule),
        canActivate: [permissionGuard],
        data: { roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN], permissions: [Permission.BADGES_VIEW] }
      },
      // Certifications Management
      { 
        path: 'certifications', 
        loadChildren: () => import('./features/certifications-badges/certifications.module').then(m => m.CertificationsModule),
        canActivate: [permissionGuard],
        data: { roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN], permissions: [Permission.CERTIFICATIONS_VIEW] }
      },
      // My Certifications - LEARNER
      { 
        path: 'my-certifications', 
        component: MyCertificationsComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.CERTIFICATIONS_VIEW] }
      },
      // Certificate Viewer - LEARNER
      { 
        path: 'certificate-viewer/:id', 
        component: CertificateViewerComponent,
        canActivate: [authGuard]
      },
      { 
        path: 'my-badges', 
        component: MyBadgesComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.BADGES_VIEW] }
      },
      // Skill Evidence
      { 
        path: 'skill-evidence', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.SKILL_EVIDENCE_VIEW, Permission.SKILL_EVIDENCE_VIEW_ALL] }
      },
      // Interview Management
      { 
        path: 'interviews', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.INTERVIEWS_VIEW, Permission.INTERVIEWS_CREATE] }
      },
      // Job Offers
      { 
        path: 'job-offers', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.JOB_OFFERS_VIEW, Permission.JOB_OFFERS_CREATE] }
      },
      // Planning
      { 
        path: 'planning', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.PLANNING_VIEW, Permission.PLANNING_CREATE] }
      },
      // Event Management
      { 
        path: 'events', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.EVENTS_VIEW, Permission.EVENTS_CREATE] }
      },
      // User Management
      { 
        path: 'users', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.USERS_VIEW] }
      },
      // Company Management
      { 
        path: 'companies', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.COMPANIES_VIEW, Permission.COMPANIES_CREATE] }
      },
      // Sponsor Management
      { 
        path: 'sponsors', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.SPONSORS_VIEW] }
      },
      // Contact Management
      { 
        path: 'contacts', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.CONTACTS_VIEW] }
      },
      // Participation
      { 
        path: 'participation', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.PARTICIPATION_VIEW, Permission.PARTICIPATION_VIEW_ALL] }
      },
      // Learning Paths
      { 
        path: 'learning-paths', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.LEARNING_PATH_VIEW] }
      },
      // System Settings - ADMIN
      { 
        path: 'settings', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.SYSTEM_SETTINGS] }
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
