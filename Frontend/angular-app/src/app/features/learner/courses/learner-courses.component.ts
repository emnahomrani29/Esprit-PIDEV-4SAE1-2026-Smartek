import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';
import { TrainingService } from '../../../core/services/training.service';
import { TrainingEnrollmentService, TrainingEnrollmentResponse } from '../../../core/services/training-enrollment.service';
import { AuthService } from '../../../core/services/auth.service';
import { CourseService } from '../../../core/services/course.service';
import { LiveSessionService } from '../../../core/services/live-session.service';
import { Training, CourseInfo } from '../../../core/models/training.model';
import { LiveSessionResponse, DeliveryMode } from '../../../core/models/live-session.model';
import { SafePipe } from '../../../core/pipes/safe.pipe';
import { environment } from '../../../../environments/environment';

interface TrainingWithCourses {
  training: Training;
  enrollment: TrainingEnrollmentResponse;
  courses: CourseInfo[];
  completedCourses: Set<number>;
  progress: number;
  isExpanded: boolean; // Pour l'accordéon
}

@Component({
  selector: 'app-learner-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, SafePipe],
  templateUrl: './learner-courses.component.html',
  styleUrl: './learner-courses.component.scss',
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ height: 0, opacity: 0, overflow: 'hidden' }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        style({ height: '*', opacity: 1, overflow: 'hidden' }),
        animate('300ms ease-in', style({ height: 0, opacity: 0 }))
      ])
    ])
  ]
})
export class LearnerCoursesComponent implements OnInit {
  trainingsWithCourses: TrainingWithCourses[] = [];
  filteredTrainings: TrainingWithCourses[] = [];
  isLoading = false;
  errorMessage = '';
  showPdfModal = false;
  selectedCourse: CourseInfo | null = null;
  selectedTraining: TrainingWithCourses | null = null;
  pdfUrl: string | null = null;

  // Live Sessions
  showLiveSessionsModal = false;
  currentCourseForSessions: CourseInfo | null = null;
  liveSessions: LiveSessionResponse[] = [];
  loadingLiveSessions = false;
  DeliveryMode = DeliveryMode;

