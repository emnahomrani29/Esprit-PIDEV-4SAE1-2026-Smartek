import { Routes } from '@angular/router';
import { HomePageComponent } from './features/home/home-page/home-page.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout/dashboard-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page/dashboard-page.component';
import { SignUpComponent } from './features/auth/sign-up/sign-up.component';
import { SignInComponent } from './features/auth/sign-in/sign-in.component';
import { authGuard } from './core/guards/auth.guard';
import { SponsorListComponent } from './features/sponsors/sponsor-list/sponsor-list.component';
import { AddSponsorComponent } from './features/sponsors/add-sponsor/add-sponsor.component';
import { EditSponsorComponent } from './features/sponsors/edit-sponsor/edit-sponsor.component';
import { ContractListComponent } from './features/contracts/contract-list/contract-list.component';
import { AddContractComponent } from './features/contracts/add-contract/add-contract.component';
import { EditContractComponent } from './features/contracts/edit-contract/edit-contract.component';
import { SponsorshipListComponent } from './features/sponsorships/sponsorship-list/sponsorship-list.component';
// import { AddSponsorshipComponent } from './features/sponsorships/add-sponsorship/add-sponsorship.component';
import { SponsorDashboardComponent } from './features/dashboard/sponsor-dashboard/sponsor-dashboard.component';
import { SponsorLayoutComponent } from './features/dashboard/sponsor-layout/sponsor-layout.component';
import { AdminPendingApprovalsComponent } from './features/dashboard/admin-pending-approvals/admin-pending-approvals.component';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'auth/sign-in', component: SignInComponent },
  { path: 'auth/sign-up', component: SignUpComponent },
  
  // Admin Dashboard (with navbar and sidebar)
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardPageComponent },
      { path: 'profile', component: DashboardPageComponent },
      { path: 'admin-approvals', component: AdminPendingApprovalsComponent },

      // Sponsor Management
      { path: 'sponsors', component: SponsorListComponent },
      { path: 'sponsors/add', component: AddSponsorComponent },
      { path: 'sponsors/edit/:id', component: EditSponsorComponent },

      // Contract Management
      { path: 'contracts', component: ContractListComponent },
      { path: 'contracts/add', component: AddContractComponent },
      { path: 'contracts/edit/:id', component: EditContractComponent },

      // Sponsorship Management
      { path: 'sponsorships', component: SponsorshipListComponent },
    ]
  },

  // Sponsor Dashboard (clean layout without navbar)
  {
    path: 'sponsor',
    component: SponsorLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: SponsorDashboardComponent },
      { path: 'dashboard', component: SponsorDashboardComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
