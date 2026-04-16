import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  LiveSessionRequest, 
  LiveSessionResponse, 
  SessionStatus 
} from '../models/live-session.model';

@Injectable({
  providedIn: 'root'
})
export class LiveSessionService {
  private apiUrl = `${environment.apiUrl}/courses`;

  constructor(private http: HttpClient) {}

  /**
   * Crée une nouvelle session live
   */
  createSession(request: LiveSessionRequest): Observable<LiveSessionResponse> {
    return this.http.post<LiveSessionResponse>(`${this.apiUrl}/sessions`, request);
  }

  /**
   * Récupère toutes les sessions d'un cours
   */
  getSessionsByCourseId(courseId: number): Observable<LiveSessionResponse[]> {
    return this.http.get<LiveSessionResponse[]>(`${this.apiUrl}/course/${courseId}/sessions`);
  }

  /**
   * Récupère les sessions à venir d'un cours
   */
  getUpcomingSessionsByCourseId(courseId: number): Observable<LiveSessionResponse[]> {
    return this.http.get<LiveSessionResponse[]>(`${this.apiUrl}/course/${courseId}/sessions/upcoming`);
  }

  /**
   * Récupère les sessions en cours d'un cours
   */
  getOngoingSessionsByCourseId(courseId: number): Observable<LiveSessionResponse[]> {
    return this.http.get<LiveSessionResponse[]>(`${this.apiUrl}/course/${courseId}/sessions/ongoing`);
  }

  /**
   * Récupère une session par son ID
   */
  getSessionById(sessionId: number): Observable<LiveSessionResponse> {
    return this.http.get<LiveSessionResponse>(`${this.apiUrl}/sessions/${sessionId}`);
  }

  /**
   * Met à jour une session
   */
  updateSession(sessionId: number, request: LiveSessionRequest): Observable<LiveSessionResponse> {
    return this.http.put<LiveSessionResponse>(`${this.apiUrl}/sessions/${sessionId}`, request);
  }

  /**
   * Change le statut d'une session
   */
  updateSessionStatus(sessionId: number, status: SessionStatus): Observable<LiveSessionResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<LiveSessionResponse>(
      `${this.apiUrl}/sessions/${sessionId}/status`, 
      null, 
      { params }
    );
  }

  /**
   * Supprime une session
   */
  deleteSession(sessionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/sessions/${sessionId}`);
  }

  /**
   * Récupère toutes les sessions d'un trainer
   */
  getSessionsByTrainerId(trainerId: number): Observable<LiveSessionResponse[]> {
    return this.http.get<LiveSessionResponse[]>(`${this.apiUrl}/trainer/${trainerId}/sessions`);
  }

  /**
   * Calcule le temps restant avant le début d'une session
   */
  getTimeUntilStart(startTime: string): string {
    const now = new Date();
    const start = new Date(startTime);
    const diff = start.getTime() - now.getTime();

    if (diff <= 0) {
      return 'Maintenant';
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (days > 0) {
      return `Dans ${days} jour${days > 1 ? 's' : ''}`;
    } else if (hours > 0) {
      return `Dans ${hours}h ${minutes}min`;
    } else {
      return `Dans ${minutes} min`;
    }
  }

  /**
   * Calcule la durée d'une session
   */
  getSessionDuration(startTime: string, endTime: string): string {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diff = end.getTime() - start.getTime();

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (hours > 0) {
      return `${hours}h ${minutes}min`;
    } else {
      return `${minutes} min`;
    }
  }

  /**
   * Vérifie si une session est en cours
   */
  isSessionOngoing(startTime: string, endTime: string): boolean {
    const now = new Date();
    const start = new Date(startTime);
    const end = new Date(endTime);
    return now >= start && now <= end;
  }

  /**
   * Vérifie si une session est à venir
   */
  isSessionUpcoming(startTime: string): boolean {
    const now = new Date();
    const start = new Date(startTime);
    return start > now;
  }

  /**
   * Vérifie si une session est passée
   */
  isSessionPast(endTime: string): boolean {
    const now = new Date();
    const end = new Date(endTime);
    return end < now;
  }

  /**
   * Obtient le label du statut en français
   */
  getStatusLabel(status: SessionStatus): string {
    const labels: Record<SessionStatus, string> = {
      [SessionStatus.SCHEDULED]: 'À venir',
      [SessionStatus.ONGOING]: 'En direct',
      [SessionStatus.COMPLETED]: 'Terminée',
      [SessionStatus.CANCELLED]: 'Annulée'
    };
    return labels[status] || status;
  }

  /**
   * Obtient la couleur du badge de statut
   */
  getStatusColor(status: SessionStatus): string {
    const colors: Record<SessionStatus, string> = {
      [SessionStatus.SCHEDULED]: 'primary',
      [SessionStatus.ONGOING]: 'danger',
      [SessionStatus.COMPLETED]: 'secondary',
      [SessionStatus.CANCELLED]: 'warning'
    };
    return colors[status] || 'secondary';
  }
}