  // Recherche et tri
  searchTerm: string = '';
  sortBy: 'name' | 'progress' | 'date' = 'name';
  sortOrder: 'asc' | 'desc' = 'asc';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trainingService: TrainingService,
    private enrollmentService: TrainingEnrollmentService,
    private authService: AuthService,
    private courseService: CourseService,
    private liveSessionService: LiveSessionService
  ) {}

  ngOnInit(): void {
    this.loadAllTrainingsWithCourses();
  }

  ngAfterViewInit(): void {
    // Appliquer les filtres après le chargement initial
    setTimeout(() => this.applyFiltersAndSort(), 0);
  }

  loadAllTrainingsWithCourses(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) {
      this.errorMessage = 'Utilisateur non connecté';
      return;
    }

    this.isLoading = true;
    
    // Charger toutes les inscriptions du learner
    this.enrollmentService.getUserEnrollments(currentUser.userId).subscribe({
      next: (enrollments) => {
        if (enrollments.length === 0) {
          this.isLoading = false;
          return;
        }

        // Pour chaque inscription, charger les détails de la formation et ses cours
        let loadedCount = 0;
        enrollments.forEach(enrollment => {
          this.trainingService.getTrainingById(enrollment.trainingId).subscribe({
            next: (training) => {
              const completedCourses = this.loadCompletedCoursesForTraining(currentUser.userId, enrollment.trainingId);
              
              this.trainingsWithCourses.push({
                training,
                enrollment,
                courses: training.courses || [],
                completedCourses,
                progress: enrollment.progress,
                isExpanded: true // Par défaut, toutes les sections sont ouvertes
              });

              loadedCount++;
              if (loadedCount === enrollments.length) {
                this.isLoading = false;
                this.applyFiltersAndSort();
              }
            },
            error: (error) => {
              console.error('Error loading training:', error);
              loadedCount++;
              if (loadedCount === enrollments.length) {
                this.isLoading = false;
                this.applyFiltersAndSort();
              }
            }
          });
        });
      },
      error: (error) => {
        console.error('Error loading enrollments:', error);
        this.errorMessage = 'Erreur lors du chargement de vos formations';
        this.isLoading = false;
      }
    });
  }

  loadCompletedCoursesForTraining(userId: number, trainingId: number): Set<number> {
    const key = `completed_courses_${userId}_${trainingId}`;
    const saved = localStorage.getItem(key);
    if (saved) {
      const savedCourseIds = JSON.parse(saved);
      return new Set(savedCourseIds);
    }
    return new Set();
  }

  saveCompletedCourses(trainingWithCourses: TrainingWithCourses): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    const key = `completed_courses_${currentUser.userId}_${trainingWithCourses.enrollment.trainingId}`;
    localStorage.setItem(key, JSON.stringify(Array.from(trainingWithCourses.completedCourses)));
  }

  isCourseCompleted(trainingWithCourses: TrainingWithCourses, courseId: number | undefined): boolean {
    return courseId ? trainingWithCourses.completedCourses.has(courseId) : false;
  }

  toggleCourseCompletion(trainingWithCourses: TrainingWithCourses, courseId: number | undefined): void {
    if (!courseId) return;

    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    const wasCompleted = trainingWithCourses.completedCourses.has(courseId);

    if (wasCompleted) {
      // Dé-compléter le cours
      trainingWithCourses.completedCourses.delete(courseId);
      this.saveCompletedCourses(trainingWithCourses);
      this.updateProgress(trainingWithCourses);

      // Appeler l'API backend pour dé-compléter le cours
      this.courseService.uncompleteCourse(courseId, currentUser.userId).subscribe({
        next: () => {
          console.log(`Cours ${courseId} marqué comme non terminé dans le backend`);
        },
        error: (error) => {
          console.error('Erreur lors de la dé-complétion du cours:', error);
          // Rollback en cas d'erreur
          trainingWithCourses.completedCourses.add(courseId);
          this.saveCompletedCourses(trainingWithCourses);
          this.updateProgress(trainingWithCourses);
        }
      });
    } else {
      // Compléter le cours
      trainingWithCourses.completedCourses.add(courseId);
      this.saveCompletedCourses(trainingWithCourses);
      this.updateProgress(trainingWithCourses);

      // Appeler l'API backend pour compléter le cours
      this.courseService.completeCourse(courseId, currentUser.userId).subscribe({
        next: () => {
          console.log(`Cours ${courseId} marqué comme terminé dans le backend`);
        },
        error: (error) => {
          console.error('Erreur lors de la complétion du cours:', error);
          // Rollback en cas d'erreur
          trainingWithCourses.completedCourses.delete(courseId);
          this.saveCompletedCourses(trainingWithCourses);
          this.updateProgress(trainingWithCourses);
    }
 });
    }
  }         

  updateProgress(trainingWithCourses: TrainingWithCourses): void {
    if (trainingWithCourses.courses.length === 0) return;

    const newProgress = Math.round((trainingWithCourses.completedCourses.size / trainingWithCourses.courses.length) * 100);
    trainingWithCourses.progress = newProgress;

    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    this.enrollmentService.updateProgress(
      currentUser.userId, 
      trainingWithCourses.enrollment.trainingId, 
      newProgress
    ).subscribe({
      next: () => {
        console.log('Progress updated successfully');
      },
      error: (error) => {
        console.error('Error updating progress:', error);
      }
    });
  }

  openCoursePdf(trainingWithCourses: TrainingWithCourses, course: CourseInfo): void {
    this.selectedCourse = course;
    this.selectedTraining = trainingWithCourses;
    
    if (course.chapters && course.chapters.length > 0) {
      const firstChapterWithPdf = course.chapters.find(ch => ch.pdfFilePath);
      if (firstChapterWithPdf) {
        this.showPdfModal = true;
        this.pdfUrl = null; // Reset pour afficher le loading
        document.body.style.overflow = 'hidden';
        
        // Télécharger le PDF avec authentification et créer un Blob URL
        this.courseService.getPdfBlob(course.courseId!, firstChapterWithPdf.chapterId!).subscribe({
          next: (blob) => {
            // Créer une URL Blob pour afficher le PDF
            this.pdfUrl = URL.createObjectURL(blob);
          },
          error: (error) => {
            console.error('Erreur lors du chargement du PDF:', error);
            let errorMessage = 'Erreur lors du chargement du PDF.';
            
            if (error.status === 401) {
              errorMessage = 'Vous devez être connecté pour voir ce PDF.';
            } else if (error.status === 404) {
              errorMessage = 'Le fichier PDF n\'a pas été trouvé.';
            } else if (error.status === 403) {
              errorMessage = 'Vous n\'avez pas l\'autorisation d\'accéder à ce PDF.';
            }
            
            alert(errorMessage);
            this.closePdfModal();
          }
        });
      } else {
        alert('Aucun PDF disponible pour ce cours');
      }
    } else {
      alert('Ce cours n\'a pas encore de contenu PDF');
    }
  }

  closePdfModal(): void {
    // Libérer l'URL Blob pour éviter les fuites mémoire
    if (this.pdfUrl && this.pdfUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.pdfUrl);
    }
    
    this.showPdfModal = false;
    this.pdfUrl = null;
    this.selectedCourse = null;
    this.selectedTraining = null;
    document.body.style.overflow = 'auto';
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

  // Handles both enrollment statuses and session statuses
  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      // Enrollment statuses
      'ENROLLED': 'Inscrit',
      'IN_PROGRESS': 'En cours',
      'COMPLETED': 'Terminé',
      'CANCELLED': 'Annulé',
      // Session statuses
      'SCHEDULED': 'À venir',
      'ONGOING': 'En direct',
    };
    return labels[status] || status;
  }

  toggleTrainingExpansion(trainingData: TrainingWithCourses): void {
    trainingData.isExpanded = !trainingData.isExpanded;
  }

  expandAll(): void {
    this.trainingsWithCourses.forEach(t => t.isExpanded = true);
  }

  collapseAll(): void {
    this.trainingsWithCourses.forEach(t => t.isExpanded = false);
  }

  // Recherche et tri
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.applyFiltersAndSort();
  }

  onSortChange(sortBy: 'name' | 'progress' | 'date'): void {
    if (this.sortBy === sortBy) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortOrder = 'asc';
    }
    this.applyFiltersAndSort();
  }

  applyFiltersAndSort(): void {
    // Filtrer par recherche
    let filtered = this.trainingsWithCourses.filter(t => 
      t.training.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      t.courses.some(c => c.title?.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );

    // Trier
    filtered.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.training.title.localeCompare(b.training.title);
          break;
        case 'progress':
          comparison = a.progress - b.progress;
          break;
        case 'date':
          const dateA = a.enrollment.enrolledAt ? new Date(a.enrollment.enrolledAt).getTime() : 0;
          const dateB = b.enrollment.enrolledAt ? new Date(b.enrollment.enrolledAt).getTime() : 0;
          comparison = dateA - dateB;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredTrainings = filtered;
  }

  get displayedTrainings(): TrainingWithCourses[] {
    return this.filteredTrainings.length > 0 || this.searchTerm ? this.filteredTrainings : this.trainingsWithCourses;
  }

  // Live Sessions Management
  openLiveSessionsModal(course: CourseInfo): void {
    this.currentCourseForSessions = course;
    this.showLiveSessionsModal = true;
    this.loadLiveSessionsForCourse(course.courseId!);
  }

  closeLiveSessionsModal(): void {
    this.showLiveSessionsModal = false;
    this.currentCourseForSessions = null;
    this.liveSessions = [];
  }

  loadLiveSessionsForCourse(courseId: number): void {
    this.loadingLiveSessions = true;
    this.liveSessionService.getSessionsByCourseId(courseId).subscribe({
      next: (sessions) => {
        this.liveSessions = sessions;
        this.loadingLiveSessions = false;
      },
      error: (error) => {
        console.error('Error loading live sessions:', error);
        this.liveSessions = [];
        this.loadingLiveSessions = false;
      }
    });
  }

  joinSession(roomId: string): void {
    this.router.navigate(['/video-conference', roomId]);
  }

  formatDateTime(dateTime: string): string {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSessionDuration(startTime: string, endTime: string): string {
    return this.liveSessionService.getSessionDuration(startTime, endTime);
  }

  isSessionOngoing(session: LiveSessionResponse): boolean {
    return session.status === 'ONGOING';
  }

  isSessionUpcoming(session: LiveSessionResponse): boolean {
    return session.status === 'SCHEDULED';
  }
}
