import { Routes } from '@angular/router';
import { HomePageComponent } from './features/home/home-page/home-page.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout/dashboard-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page/dashboard-page.component';
import { MyCertificationsComponent } from './features/certifications-badges/my-certifications/my-certifications.component';
import { MyBadgesComponent } from './features/certifications-badges/my-badges/my-badges.component';
import { CertificateViewerComponent } from './features/certifications-badges/certificate-viewer/certificate-viewer.component';
import { JobOffersRouterComponent } from './features/dashboard/job-offers-router/job-offers-router.component';
import { JobOffersComponent } from './features/dashboard/job-offers/job-offers.component';
import { JobOffersLearnerComponent } from './features/learner/job-offers/job-offers-learner.component';
import { InterviewsLearnerComponent } from './features/learner/interviews/interviews-learner.component';
import { PlanningComponent } from './features/dashboard/planning/planning.component';
import { SignUpComponent } from './features/auth/sign-up/sign-up.component';
import { SignInComponent } from './features/auth/sign-in/sign-in.component';
import { SettingsComponent } from './features/settings/settings.component';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';
import { Permission } from './core/enums/permission.enum';
import { Role } from './core/enums/role.enum';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'auth/sign-in', component: SignInComponent },
  { path: 'auth/sign-up', component: SignUpComponent },
  { path: 'settings', component: SettingsComponent, canActivate: [authGuard] },
  { path: 'test-offers', component: JobOffersComponent },
  { path: 'test-offers-learner', component: JobOffersLearnerComponent },
  { path: 'test-interviews-learner', component: InterviewsLearnerComponent },
  { 
    path: 'learner-training', 
    loadComponent: () => import('./features/learner/training/learner-training.component').then(m => m.LearnerTrainingComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-courses', 
    loadComponent: () => import('./features/learner/courses/learner-courses.component').then(m => m.LearnerCoursesComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-exams', 
    loadComponent: () => import('./features/learner/exams/learner-exams.component').then(m => m.LearnerExamsComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-exams/take/:id', 
    loadComponent: () => import('./features/learner/exam-take/exam-take.component').then(m => m.ExamTakeComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-exams/result/:id', 
    loadComponent: () => import('./features/learner/exam-result/exam-result.component').then(m => m.ExamResultComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-performance', 
    loadComponent: () => import('./features/learner/performance/performance.component').then(m => m.PerformanceComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner-job-offers', 
    component: JobOffersLearnerComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'learner-interviews', 
    component: InterviewsLearnerComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'learner/planning', 
    loadComponent: () => import('./features/learner/planning/learner-planning.component').then(m => m.LearnerPlanningComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'learner/events', 
    loadComponent: () => import('./features/learner/events/learner-events.component').then(m => m.LearnerEventsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'learner/certifications',
    component: MyCertificationsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'learner/badges',
    component: MyBadgesComponent,
    canActivate: [authGuard]
  },
  {
    path: 'learner/certificate-viewer/:id',
    component: CertificateViewerComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'trainer/courses',
    loadComponent: () => import('./features/trainer/courses/trainer-courses.component').then(m => m.TrainerCoursesComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/training-management',
    loadComponent: () => import('./features/trainer/training-management/trainer-training-management.component').then(m => m.TrainerTrainingManagementComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/exams',
    loadComponent: () => import('./features/trainer/exams/trainer-exams.component').then(m => m.TrainerExamsComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/skill-evidence',
    loadComponent: () => import('./features/trainer/skill-evidence/trainer-skill-evidence.component').then(m => m.TrainerSkillEvidenceComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/badge-management',
    loadComponent: () => import('./features/trainer/badge-management/trainer-badge-management.component').then(m => m.TrainerBadgeManagementComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/learner-analytics',
    loadComponent: () => import('./features/trainer/learner-analytics/learner-analytics.component').then(m => m.LearnerAnalyticsComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/planning',
    loadComponent: () => import('./features/trainer/planning/trainer-planning.component').then(m => m.TrainerPlanningComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/events',
    loadComponent: () => import('./features/trainer/events/trainer-events.component').then(m => m.TrainerEventsComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  { 
    path: 'trainer/weekly-planning',
    loadComponent: () => import('./features/trainer/weekly-planning/trainer-weekly-planning.component').then(m => m.TrainerWeeklyPlanningComponent),
    canActivate: [permissionGuard],
    data: { roles: [Role.TRAINER] }
  },
  {
    path: 'rh-smartek',
    loadComponent: () => import('./features/rh-smartek/rh-smartek-layout/rh-smartek-layout.component').then(m => m.RhSmartekLayoutComponent),
    canActivate: [authGuard, permissionGuard],
    data: { roles: [Role.RH_SMARTEK] },
    children: [
      { path: '', redirectTo: 'certifications', pathMatch: 'full' },
      { path: 'certifications', loadComponent: () => import('./features/rh-smartek/certifications/certifications.component').then(m => m.RhCertificationsComponent) },
      { path: 'courses', loadComponent: () => import('./features/rh-smartek/courses/courses.component').then(m => m.RhCoursesComponent) },
      { path: 'exams', loadComponent: () => import('./features/rh-smartek/exams/exams.component').then(m => m.RhExamsComponent) },
      { path: 'interviews', loadComponent: () => import('./features/rh-smartek/interviews/interviews.component').then(m => m.InterviewsComponent) },
      { path: 'schedule', loadComponent: () => import('./features/rh-smartek/schedule/schedule.component').then(m => m.ScheduleComponent) },
      { path: 'events', loadComponent: () => import('./features/rh-smartek/events/events.component').then(m => m.RhEventsComponent) }
    ]
  },
  {
    path: 'rh-company',
    loadComponent: () => import('./features/rh-company/rh-company-layout/rh-company-layout.component').then(m => m.RhCompanyLayoutComponent),
    canActivate: [authGuard, permissionGuard],
    data: { roles: [Role.RH_COMPANY] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/rh-company/dashboard/rh-company-dashboard.component').then(m => m.RhCompanyDashboardComponent) },
      { path: 'offers', loadComponent: () => import('./features/rh-company/offers/rh-company-offers.component').then(m => m.RhCompanyOffersComponent) },
      { path: 'participation', loadComponent: () => import('./features/rh-company/participation/participation.component').then(m => m.CompanyParticipationComponent) }
    ]
  },
  { 
    path: 'dashboard', 
    component: DashboardLayoutComponent,
    canActivate: [authGuard, permissionGuard],
    data: { roles: [Role.ADMIN, Role.RH_SMARTEK] },
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
      { 
        path: 'courses', 
        loadComponent: () => import('./features/dashboard/course-management/course-management.component').then(m => m.CourseManagementComponent)
      },
      { 
        path: 'courses/:courseId/chapters',
        loadComponent: () => import('./features/dashboard/chapter-management/chapter-management.component').then(m => m.ChapterManagementComponent)
      },
      { 
        path: 'my-courses', 
        loadComponent: () => import('./features/dashboard/my-courses/my-courses.component').then(m => m.MyCoursesComponent)
      },
      { 
        path: 'exams', 
        loadComponent: () => import('./features/dashboard/exam-management/exam-management.component').then(m => m.ExamManagementComponent)
      },
      { 
        path: 'my-exams', 
        loadComponent: () => import('./features/dashboard/my-exams/my-exams.component').then(m => m.MyExamsComponent)
      },
      { 
        path: 'training', 
        loadComponent: () => import('./features/dashboard/training-management/training-management.component').then(m => m.TrainingManagementComponent)
      },
      { 
        path: 'my-training',
        loadComponent: () => import('./features/dashboard/my-training/my-training.component').then(m => m.MyTrainingComponent)
      },
      { 
        path: 'badges', 
        loadChildren: () => import('./features/certifications-badges/badges.module').then(m => m.BadgesModule),
        canActivate: [permissionGuard],
        data: { roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN], permissions: [Permission.BADGES_VIEW] }
      },
      { 
        path: 'certifications', 
        loadChildren: () => import('./features/certifications-badges/certifications.module').then(m => m.CertificationsModule),
        canActivate: [permissionGuard],
        data: { roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN], permissions: [Permission.CERTIFICATIONS_VIEW] }
      },
      { 
        path: 'my-certifications', 
        component: MyCertificationsComponent,
        canActivate: [permissionGuard],
        data: { roles: [Role.LEARNER], permissions: [Permission.CERTIFICATIONS_VIEW] }
      },
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
      { 
        path: 'skill-evidence', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.SKILL_EVIDENCE_VIEW, Permission.SKILL_EVIDENCE_VIEW_ALL] }
      },
      { 
        path: 'interviews', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.INTERVIEWS_VIEW, Permission.INTERVIEWS_CREATE] }
      },
      { 
        path: 'job-offers', 
        component: JobOffersRouterComponent
      },
      { 
        path: 'planning', 
        component: PlanningComponent
      },
      { 
        path: 'events', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.EVENTS_VIEW, Permission.EVENTS_CREATE] }
      },
      { 
        path: 'users', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.USERS_VIEW] }
      },
      { 
        path: 'companies', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.COMPANIES_VIEW, Permission.COMPANIES_CREATE] }
      },
      { 
        path: 'sponsors', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.SPONSORS_VIEW] }
      },
      { 
        path: 'contacts', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.CONTACTS_VIEW] }
      },
      { 
        path: 'participation', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.PARTICIPATION_VIEW, Permission.PARTICIPATION_VIEW_ALL] }
      },
      { 
        path: 'learning-paths', 
        component: DashboardPageComponent,
        canActivate: [permissionGuard],
        data: { permissions: [Permission.LEARNING_PATH_VIEW] }
      },
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
