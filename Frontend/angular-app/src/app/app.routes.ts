import { Routes } from '@angular/router';
import { HomePageComponent } from './features/home/home-page/home-page.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout/dashboard-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page/dashboard-page.component';
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
  { path: 'test-offers', component: JobOffersComponent }, // Route de test sans guards
  { path: 'test-offers-learner', component: JobOffersLearnerComponent }, // Route de test pour learner
  { path: 'test-interviews-learner', component: InterviewsLearnerComponent }, // Route de test pour entretiens learner
  // Frontoffice routes for learners
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
    path: 'learner/skill-evidence',
    loadComponent: () => import('./features/learner/skill-evidence/learner-skill-evidence.component').then(m => m.LearnerSkillEvidenceComponent),
    canActivate: [authGuard]
  },
  {
    path: 'learner/skill-evidence-analytics',
    loadComponent: () => import('./features/learner/skill-evidence-analytics/learner-skill-evidence-analytics.component').then(m => m.LearnerSkillEvidenceAnalyticsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'learner/learning-path',
    loadComponent: () => import('./features/learner/learning-path/learner-learning-path.component').then(m => m.LearnerLearningPathComponent),
    canActivate: [authGuard]
  },
  {
    path: 'learner/learning-style',
    loadComponent: () => import('./features/learner/learning-style/learner-learning-style.component').then(m => m.LearnerLearningStyleComponent),
    canActivate: [authGuard]
  },
  {
    path: 'learner/learning-style-quiz',
    loadComponent: () => import('./features/learner/learning-style-quiz/learner-learning-style-quiz.component').then(m => m.LearnerLearningStyleQuizComponent),
    canActivate: [authGuard]
  },
  
  // Trainer routes (sans layout, utilise le header du site)
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

  // RH Smartek routes with layout
  {
    path: 'rh-smartek',
    loadComponent: () => import('./features/rh-smartek/rh-smartek-layout/rh-smartek-layout.component').then(m => m.RhSmartekLayoutComponent),
    canActivate: [authGuard, permissionGuard],
    data: { roles: [Role.RH_SMARTEK] },
    children: [
      {
        path: '',
        redirectTo: 'certifications',
        pathMatch: 'full'
      },
      {
        path: 'certifications',
        loadComponent: () => import('./features/rh-smartek/certifications/certifications.component').then(m => m.RhCertificationsComponent)
      },
      {
        path: 'courses',
        loadComponent: () => import('./features/rh-smartek/courses/courses.component').then(m => m.RhCoursesComponent)
      },
      {
        path: 'exams',
        loadComponent: () => import('./features/rh-smartek/exams/exams.component').then(m => m.RhExamsComponent)
      },
      {
        path: 'interviews',
        loadComponent: () => import('./features/rh-smartek/interviews/interviews.component').then(m => m.InterviewsComponent)
      },
      {
        path: 'schedule',
        loadComponent: () => import('./features/rh-smartek/schedule/schedule.component').then(m => m.ScheduleComponent)
      },
      {
        path: 'events',
        loadComponent: () => import('./features/rh-smartek/events/events.component').then(m => m.RhEventsComponent)
      }
    ]
  },

  // RH Company routes with layout
  {
    path: 'rh-company',
    loadComponent: () => import('./features/rh-company/rh-company-layout/rh-company-layout.component').then(m => m.RhCompanyLayoutComponent),
    canActivate: [authGuard, permissionGuard],
    data: { roles: [Role.RH_COMPANY] },
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/rh-company/dashboard/rh-company-dashboard.component').then(m => m.RhCompanyDashboardComponent)
      },
      {
        path: 'offers',
        component: JobOffersComponent
      },
      {
        path: 'participation',
        loadComponent: () => import('./features/rh-company/participation/participation.component').then(m => m.CompanyParticipationComponent)
      }
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
        component: DashboardPageComponent
      },
      // Course Management - ADMIN only
      { 
        path: 'courses',
        loadComponent: () => import('./features/dashboard/course-management/course-management.component').then(m => m.CourseManagementComponent)
      },
      // Chapter Management - ADMIN only
      { 
        path: 'courses/:courseId/chapters',
        loadComponent: () => import('./features/dashboard/chapter-management/chapter-management.component').then(m => m.ChapterManagementComponent)
      },
      // My Courses - ADMIN only
      { 
        path: 'my-courses', 
        loadComponent: () => import('./features/dashboard/my-courses/my-courses.component').then(m => m.MyCoursesComponent)
      },
      // Exam Management - ADMIN only
      { 
        path: 'exams', 
        loadComponent: () => import('./features/dashboard/exam-management/exam-management.component').then(m => m.ExamManagementComponent)
      },
      // My Exams - ADMIN only
      { 
        path: 'my-exams', 
        loadComponent: () => import('./features/dashboard/my-exams/my-exams.component').then(m => m.MyExamsComponent)
      },
      // Training Management - ADMIN only
      { 
        path: 'training', 
        loadComponent: () => import('./features/dashboard/training-management/training-management.component').then(m => m.TrainingManagementComponent)
      },
      // My Training - ADMIN only
      { 
        path: 'my-training',
        loadComponent: () => import('./features/dashboard/my-training/my-training.component').then(m => m.MyTrainingComponent)
      },
      // Certifications & Badges - ADMIN only
      { 
        path: 'certifications', 
        component: DashboardPageComponent
      },
      // My Certifications - ADMIN only
      { 
        path: 'my-certifications', 
        component: DashboardPageComponent
      },
      // Skill Evidence - ADMIN only
      { 
        path: 'skill-evidence', 
        loadComponent: () => import('./features/dashboard/skill-evidence-management/skill-evidence-management.component').then(m => m.SkillEvidenceManagementComponent)
      },
      // Skill Evidence Analytics - ADMIN only
      {
        path: 'skill-evidence-analytics',
        loadComponent: () => import('./features/dashboard/skill-evidence-analytics/skill-evidence-analytics.component').then(m => m.SkillEvidenceAnalyticsComponent)
      },
      // Interview Management - ADMIN only
      { 
        path: 'interviews', 
        component: DashboardPageComponent
      },
      // Job Offers - ADMIN only
      { 
        path: 'job-offers', 
        component: JobOffersRouterComponent
      },
      // Planning - ADMIN only
      { 
        path: 'planning', 
        component: PlanningComponent
      },
      // Event Management - ADMIN only
      { 
        path: 'events', 
        component: DashboardPageComponent
      },
      // User Management - ADMIN only
      { 
        path: 'users', 
        component: DashboardPageComponent
      },
      // Company Management - ADMIN only
      { 
        path: 'companies', 
        component: DashboardPageComponent
      },
      // Sponsor Management - ADMIN only
      { 
        path: 'sponsors', 
        component: DashboardPageComponent
      },
      // Contact Management - ADMIN only
      { 
        path: 'contacts', 
        component: DashboardPageComponent
      },
      // Participation - ADMIN only
      { 
        path: 'participation', 
        component: DashboardPageComponent
      },
      // Learning Paths - ADMIN only
      { 
        path: 'learning-paths', 
        loadComponent: () => import('./features/dashboard/learning-path-management/learning-path-management.component').then(m => m.LearningPathManagementComponent)
      },
      // System Settings - ADMIN only
      { 
        path: 'settings', 
        component: DashboardPageComponent
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
