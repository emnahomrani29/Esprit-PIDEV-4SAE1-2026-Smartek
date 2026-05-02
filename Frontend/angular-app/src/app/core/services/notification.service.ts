import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, catchError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  private serviceAvailable = true;

  constructor(private http: HttpClient) {}

  getUnread(learnerId: number): Observable<Notification[]> {
    if (!this.serviceAvailable) {
      return of([]); // Retourner un tableau vide si le service n'est pas disponible
    }
    
    return this.http.get<Notification[]>(`${this.apiUrl}/learner/${learnerId}/unread`).pipe(
      catchError((error) => {
        if (error.status === 503) {
          this.serviceAvailable = false;
          console.warn('Service de notifications indisponible - fonctionnalité désactivée temporairement');
          // Réessayer après 5 minutes
          setTimeout(() => { this.serviceAvailable = true; }, 300000);
        }
        return of([]); // Retourner un tableau vide en cas d'erreur
      })
    );
  }

  getAll(learnerId: number): Observable<Notification[]> {
    if (!this.serviceAvailable) {
      return of([]);
    }
    
    return this.http.get<Notification[]>(`${this.apiUrl}/learner/${learnerId}`).pipe(
      catchError(() => of([]))
    );
  }

  markAsRead(notificationId: number): Observable<void> {
    if (!this.serviceAvailable) {
      return of(void 0);
    }
    
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {}).pipe(
      catchError(() => of(void 0))
    );
  }

  markAllAsRead(learnerId: number): Observable<void> {
    if (!this.serviceAvailable) {
      return of(void 0);
    }
    
    return this.http.put<void>(`${this.apiUrl}/learner/${learnerId}/read-all`, {}).pipe(
      catchError(() => of(void 0))
    );
  }

  delete(notificationId: number): Observable<void> {
    if (!this.serviceAvailable) {
      return of(void 0);
    }
    
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`).pipe(
      catchError(() => of(void 0))
    );
  }
}
