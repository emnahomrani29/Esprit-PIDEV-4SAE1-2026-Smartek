import { Routes } from '@angular/router';
import { HomePageComponent } from './features/home/home-page/home-page.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout/dashboard-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page/dashboard-page.component';
import { SignUpComponent } from './features/auth/sign-up/sign-up.component';
import { SignInComponent } from './features/auth/sign-in/sign-in.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

// Dashboard components
import { SponsorListComponent } from './features/sponsors/sponsor-list/sponsor-list.component';
import { AddSponsorComponent } from './features/sponsors/add-sponsor/add-sponsor.component';
import { EditSponsorComponent } from './features/sponsors/edit-sponsor/edit-sponsor.component';
import { ContractListComponent } from './features/contracts/contract-list/contract-list.component';
import { AddContractComponent } from './features/contracts/add-contract/add-contract.component';
import { EditContractComponent } from './features/contracts/edit-contract/edit-contract.component';
import { SponsorshipListComponent } from './features/sponsorships/sponsorship-list/sponsorship-list.component';
import { SponsorDashboardComponent } from './features/dashboard/sponsor-dashboard/sponsor-dashboard.component';
import { SponsorLayoutComponent } from './features/dashboard/sponsor-layout/sponsor-layout.component';
import { AdminPendingApprovalsComponent } from './features/dashboard/admin-pending-approvals/admin-pending-approvals.component';
import { SkillEvidenceManagementComponent } from './features/dashboard/skill-evidence-management/skill-evidence-management.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { RhCertificationsComponent } from './features/rh-smartek/certifications/certifications.component';
import { RhCompanyDashboardComponent } from './features/rh-company/dashboard/rh-company-dashboard.component';
import { RhCompanyOffersComponent } from './features/rh-company/offers/rh-company-offers.component';
import { CompanyParticipationComponent } from './features/rh-company/participation/participation.component';

// Learner layouts & components
import { LearnerLayoutComponent } from './features/learner/learner-layout/learner-layout.component';
import { LearnerCoursesComponent } from './features/learner/courses/learner-courses.component';
import { LearnerTrainingComponent } from './features/learner/training/learner-training.component';
import { LearnerPlanningComponent } from './features/learner/planning/learner-planning.component';
import { LearnerEventsComponent } from './features/learner/events/learner-events.component';
import { LearnerExamsComponent } from './features/learner/exams/learner-exams.component';

// Trainer layouts & components
import { TrainerLayoutComponent } from './features/trainer/trainer-layout/trainer-layout.component';
import { TrainerCoursesComponent } from './features/trainer/courses/trainer-courses.component';
import { TrainerTrainingManagementComponent } from './features/trainer/training-management/trainer-training-management.component';
import { TrainerPlanningComponent } from './features/trainer/planning/trainer-planning.component';
import { TrainerEventsComponent } from './features/trainer/events/trainer-events.component';
import { TrainerExamsComponent } from './features/trainer/exams/trainer-exams.component';
import { LearnerAnalyticsComponent } from './features/trainer/learner-analytics/learner-analytics.component';

// RH Smartek layouts & components
import { RhSmartekLayoutComponent } from './features/rh-smartek/rh-smartek-layout/rh-smartek-layout.component';
import { RhCoursesComponent } from './features/rh-smartek/courses/courses.component';
import { RhEventsComponent } from './features/rh-smartek/events/events.component';
import { RhExamsComponent } from './features/rh-smartek/exams/exams.component';
import { InterviewsComponent } from './features/rh-smartek/interviews/interviews.component';
import { ScheduleComponent } from './features/rh-smartek/schedule/schedule.component';

// Partner layouts & components
import { PartnerLayoutComponent } from './features/partner/partner-layout/partner-layout.component';
import { PartnerEventsComponent } from './features/partner/events/events.component';
import { SponsorshipComponent } from './features/partner/sponsorship/sponsorship.component';

