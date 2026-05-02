import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { AdminPendingApprovalsComponent } from '../admin-pending-approvals/admin-pending-approvals.component';
import { SponsorService } from '../../../shared/sponsor.service';
import { WorkflowService } from '../../../shared/workflow.service';
import { NotificationService } from '../../../shared/notification.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

interface PlatformStats {
  totalBudget: number;
  totalSponsors: number;
  totalSponsorships: number;
  pendingApprovals: number;
  budgetGrowth: number;
  sponsorGrowth: number;
  sponsorshipGrowth: number;
  approvalRate: number;
  avgResponseTime: number;
  activeContracts: number;
}

interface TopSponsor {
  id: number;
  name: string;
  companyName: string;
  totalAmount: number;
  sponsorshipCount: number;
}

interface PendingSponsorship {
  id: number;
  sponsorName: string;
  sponsorEmail: string;
  amountAllocated: number;
  targetType: string;
  createdAt: string;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminPendingApprovalsComponent],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  currentUser: AuthResponse | null = null;
  platformStats: PlatformStats = {
    totalBudget: 0,
    totalSponsors: 0,
    totalSponsorships: 0,
    pendingApprovals: 0,
    budgetGrowth: 0,
    sponsorGrowth: 0,
    sponsorshipGrowth: 0,
    approvalRate: 0,
    avgResponseTime: 0,
    activeContracts: 0
  };
  topSponsors: TopSponsor[] = [];
  pendingSponsorships: PendingSponsorship[] = [];
  private refreshSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router,
    private sponsorService: SponsorService,
    private workflowService: WorkflowService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();

    // Redirect sponsors to their dedicated dashboard
    if (this.currentUser?.role === 'SPONSOR') {
      this.router.navigate(['/dashboard/sponsor-dashboard']);
      return;
    }

    // Load initial data
    this.loadPlatformStats();
    this.loadTopSponsors();
    this.loadPendingSponsorships();

    // Auto-refresh every 30 seconds
    this.refreshSubscription = interval(30000).pipe(
      switchMap(() => {
        this.loadPlatformStats();
        this.loadTopSponsors();
        this.loadPendingSponsorships();
        return [];
      })
    ).subscribe();

    // Fetch fresh user data from database
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        this.currentUser = userData;
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

  loadPlatformStats(): void {
    if (!this.isAdmin()) return;

    // Load platform statistics
    this.sponsorService.getPlatformStats().subscribe({
      next: (stats) => {
        this.platformStats = {
          totalBudget: stats.totalBudget || 0,
          totalSponsors: stats.totalSponsors || 0,
          totalSponsorships: stats.totalSponsorships || 0,
          pendingApprovals: stats.pendingApprovals || 0,
          budgetGrowth: stats.budgetGrowth || 0,
          sponsorGrowth: stats.sponsorGrowth || 0,
          sponsorshipGrowth: stats.sponsorshipGrowth || 0,
          approvalRate: stats.approvalRate || 0,
          avgResponseTime: stats.avgResponseTime || 0,
          activeContracts: stats.activeContracts || 0
        };
      },
      error: (error) => {
        // Set empty values on error - no fake data
        this.platformStats = {
          totalBudget: 0,
          totalSponsors: 0,
          totalSponsorships: 0,
          pendingApprovals: 0,
          budgetGrowth: 0,
          sponsorGrowth: 0,
          sponsorshipGrowth: 0,
          approvalRate: 0,
          avgResponseTime: 0,
          activeContracts: 0
        };
      }
    });
  }

  loadTopSponsors(): void {
    if (!this.isAdmin()) return;

    this.sponsorService.getTopSponsors().subscribe({
      next: (sponsors) => {
        this.topSponsors = sponsors || [];
      },
      error: (error) => {
        // Set empty array on error - no fake data
        this.topSponsors = [];
      }
    });
  }

  loadPendingSponsorships(): void {
    if (!this.isAdmin()) return;

    this.workflowService.getPendingSponsorships().subscribe({
      next: (sponsorships) => {
        this.pendingSponsorships = sponsorships.map(s => ({
          id: s.id || 0,
          sponsorName: s.sponsorName || 'Unknown',
          sponsorEmail: s.sponsorEmail || '',
          amountAllocated: s.amountAllocated || 0,
          targetType: s.targetType || '',
          createdAt: s.createdAt || ''
        }));
      },
      error: (error) => {
        this.pendingSponsorships = [];
      }
    });
  }

  getUserImage(): string | null {
    if (this.currentUser?.imageBase64) {
      return `data:image/jpeg;base64,${this.currentUser.imageBase64}`;
    }
    return null;
  }

  getUserInitial(): string {
    return this.currentUser?.firstName?.charAt(0).toUpperCase() || 'U';
  }

  formatRole(role: string | undefined): string {
    if (!role) return 'User';

    const roleMap: { [key: string]: string } = {
      'LEARNER': 'Learner',
      'TRAINER': 'Trainer',
      'RH_COMPANY': 'HR Company',
      'RH_SMARTEK': 'HR Smartek',
      'PARTNER': 'Partner',
      'ADMIN': 'Admin',
      'SPONSOR': 'Sponsor'
    };

    return roleMap[role] || role;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  getTimeAgo(dateString: string): string {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInHours / 24);
    
    if (diffInDays > 0) {
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    } else if (diffInHours > 0) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    } else {
      return 'Just now';
    }
  }
}
