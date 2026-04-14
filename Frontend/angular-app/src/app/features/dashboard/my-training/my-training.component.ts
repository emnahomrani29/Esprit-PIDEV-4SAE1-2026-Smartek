import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TrainingEnrollmentService, TrainingEnrollmentResponse } from '../../../core/services/training-enrollment.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-my-training',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-training.component.html',
  styleUrl: './my-training.component.scss'
})
export class MyTrainingComponent implements OnInit {
  enrollments: TrainingEnrollmentResponse[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private enrollmentService: TrainingEnrollmentService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMyTrainings();
  }

  loadMyTrainings(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) {
      this.errorMessage = 'Utilisateur non connecté';
      return;
    }

    this.isLoading = true;
    this.enrollmentService.getUserEnrollments(currentUser.userId).subscribe({
      next: (enrollments) => {
        this.enrollments = enrollments;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading enrollments:', error);
        this.errorMessage = 'Erreur lors du chargement de vos formations';
        this.isLoading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ENROLLED': return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS': return 'bg-yellow-100 text-yellow-800';
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ENROLLED': return 'Inscrit';
      case 'IN_PROGRESS': return 'En cours';
      case 'COMPLETED': return 'Terminé';
      case 'CANCELLED': return 'Annulé';
      default: return status;
    }
  }

  continueTraining(trainingId: number): void {
    // Rediriger vers My Courses avec le trainingId en paramètre
    this.router.navigate(['/dashboard/my-courses'], { 
      queryParams: { trainingId: trainingId } 
    });
  }

  unenroll(enrollment: TrainingEnrollmentResponse): void {
    if (!confirm('Êtes-vous sûr de vouloir vous désinscrire de cette formation?')) {
      return;
    }

    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    this.isLoading = true;
    this.enrollmentService.unenrollUser(currentUser.userId, enrollment.trainingId).subscribe({
      next: () => {
        this.loadMyTrainings();
        alert('Désinscription réussie');
      },
      error: (error) => {
        console.error('Error unenrolling:', error);
        if (error.status === 404) {
          alert('Cette inscription n\'existe plus. Actualisation de la liste...');
          this.loadMyTrainings();
        } else {
          alert('Erreur lors de la désinscription: ' + (error.error?.message || 'Erreur inconnue'));
        }
        this.isLoading = false;
      }
    });
  }
}