export const routes: Routes = [
  // ─── PUBLIC ────────────────────────────────────────────────────────────────
  { path: '', component: HomePageComponent },
  { path: 'auth/sign-in', component: SignInComponent },
  { path: 'auth/sign-up', component: SignUpComponent },

  // ─── ADMIN ─────────────────────────────────────────────────────────────────
  // Accès : ADMIN + RH_SMARTEK + RH_COMPANY (selon sous-route)
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'RH_SMARTEK', 'RH_COMPANY'] },
    children: [
      { path: '', component: DashboardPageComponent },
      { path: 'profile', component: DashboardPageComponent },

      // ── ADMIN + RH_SMARTEK ──────────────────────────────────────────────
      { path: 'admin-approvals',  component: AdminPendingApprovalsComponent,   data: { roles: ['ADMIN', 'RH_SMARTEK'] } },
      { path: 'skill-validation', component: SkillEvidenceManagementComponent, data: { roles: ['ADMIN', 'RH_SMARTEK'] } },
      { path: 'users',            component: UserManagementComponent,           data: { roles: ['ADMIN', 'RH_SMARTEK'] } },
      { path: 'certifications',   component: RhCertificationsComponent,         data: { roles: ['ADMIN', 'RH_SMARTEK'] } },

      // ── ADMIN uniquement : Sponsor Management ───────────────────────────
      { path: 'sponsors',           component: SponsorListComponent,     data: { roles: ['ADMIN'] } },
      { path: 'sponsors/add',       component: AddSponsorComponent,      data: { roles: ['ADMIN'] } },
      { path: 'sponsors/edit/:id',  component: EditSponsorComponent,     data: { roles: ['ADMIN'] } },
      { path: 'contracts',          component: ContractListComponent,    data: { roles: ['ADMIN'] } },
      { path: 'contracts/add',      component: AddContractComponent,     data: { roles: ['ADMIN'] } },
      { path: 'contracts/edit/:id', component: EditContractComponent,    data: { roles: ['ADMIN'] } },
      { path: 'sponsorships',       component: SponsorshipListComponent, data: { roles: ['ADMIN'] } },

      // ── ADMIN : Training Management ─────────────────────────────────────
      {
        path: 'training-management',
        loadComponent: () => import('./features/dashboard/training-management/training-management.component').then(m => m.TrainingManagementComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Course Management ───────────────────────────────────────
      {
        path: 'course-management',
        loadComponent: () => import('./features/trainer/courses/trainer-courses.component').then(m => m.TrainerCoursesComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Exam Management ─────────────────────────────────────────
      {
        path: 'exam-management',
        loadComponent: () => import('./features/trainer/exams/trainer-exams.component').then(m => m.TrainerExamsComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Event Management ────────────────────────────────────────
      {
        path: 'event-management',
        loadComponent: () => import('./features/trainer/events/trainer-events.component').then(m => m.TrainerEventsComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Planning Management ─────────────────────────────────────
      {
        path: 'planning-management',
        loadComponent: () => import('./features/trainer/planning/trainer-planning.component').then(m => m.TrainerPlanningComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Learning Path Management ───────────────────────────────
      {
        path: 'learning-paths',
        loadComponent: () => import('./features/dashboard/learning-path-management/learning-path-management.component').then(m => m.LearningPathManagementComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Badge & Certification Management ────────────────────────
      {
        path: 'badge-management',
        loadComponent: () => import('./features/certifications-badges/badge-template-list/badge-template-list.component').then(m => m.BadgeTemplateListComponent),
        data: { roles: ['ADMIN'] }
      },
      {
        path: 'certification-management',
        loadComponent: () => import('./features/certifications-badges/certification-template-list/certification-template-list.component').then(m => m.CertificationTemplateListComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── ADMIN : Analytics ───────────────────────────────────────────────
      {
        path: 'learner-analytics',
        loadComponent: () => import('./features/trainer/learner-analytics/learner-analytics.component').then(m => m.LearnerAnalyticsComponent),
        data: { roles: ['ADMIN'] }
      },

      // ── RH_COMPANY (+ ADMIN peut voir) ──────────────────────────────────
      { path: 'employees',    component: RhCompanyDashboardComponent,   data: { roles: ['ADMIN', 'RH_COMPANY'] } },
      { path: 'enrollments',  component: CompanyParticipationComponent, data: { roles: ['ADMIN', 'RH_COMPANY'] } },
      { path: 'job-offers',   component: RhCompanyOffersComponent,      data: { roles: ['ADMIN', 'RH_COMPANY'] } },
      {
        path: 'employee-certifications',
        loadComponent: () => import('./features/certifications-badges/my-certifications/my-certifications.component').then(m => m.MyCertificationsComponent),
        data: { roles: ['ADMIN', 'RH_COMPANY'] }
      },
      {
        path: 'employee-skills',
        loadComponent: () => import('./features/dashboard/skill-evidence-management/skill-evidence-management.component').then(m => m.SkillEvidenceManagementComponent),
        data: { roles: ['ADMIN', 'RH_COMPANY'] }
      },
    ]
  },

  // ─── SPONSOR ───────────────────────────────────────────────────────────────
  // Accès : SPONSOR uniquement
  {
    path: 'sponsor',
    component: SponsorLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['SPONSOR'] },
    children: [
      { path: '', component: SponsorDashboardComponent },
      { path: 'dashboard', component: SponsorDashboardComponent },
    ]
  },

  // ─── LEARNER ───────────────────────────────────────────────────────────────
  // Accès : LEARNER uniquement (layout dédié avec navbar learner)
  {
    path: 'learner',
    component: LearnerLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['LEARNER'] },
    children: [
      { path: '',                component: LearnerCoursesComponent },
      { path: 'courses',         component: LearnerCoursesComponent },
      { path: 'training',        component: LearnerTrainingComponent },
      { path: 'planning',        component: LearnerPlanningComponent },
      { path: 'events',          component: LearnerEventsComponent },
      { path: 'exams',           component: LearnerExamsComponent },
      {
        path: 'job-offers',
        loadComponent: () => import('./features/learner/job-offers/job-offers-learner.component').then(m => m.JobOffersLearnerComponent)
      },
      {
        path: 'skills',
        loadComponent: () => import('./features/learner/skill-evidence/learner-skill-evidence.component').then(m => m.LearnerSkillEvidenceComponent)
      },
      {
        path: 'certifications',
        loadComponent: () => import('./features/learner/certifications/certifications.component').then(m => m.LearnerCertificationsComponent)
      },
      {
        path: 'learning-path',
        loadComponent: () => import('./features/learner/learning-path/learner-learning-path.component').then(m => m.LearnerLearningPathComponent)
      },
    ]
  },

  // ─── TRAINER ───────────────────────────────────────────────────────────────
  // Accès : TRAINER uniquement (layout dédié avec navbar trainer)
  {
    path: 'trainer',
    component: TrainerLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TRAINER'] },
    children: [
      { path: '',                    component: TrainerCoursesComponent },
      { path: 'courses',             component: TrainerCoursesComponent },
      { path: 'training-management', component: TrainerTrainingManagementComponent },
      { path: 'planning',            component: TrainerPlanningComponent },
      { path: 'events',              component: TrainerEventsComponent },
      { path: 'exams',               component: TrainerExamsComponent },
      { path: 'learner-analytics',   component: LearnerAnalyticsComponent },
      {
        path: 'badge-templates',
        loadComponent: () => import('./features/certifications-badges/badge-template-list/badge-template-list.component').then(m => m.BadgeTemplateListComponent)
      },
      {
        path: 'certification-templates',
        loadComponent: () => import('./features/certifications-badges/certification-template-list/certification-template-list.component').then(m => m.CertificationTemplateListComponent)
      },
      {
        path: 'award-badges',
        loadComponent: () => import('./features/certifications-badges/award-badge/award-badge.component').then(m => m.AwardBadgeComponent)
      },
      {
        path: 'learning-paths',
        loadComponent: () => import('./features/dashboard/learning-path-management/learning-path-management.component').then(m => m.LearningPathManagementComponent)
      },
    ]
  },

  // ─── RH_SMARTEK ────────────────────────────────────────────────────────────
  // Accès : RH_SMARTEK uniquement (layout dédié)
  {
    path: 'rh-smartek',
    component: RhSmartekLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['RH_SMARTEK'] },
    children: [
      { path: '',               component: RhCoursesComponent },
      { path: 'courses',        component: RhCoursesComponent },
      { path: 'events',         component: RhEventsComponent },
      { path: 'exams',          component: RhExamsComponent },
      { path: 'interviews',     component: InterviewsComponent },
      { path: 'schedule',       component: ScheduleComponent },
      { path: 'certifications', component: RhCertificationsComponent },
      // Routes partagées avec dashboard (users, skill-validation)
      { path: 'users',          component: UserManagementComponent },
      { path: 'skill-validation', component: SkillEvidenceManagementComponent },
      {
        path: 'training',
        loadComponent: () => import('./features/dashboard/training-management/training-management.component').then(m => m.TrainingManagementComponent)
      },
      {
        path: 'offers',
        loadComponent: () => import('./features/rh-company/offers/rh-company-offers.component').then(m => m.RhCompanyOffersComponent)
      },
    ]
  },

  // ─── PARTNER ───────────────────────────────────────────────────────────────
  // Accès : PARTNER uniquement (layout dédié)
  {
    path: 'partner',
    component: PartnerLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PARTNER'] },
    children: [
      { path: '',            component: PartnerEventsComponent },
      { path: 'events',      component: PartnerEventsComponent },
      { path: 'sponsorship', component: SponsorshipComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
