import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  getUnread(learnerId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/learner/${learnerId}/unread`);
  }

  getAll(learnerId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/learner/${learnerId}`);
  }

  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  markAllAsRead(learnerId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/learner/${learnerId}/read-all`, {});
  }

  delete(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }
}
