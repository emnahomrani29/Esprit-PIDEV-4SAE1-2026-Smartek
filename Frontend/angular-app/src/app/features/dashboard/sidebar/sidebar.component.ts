import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { PermissionService } from '../../../core/services/permission.service';
import { HasPermissionDirective } from '../../../core/directives/has-permission.directive';
import { MENU_ITEMS, MenuItem } from '../../../core/config/menu.config';
import { WorkflowService } from '../../../shared/workflow.service';
import { SponsorService } from '../../../shared/sponsor.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, HasPermissionDirective],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit, OnDestroy {
  currentUser: AuthResponse | null = null;
  menuItems: MenuItem[] = [];
  pendingApprovalsCount: number = 0;
  sponsorStatusCounts: { pending: number; approved: number; rejected: number } = { pending: 0, approved: 0, rejected: 0 };
  private refreshSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private permissionService: PermissionService,
    private workflowService: WorkflowService,
    private sponsorService: SponsorService
  ) {}

  ngOnInit(): void {
    // Charger les données depuis le localStorage d'abord
    this.currentUser = this.authService.getUserInfo();

    // Filtrer les menus selon les permissions
    this.filterMenuItems();

    // Load notification counts
    this.loadNotificationCounts();

    // Refresh counts every 30 seconds
    this.refreshSubscription = interval(30000).pipe(
      switchMap(() => {
        this.loadNotificationCounts();
        return [];
      })
    ).subscribe();

    // Puis récupérer les données à jour depuis la base de données
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        this.currentUser = userData;
        this.filterMenuItems();
        this.loadNotificationCounts();
      },
      error: (error) => {
        // Error fetching user data
      }
    });
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadNotificationCounts(): void {
    // Load admin pending approvals count
    if (this.isAdmin()) {
      this.workflowService.getPendingCount().subscribe({
        next: (count) => this.pendingApprovalsCount = count,
        error: (err) => { /* Error loading pending count */ }
      });
    }

    // Load sponsor status summary
    if (this.isSponsor() && this.currentUser?.email) {
      this.sponsorService.getSponsorByEmail(this.currentUser.email).subscribe({
        next: (sponsor) => {
          if (sponsor.id) {
            this.workflowService.getStatusSummary(sponsor.id).subscribe({
              next: (summary) => {
                this.sponsorStatusCounts = {
                  pending: summary.pendingCount || 0,
                  approved: summary.approvedCount || 0,
                  rejected: summary.rejectedCount || 0
                };
              },
              error: (err) => { /* Error loading status summary */ }
            });
          }
        },
        error: (err) => { /* Error loading sponsor */ }
      });
    }
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  isSponsor(): boolean {
    return this.currentUser?.role === 'SPONSOR';
  }

  filterMenuItems(): void {
    this.menuItems = MENU_ITEMS.filter(item => {
      // Always show generic dividers (no roles)
      if (item.divider && (!item.roles || item.roles.length === 0)) {
        return true;
      }

      // Show headers only if the user has the required role
      if (item.header) {
        if (item.roles && item.roles.length > 0) {
          return this.permissionService.hasAnyRole(item.roles);
        }
        return true;
      }

      // Check roles first
      if (item.roles && item.roles.length > 0) {
        if (!this.permissionService.hasAnyRole(item.roles)) {
          return false;
        }
      }

      // If no permissions/roles required, show the item
      if ((!item.permissions || item.permissions.length === 0) &&
          (!item.roles || item.roles.length === 0)) {
        return true;
      }

      // Check permissions
      if (item.permissions && item.permissions.length > 0) {
        return this.permissionService.hasAnyPermission(item.permissions);
      }

      return true;
    });
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
}
