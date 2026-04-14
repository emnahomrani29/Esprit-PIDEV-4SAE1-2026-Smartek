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
  userRole = 'RH Entreprise';
  userInitial = 'R';
  experience = 0;
  companyId = 0;

  stats = {
    activeOffers: 0,
    pendingApplications: 0,
    scheduledInterviews: 0,
    employeesInTraining: 0,
    offersChange: '+12%',
    applicationsChange: '+5%',
    interviewsChange: '-2%',
    trainingChange: '+8%'
  };

  recentOffers: any[] = [];
  recentActivities: any[] = [];

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit() {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH';
    this.userInitial = this.userName.charAt(0).toUpperCase();
    this.experience = userInfo?.experience || 0;
    this.companyId = userInfo?.userId || 0;
    this.loadStats();
    this.loadRecentOffers();
    this.loadRecentActivities();
  }

  loadStats() {
    this.http.get<any>(`${environment.apiUrl}/offers/stats/company/${this.companyId}`).subscribe({
      next: (data) => {
        this.stats.activeOffers = data.activeOffers || 0;
        this.stats.pendingApplications = data.pendingApplications || 0;
        this.stats.scheduledInterviews = data.scheduledInterviews || 0;
      },
      error: () => {
        this.stats = { ...this.stats, activeOffers: 8, pendingApplications: 15, scheduledInterviews: 5, employeesInTraining: 12 };
      }
    });
  }

  loadRecentOffers() {
    this.http.get<any>(`${environment.apiUrl}/offers/company/${this.companyId}`).subscribe({
      next: (data) => {
        const arr = Array.isArray(data) ? data : (data?.content ?? []);
        this.recentOffers = arr.slice(0, 4).map((o: any) => ({
          title: o.title,
          contractType: o.contractType,
          location: o.location,
          applicationCount: o.applicationCount || 0,
          status: o.status,
          completion: Math.floor(Math.random() * 60) + 20
        }));
      },
      error: () => {
        this.recentOffers = [
          { title: 'Développeur Full Stack', contractType: 'CDI', location: 'Paris', applicationCount: 12, status: 'ACTIVE', completion: 60 },
          { title: 'Chef de Projet IT', contractType: 'CDI', location: 'Lyon', applicationCount: 7, status: 'ACTIVE', completion: 35 },
          { title: 'UX Designer', contractType: 'CDD', location: 'Remote', applicationCount: 5, status: 'DRAFT', completion: 15 },
        ];
      }
    });
  }

  loadRecentActivities() {
    this.recentActivities = [
      { icon: 'offer', color: 'bg-green-500', label: 'Nouvelle offre publiée', sub: 'Développeur React', time: 'Il y a 2h' },
      { icon: 'app', color: 'bg-blue-500', label: 'Candidature reçue', sub: 'Pour Chef de Projet', time: 'Il y a 4h' },
      { icon: 'interview', color: 'bg-purple-500', label: 'Entretien planifié', sub: 'Jean Dupont — 15h00', time: 'Aujourd\'hui' },
      { icon: 'training', color: 'bg-orange-500', label: 'Formation terminée', sub: '3 employés — Angular', time: 'Hier' },
    ];
  }

  statusColor(status: string): string {
    const map: any = { ACTIVE: 'text-green-600', CLOSED: 'text-red-500', DRAFT: 'text-gray-400', EXPIRED: 'text-orange-500' };
    return map[status] || 'text-gray-500';
  }

  statusBg(status: string): string {
    const map: any = { ACTIVE: 'bg-green-100', CLOSED: 'bg-red-100', DRAFT: 'bg-gray-100', EXPIRED: 'bg-orange-100' };
    return map[status] || 'bg-gray-100';
  }
}
