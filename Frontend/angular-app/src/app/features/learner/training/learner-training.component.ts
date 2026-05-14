import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TrainingEnrollmentService, TrainingEnrollmentResponse } from '../../../core/services/training-enrollment.service';
import { TrainingService } from '../../../core/services/training.service';
import { AuthService } from '../../../core/services/auth.service';
import { Training } from '../../../core/models/training.model';

@Component({
  selector: 'app-learner-training',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './learner-training.component.html',
  styleUrl: './learner-training.component.scss'
})
export class LearnerTrainingComponent implements OnInit {
  enrollments: TrainingEnrollmentResponse[] = [];
  allTrainings: Training[] = [];
  enrolledTrainingIds: Set<number> = new Set();
  isLoading = false;
  errorMessage = '';
  isEnrolling = false;

  constructor(
    private enrollmentService: TrainingEnrollmentService,
    private trainingService: TrainingService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) {
      this.errorMessage = 'Utilisateur non connecté';
      return;
    }

    this.isLoading = true;

    // Charger toutes les formations
    this.trainingService.getAllTrainings().subscribe({
      next: (trainings) => {
        this.allTrainings = trainings;
        
        // Charger les inscriptions du learner
        this.enrollmentService.getUserEnrollments(currentUser.userId).subscribe({
          next: (enrollments) => {
            this.enrollments = enrollments;
            this.enrolledTrainingIds = new Set(enrollments.map(e => e.trainingId));
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading enrollments:', error);
            this.isLoading = false;
          }
        });
      },
      error: (error) => {
        console.error('Error loading trainings:', error);
        this.errorMessage = 'Erreur lors du chargement des formations';
        this.isLoading = false;
      }
    });
  }

  isEnrolled(trainingId: number | undefined): boolean {
    return trainingId ? this.enrolledTrainingIds.has(trainingId) : false;
  }

  getEnrollment(trainingId: number | undefined): TrainingEnrollmentResponse | undefined {
    return this.enrollments.find(e => e.trainingId === trainingId);
  }

  enrollToTraining(training: Training): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !training.trainingId) return;

    if (confirm(`Voulez-vous vous inscrire à la formation "${training.title}" ?`)) {
      this.isEnrolling = true;
      
      this.enrollmentService.enrollUser({
        trainingId: training.trainingId,
        userId: currentUser.userId
      }).subscribe({
        next: (enrollment) => {
          this.enrollments.push(enrollment);
          this.enrolledTrainingIds.add(training.trainingId!);
          this.isEnrolling = false;
          alert('Inscription réussie !');
        },
        error: (error) => {
          console.error('Error enrolling:', error);
          alert('Erreur lors de l\'inscription: ' + (error.error?.message || 'Erreur inconnue'));
          this.isEnrolling = false;
        }
      });
    }
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
    this.router.navigate(['/learner-courses'], { 
      queryParams: { trainingId: trainingId } 
    });
  }
}
