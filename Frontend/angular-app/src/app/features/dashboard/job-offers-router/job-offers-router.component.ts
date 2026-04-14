import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { JobOffersComponent } from '../job-offers/job-offers.component';
import { JobOffersLearnerComponent } from '../../learner/job-offers/job-offers-learner.component';

@Component({
  selector: 'app-job-offers-router',
  standalone: true,
  imports: [CommonModule, JobOffersComponent, JobOffersLearnerComponent],
  template: `
    <app-job-offers-learner *ngIf="isLearner"></app-job-offers-learner>
    <app-job-offers *ngIf="!isLearner"></app-job-offers>
  `
})
export class JobOffersRouterComponent implements OnInit {
  isLearner = false;

  constructor(private router: Router) {}

  ngOnInit() {
    // Récupérer le rôle de l'utilisateur depuis le localStorage ou un service d'authentification
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.isLearner = user.role === 'LEARNER';
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
  }
}
