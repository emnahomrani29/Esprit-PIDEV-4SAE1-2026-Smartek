import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PlanningRequest {
  date: string;         // YYYY-MM-DD
  startTime: string;    // HH:mm:ss
  endTime: string;      // HH:mm:ss
  title: string;
  description?: string;
  eventType: string;
  location?: string;
  color: string;
}

export interface PlanningResponse {
  planningId: number;
  date: string;
  startTime: string;
  endTime: string;
  title: string;
  description: string;
  eventType: string;
  location: string;
  color: string;
  status: string; // SCHEDULED, PUBLISHED, DRAFT, COMPLETED, CANCELLED
}

/** Événement provenant du event-service (via weekly proxy) */
export interface AvailableEvent {
  eventId: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  location: string;
  mode: string;
  status: string;
  physicalCapacity: number;
  onlineCapacity: number;
  physicalRegistered: number;
  onlineRegistered: number;
  price: number;
  isPaid: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  private apiUrl = `${environment.apiUrl}/plannings`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<PlanningResponse[]> {
    return this.http.get<PlanningResponse[]>(this.apiUrl);
  }

  getById(id: number): Observable<PlanningResponse> {
    return this.http.get<PlanningResponse>(`${this.apiUrl}/${id}`);
  }

  getByDateRange(startDate: string, endDate: string): Observable<PlanningResponse[]> {
    return this.http.get<PlanningResponse[]>(`${this.apiUrl}/range?startDate=${startDate}&endDate=${endDate}`);
  }

  create(request: PlanningRequest): Observable<PlanningResponse> {
    return this.http.post<PlanningResponse>(this.apiUrl, request);
  }

  update(id: number, request: PlanningRequest): Observable<PlanningResponse> {
    return this.http.put<PlanningResponse>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  publish(id: number): Observable<PlanningResponse> {
    return this.http.post<PlanningResponse>(`${this.apiUrl}/${id}/publish`, {});
  }

  unpublish(id: number): Observable<PlanningResponse> {
    return this.http.post<PlanningResponse>(`${this.apiUrl}/${id}/unpublish`, {});
  }

  getPublished(): Observable<PlanningResponse[]> {
    return this.http.get<PlanningResponse[]>(`${this.apiUrl}/published`);
  }

  /** Récupère les événements disponibles depuis le event-service (via planning weekly proxy) */
  getAvailableEvents(): Observable<AvailableEvent[]> {
    return this.http.get<AvailableEvent[]>(`${this.apiUrl}/weekly/events`);
  }

  /** Publie toutes les sessions d'une semaine pour un trainer */
  publishWeek(trainerId: number, weekStartDate: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/weekly/publish?trainerId=${trainerId}&weekStartDate=${weekStartDate}`, {}
    );
  }

  /** Dépublie toutes les sessions d'une semaine pour un trainer */
  unpublishWeek(trainerId: number, weekStartDate: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/weekly/unpublish?trainerId=${trainerId}&weekStartDate=${weekStartDate}`, {}
    );
  }
}
