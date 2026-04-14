import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, EventRequest } from '../models/event.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = 'http://localhost:8090/events';

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  getUpcomingEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/upcoming`);
  }

  createEvent(event: EventRequest): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, event);
  }

  updateEvent(id: number, event: EventRequest): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${id}`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  registerParticipation(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/${id}/register`, {});
  }

  cancelParticipation(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/${id}/cancel`, {});
  }

  // Inscription avancée avec logique métier
  registerForEventAdvanced(eventId: number, userId: number, participationMode: string): Observable<any> {
    const requestBody = {
      eventId: eventId,
      userId: userId,
      participationMode: participationMode
    };
    console.log('Registration request:', requestBody);
    return this.http.post(`${this.apiUrl}/business/register`, requestBody);
  }

  // Annuler une inscription avancée
  cancelRegistrationAdvanced(registrationId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/business/registrations/${registrationId}?userId=${userId}`);
  }

  // Récupérer les inscriptions d'un utilisateur
  getUserRegistrations(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/business/user/${userId}/registrations`);
  }

  changeEventStatus(id: number, newStatus: string, reason?: string, changedBy?: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/business/${id}/status`, {
      newStatus,
      reason: reason || `Status changed to ${newStatus}`,
      changedBy: changedBy || 1
    });
  }
}
