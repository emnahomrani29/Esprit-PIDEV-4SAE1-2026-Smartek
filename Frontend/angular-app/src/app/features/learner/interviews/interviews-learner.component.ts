import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterviewService } from '../../../core/services/interview.service';
import { OfferService } from '../../../core/services/offer.service';
import { AuthService } from '../../../core/services/auth.service';
import { Interview } from '../../../core/models/interview.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-interviews-learner',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interviews-learner.component.html',
  styleUrls: ['./interviews-learner.component.css']
})
export class InterviewsLearnerComponent implements OnInit {
  interviews: Interview[] = [];
  selectedInterview: Interview | null = null;
  showDetailModal = false;
  
  loading = false;
  errorMessage = '';
  successMessage = '';
  currentUserId: number | null = null;

  constructor(
    private interviewService: InterviewService,
    private offerService: OfferService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadCurrentUser();
  }

  loadCurrentUser() {
    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      this.currentUserId = userInfo.userId;
      this.loadInterviews();
    } else {
      this.errorMessage = 'Vous devez être connecté pour voir vos entretiens';
    }
  }

  loadInterviews() {
    if (!this.currentUserId) return;
    
    this.loading = true;
    this.errorMessage = '';
    
    console.log('Loading interviews for learner:', this.currentUserId);
    
    this.interviewService.getInterviewsByLearner(this.currentUserId).subscribe({
      next: (data) => {
        console.log('Interviews loaded:', data);
        this.interviews = data;
        
        // Charger les titres des offres
        this.loadOfferTitles();
        
        // Trier par date (les plus proches en premier)
        this.interviews.sort((a, b) => {
          return new Date(a.interviewDate).getTime() - new Date(b.interviewDate).getTime();
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading interviews:', error);
        this.errorMessage = 'Erreur lors du chargement de vos entretiens';
        this.loading = false;
      }
    });
  }

  loadOfferTitles() {
    // Récupérer les IDs uniques des offres
    const offerIds = [...new Set(this.interviews.map(i => i.offerId))];
    
    // Charger toutes les offres en parallèle
    const offerRequests = offerIds.map(id => this.offerService.getOfferById(id));
    
    forkJoin(offerRequests).subscribe({
      next: (offers) => {
        // Créer une map offerId -> titre
        const offerTitles = new Map();
        offers.forEach(offer => {
          if (offer && offer.id) {
            offerTitles.set(offer.id, offer.title);
          }
        });
        
        // Ajouter les titres aux entretiens
        this.interviews.forEach(interview => {
          interview.offerTitle = offerTitles.get(interview.offerId) || 'Offre d\'emploi';
        });
      },
      error: (error) => {
        console.error('Error loading offer titles:', error);
        // Continuer même si le chargement des titres échoue
      }
    });
  }

  viewDetails(interview: Interview) {
    this.selectedInterview = interview;
    this.showDetailModal = true;
  }

  closeDetailModal() {
    this.showDetailModal = false;
    this.selectedInterview = null;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return 'bg-blue-100 text-blue-800';
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'RESCHEDULED': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'SCHEDULED': return '📅 Planifié';
      case 'COMPLETED': return '✓ Terminé';
      case 'CANCELLED': return '✗ Annulé';
      case 'RESCHEDULED': return '↻ Reporté';
      default: return status;
    }
  }

  isUpcoming(interviewDate: string): boolean {
    return new Date(interviewDate) > new Date();
  }

  isPast(interviewDate: string): boolean {
    return new Date(interviewDate) < new Date();
  }

  getTimeUntil(interviewDate: string): string {
    const now = new Date();
    const interview = new Date(interviewDate);
    const diff = interview.getTime() - now.getTime();
    
    if (diff < 0) return 'Passé';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) {
      return `${days} jour${days > 1 ? 's' : ''}`;
    } else if (hours > 0) {
      return `${hours} heure${hours > 1 ? 's' : ''}`;
    } else {
      return `${minutes} minute${minutes > 1 ? 's' : ''}`;
    }
  }

  getUpcomingCount(): number {
    return this.interviews.filter(i => 
      i.status === 'SCHEDULED' && this.isUpcoming(i.interviewDate)
    ).length;
  }

  getCompletedCount(): number {
    return this.interviews.filter(i => i.status === 'COMPLETED').length;
  }
}
