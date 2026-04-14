import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-rh-company-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './rh-company-dashboard.component.html',
  styleUrls: ['./rh-company-dashboard.component.css']
})
export class RhCompanyDashboardComponent implements OnInit {
  userName: string = '';
  companyName: string = 'Entreprise';
  stats = {
    activeOffers: 0,
    pendingApplications: 0,
    scheduledInterviews: 0,
    employeesInTraining: 0
  };

  recentActivities: any[] = [];

  constructor(private authService: AuthService) {}

  ngOnInit() {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH';
    
    this.loadStats();
    this.loadRecentActivities();
  }

  loadStats() {
    // TODO: Charger les statistiques depuis l'API
    this.stats = {
      activeOffers: 8,
      pendingApplications: 15,
      scheduledInterviews: 5,
      employeesInTraining: 12
    };
  }

  loadRecentActivities() {
    // TODO: Charger les activités récentes depuis l'API
    this.recentActivities = [
      {
        type: 'application',
        message: 'Nouvelle candidature pour "Développeur Full Stack"',
        date: new Date(),
        icon: '📝'
      },
      {
        type: 'interview',
        message: 'Entretien programmé avec Jean Dupont',
        date: new Date(Date.now() - 3600000),
        icon: '🎤'
      },
      {
        type: 'training',
        message: '3 employés ont terminé la formation Angular',
        date: new Date(Date.now() - 7200000),
        icon: '✅'
      }
    ];
  }
}
