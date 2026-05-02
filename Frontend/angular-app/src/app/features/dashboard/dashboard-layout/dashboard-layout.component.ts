import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { CommonModule } from '@angular/common';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { NotificationComponent } from '../../../shared/components/notification/notification.component';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, CommonModule, NotificationComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss'
})
export class DashboardLayoutComponent implements OnInit, OnDestroy {
  currentUser: AuthResponse | null = null;
  currentRoute: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentRoute = event.url;
    });
  }

  ngOnDestroy(): void {
    // No subscriptions to clean up
  }

  logout(): void {
    this.authService.logout();
  }

  getUserInitial(): string {
    return this.currentUser?.firstName?.charAt(0).toUpperCase() || 'U';
  }

  getUserImage(): string | null {
    if (this.currentUser?.imageBase64) {
      return `data:image/jpeg;base64,${this.currentUser.imageBase64}`;
    }
    return null;
  }

  formatRole(role: string | undefined): string {
    if (!role) return 'User';
    const roleMap: { [key: string]: string } = {
      'LEARNER': 'Learner',
      'TRAINER': 'Trainer',
      'RH_COMPANY': 'HR Company',
      'RH_SMARTEK': 'HR Smartek',
      'SPONSOR': 'Sponsor',
      'ADMIN': 'Administrator'
    };
    return roleMap[role] || role;
  }

  getPageTitle(): string {
    const route = this.currentRoute;
    if (route.includes('/admin-approvals')) return 'Pending Approvals';
    if (route.includes('/admin-sponsorships')) return 'All Sponsorships';
    if (route.includes('/sponsors')) return 'Sponsors';
    if (route.includes('/contracts')) return 'Contracts';
    if (route.includes('/sponsorships')) return 'Sponsorships';
    return 'Dashboard';
  }
}
