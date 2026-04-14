import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-rh-company-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './rh-company-dashboard.component.html',
  styleUrls: ['./rh-company-dashboard.component.css']
})
export class RhCompanyDashboardComponent implements OnInit {
  userName = '';
  companyId = 0;
  isLoading = true;

  stats: any = {
    activeOffers: 0, totalOffers: 0, draftOffers: 0, closedOffers: 0, expiredOffers: 0,
    totalApplications: 0, pendingApplications: 0, acceptedApplications: 0, rejectedApplications: 0,
    totalInterviews: 0, scheduledInterviews: 0, completedInterviews: 0,
    hiredCount: 0, acceptanceRate: 0
  };

  recentOffers: any[] = [];
  upcomingInterviews: any[] = [];

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit() {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH';
    this.companyId = userInfo?.userId || 0;
    this.loadStats();
    this.loadRecentOffers();
    this.loadUpcomingInterviews();
  }

  loadStats() {
    this.http.get<any>(`${environment.apiUrl}/offers/stats/company/${this.companyId}`).subscribe({
      next: (data) => { this.stats = { ...this.stats, ...data }; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  loadRecentOffers() {
    this.http.get<any>(`${environment.apiUrl}/offers/company/${this.companyId}`).subscribe({
      next: (data) => {
        const arr = Array.isArray(data) ? data : (data?.content ?? []);
        this.recentOffers = arr.slice(0, 5);
      }
    });
  }

  loadUpcomingInterviews() {
    this.http.get<any[]>(`${environment.apiUrl}/interviews/company/${this.companyId}`).subscribe({
      next: (data) => {
        this.upcomingInterviews = (data || [])
          .filter(i => i.status === 'SCHEDULED')
          .sort((a, b) => new Date(a.interviewDate).getTime() - new Date(b.interviewDate).getTime())
          .slice(0, 4);
      },
      error: () => {}
    });
  }

  get pipelineWidth() {
    const t = this.stats.totalApplications;
    if (!t) return { pending: 0, accepted: 0, rejected: 0 };
    return {
      pending: Math.round(this.stats.pendingApplications / t * 100),
      accepted: Math.round(this.stats.acceptedApplications / t * 100),
      rejected: Math.round(this.stats.rejectedApplications / t * 100),
    };
  }

  statusColor(status: string): string {
    const map: any = {
      ACTIVE: 'bg-green-100 text-green-700', CLOSED: 'bg-red-100 text-red-700',
      DRAFT: 'bg-gray-100 text-gray-500', EXPIRED: 'bg-orange-100 text-orange-600',
      PENDING: 'bg-yellow-100 text-yellow-700', ACCEPTED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-600', SCHEDULED: 'bg-blue-100 text-blue-700',
    };
    return map[status] || 'bg-gray-100 text-gray-600';
  }

  isToday(date: string): boolean {
    const d = new Date(date);
    const now = new Date();
    return d.toDateString() === now.toDateString();
  }

  isTomorrow(date: string): boolean {
    const d = new Date(date);
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return d.toDateString() === tomorrow.toDateString();
  }

  interviewLabel(date: string): string {
    if (this.isToday(date)) return "Aujourd'hui";
    if (this.isTomorrow(date)) return 'Demain';
    return '';
  }
}
