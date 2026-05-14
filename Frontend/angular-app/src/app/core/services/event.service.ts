import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EventRequest {
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  location: string;
  maxParticipations: number;
  physicalCapacity?: number;
  onlineCapacity?: number;
  mode?: string;
  price?: number;
  isPaid?: boolean;
  createdBy?: number;
}

export interface EventResponse {
  eventId: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  location: string;
  physicalCapacity: number;
  onlineCapacity: number;
  physicalRegistered: number;
  onlineRegistered: number;
  maxParticipations: number;
  currentParticipations: number;
  status: string;
  mode: string;
  price: number;
  isPaid: boolean;
  createdBy: number;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EventRegistration {
  registrationId: number;
  eventId: number;
  userId: number;
  status: string;
  paymentStatus: string;
  participationMode: string;
  registeredAt: string;
}

export interface EventRevenueResponse {
  eventId: number;
  totalRevenue: number;
  potentialRevenue: number;
  paidRegistrations: number;
  pendingPayments: number;
  averageRevenuePerParticipant: number;
}

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<EventResponse[]> {
    return this.http.get<EventResponse[]>(this.apiUrl);
  }

  getUpcomingEvents(): Observable<EventResponse[]> {
    return this.http.get<EventResponse[]>(`${this.apiUrl}/upcoming`);
  }

  getEventById(id: number): Observable<EventResponse> {
    return this.http.get<EventResponse>(`${this.apiUrl}/${id}`);
  }

  createEvent(request: EventRequest): Observable<EventResponse> {
    return this.http.post<EventResponse>(this.apiUrl, request);
  }

  updateEvent(id: number, request: EventRequest): Observable<EventResponse> {
    return this.http.put<EventResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  changeStatus(eventId: number, newStatus: string, changedBy: number, reason?: string): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/events/business/${eventId}/status`, {
      newStatus,
      changedBy,
      reason
    });
  }

  // Inscription à un événement
  registerForEvent(eventId: number, userId: number, mode: string = 'PHYSICAL'): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/events/business/register`, {
      eventId,
      userId,
      participationMode: mode
    });
  }

  // Récupérer les inscriptions d'un utilisateur
  getUserRegistrations(userId: number): Observable<EventRegistration[]> {
    return this.http.get<EventRegistration[]>(`${environment.apiUrl}/events/business/user/${userId}/registrations`);
  }

  // Annuler une inscription
  cancelRegistration(registrationId: number, userId: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/events/business/registrations/${registrationId}?userId=${userId}`);
  }

  // Créer une session de paiement Stripe
  createCheckoutSession(
    registrationId: number,
    eventId: number,
    userId: number,
    amount: number,
    eventTitle: string
  ): Observable<{ sessionId: string; checkoutUrl: string }> {
    return this.http.post<{ sessionId: string; checkoutUrl: string }>(
      `${environment.apiUrl}/events/payment/create-checkout-session`,
      { registrationId, eventId, userId, amount, currency: 'usd', eventTitle }
    );
  }

  // Vérifier le statut d'un paiement
  verifyPayment(registrationId: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/events/payment/verify?registrationId=${registrationId}`);
  }

  // Récupérer les revenus d'un événement
  getEventRevenue(eventId: number): Observable<EventRevenueResponse> {
    return this.http.get<EventRevenueResponse>(`${environment.apiUrl}/events/business/${eventId}/revenue`);
  }

  // Confirmer le paiement d'une inscription (fallback si webhook Stripe non reçu)
  confirmPayment(registrationId: number): Observable<any> {
    return this.http.post<any>(
      `${environment.apiUrl}/events/business/registrations/${registrationId}/confirm-payment`,
      {}
    );
  }
}
