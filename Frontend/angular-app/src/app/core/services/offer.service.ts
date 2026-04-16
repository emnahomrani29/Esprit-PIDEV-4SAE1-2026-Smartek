import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Offer, OfferRequest } from '../models/offer.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OfferService {
  private apiUrl = `${environment.apiUrl}/offers`;

  constructor(private http: HttpClient) {}

  getAllOffers(): Observable<Offer[]> {
    // POST /search is public and returns paginated results
    return this.http.post<any>(`${this.apiUrl}/search`, {
      page: 0, size: 100, sortBy: 'createdAt', sortDir: 'desc'
    }).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    );
  }

  getOfferById(id: number): Observable<Offer> {
    return this.http.get<Offer>(`${this.apiUrl}/${id}`);
  }

  getOffersByCompanyId(companyId: number): Observable<Offer[]> {
    return this.http.get<any>(`${this.apiUrl}/company/${companyId}`).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    );
  }

  getOffersByStatus(status: string): Observable<Offer[]> {
    return this.http.get<any>(`${this.apiUrl}/status/${status}`).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    );
  }

  createOffer(offer: OfferRequest): Observable<Offer> {
    return this.http.post<Offer>(this.apiUrl, offer);
  }

  updateOffer(id: number, offer: OfferRequest): Observable<Offer> {
    return this.http.put<Offer>(`${this.apiUrl}/${id}`, offer);
  }

  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
