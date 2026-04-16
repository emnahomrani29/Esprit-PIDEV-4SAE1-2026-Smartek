import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LiveSessionService } from '../../../core/services/live-session.service';
import { CourseService } from '../../../core/services/course.service';
import { AuthService } from '../../../core/services/auth.service';
import { 
  LiveSessionResponse, 
  LiveSessionRequest, 
  SessionStatus,
  LiveSessionDisplay 
} from '../../../core/models/live-session.model';
import { Course } from '../../../core/models/course.model';

@Component({
  selector: 'app-live-sessions-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './live-sessions-management.component.html',
  styleUrls: ['./live-sessions-management.component.scss']
})
export class LiveSessionsManagementComponent implements OnInit {
  courseId!: number;
  course?: Course;
  sessions: LiveSessionDisplay[] = [];
  filteredSessions: LiveSessionDisplay[] = [];
  selectedFilter: 'all' | 'upcoming' | 'ongoing' | 'completed' = 'all';
  
  showSessionForm = false;
  isEditMode = false;
  selectedSession?: LiveSessionResponse;
  
  sessionForm: LiveSessionRequest = {
    courseId: 0,
    title: '',
    description: '',
    trainerId: 0,
    startTime: '',
    endTime: '',
    maxParticipants: undefined
  };

  loading = false;
  error = '';
  success = '';

  SessionStatus = SessionStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private liveSessionService: LiveSessionService,
    private courseService: CourseService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
    this.loadCourse();
    this.loadSessions();
  }

  loadCourse(): void {
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (course) => {
        this.course = course;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du cours:', err);
        this.error = 'Impossible de charger le cours';
      }
    });
  }

  loadSessions(): void {
    this.loading = true;
    this.liveSessionService.getSessionsByCourseId(this.courseId).subscribe({
      next: (sessions) => {
        this.sessions = sessions.map(session => this.enrichSession(session));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des sessions:', err);
        this.error = 'Impossible de charger les sessions';
        this.loading = false;
      }
    });
  }

  enrichSession(session: LiveSessionResponse): LiveSessionDisplay {
    const isUpcoming = this.liveSessionService.isSessionUpcoming(session.startTime);
    const isOngoing = this.liveSessionService.isSessionOngoing(session.startTime, session.endTime);
    const isPast = this.liveSessionService.isSessionPast(session.endTime);

    return {
      ...session,
      isUpcoming,
      isOngoing,
      isPast,
      timeUntilStart: isUpcoming ? this.liveSessionService.getTimeUntilStart(session.startTime) : undefined,
      duration: this.liveSessionService.getSessionDuration(session.startTime, session.endTime),
      statusLabel: this.liveSessionService.getStatusLabel(session.status),
      statusColor: this.liveSessionService.getStatusColor(session.status)
    };
  }

  applyFilter(): void {
    switch (this.selectedFilter) {
      case 'upcoming':
        this.filteredSessions = this.sessions.filter(s => 
          s.status === SessionStatus.SCHEDULED && s.isUpcoming
        );
        break;
      case 'ongoing':
        this.filteredSessions = this.sessions.filter(s => 
          s.status === SessionStatus.ONGOING
        );
        break;
      case 'completed':
        this.filteredSessions = this.sessions.filter(s => 
          s.status === SessionStatus.COMPLETED || s.isPast
        );
        break;
      default:
        this.filteredSessions = [...this.sessions];
    }
  }

  setFilter(filter: 'all' | 'upcoming' | 'ongoing' | 'completed'): void {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  openCreateForm(): void {
    this.isEditMode = false;
    this.selectedSession = undefined;
    this.resetForm();
    this.showSessionForm = true;
  }

  openEditForm(session: LiveSessionResponse): void {
    this.isEditMode = true;
    this.selectedSession = session;
    this.sessionForm = {
      courseId: session.courseId,
      title: session.title,
      description: session.description || '',
      trainerId: session.trainerId,
      startTime: this.formatDateTimeForInput(session.startTime),
      endTime: this.formatDateTimeForInput(session.endTime),
      maxParticipants: session.maxParticipants
    };
    this.showSessionForm = true;
  }

  closeForm(): void {
    this.showSessionForm = false;
    this.resetForm();
  }

  resetForm(): void {
    const currentUser = this.authService.getUserInfo();
    this.sessionForm = {
      courseId: this.courseId,
      title: '',
      description: '',
      trainerId: currentUser?.userId || 0,
      startTime: '',
      endTime: '',
      maxParticipants: undefined
    };
  }

  formatDateTimeForInput(dateTime: string): string {
    // Convertit "2026-04-20T10:00:00" en format compatible avec input datetime-local
    return dateTime.substring(0, 16);
  }

  saveSession(): void {
    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    const request: LiveSessionRequest = {
      ...this.sessionForm,
      startTime: this.sessionForm.startTime + ':00',
      endTime: this.sessionForm.endTime + ':00'
    };

    const operation = this.isEditMode && this.selectedSession
      ? this.liveSessionService.updateSession(this.selectedSession.sessionId, request)
      : this.liveSessionService.createSession(request);

    operation.subscribe({
      next: (response) => {
        this.success = response.message || (this.isEditMode ? 'Session mise à jour' : 'Session créée');
        this.loading = false;
        this.closeForm();
        this.loadSessions();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        console.error('Erreur lors de la sauvegarde:', err);
        this.error = err.error?.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  validateForm(): boolean {
    if (!this.sessionForm.title.trim()) {
      this.error = 'Le titre est obligatoire';
      return false;
    }
    if (!this.sessionForm.startTime) {
      this.error = 'La date de début est obligatoire';
      return false;
    }
    if (!this.sessionForm.endTime) {
      this.error = 'La date de fin est obligatoire';
      return false;
    }
    if (this.sessionForm.endTime <= this.sessionForm.startTime) {
      this.error = 'La date de fin doit être après la date de début';
      return false;
    }
    return true;
  }

  updateStatus(sessionId: number, status: SessionStatus): void {
    this.loading = true;
    this.liveSessionService.updateSessionStatus(sessionId, status).subscribe({
      next: (response) => {
        this.success = response.message || 'Statut mis à jour';
        this.loadSessions();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        console.error('Erreur lors de la mise à jour du statut:', err);
        this.error = err.error?.message || 'Erreur lors de la mise à jour';
        this.loading = false;
      }
    });
  }

  deleteSession(sessionId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette session ?')) {
      return;
    }

    this.loading = true;
    this.liveSessionService.deleteSession(sessionId).subscribe({
      next: () => {
        this.success = 'Session supprimée';
        this.loadSessions();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        this.error = 'Erreur lors de la suppression';
        this.loading = false;
      }
    });
  }

  joinSession(roomId: string): void {
    // Naviguer vers la salle de visioconférence
    this.router.navigate(['/video-conference', roomId]);
  }

  goBack(): void {
    this.router.navigate(['/trainer/courses']);
  }

  getFilterLabel(): string {
    const labels: Record<string, string> = {
      'upcoming': 'à venir',
      'ongoing': 'en cours',
      'completed': 'terminées'
    };
    return labels[this.selectedFilter] || '';
  }
}
