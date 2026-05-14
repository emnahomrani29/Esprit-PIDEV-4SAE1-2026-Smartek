import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Sponsorship, CreateSponsorshipRequest } from './models/sponsor.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SponsorshipService {
  private apiUrl = environment.sponsorshipApiUrl;

  constructor(private http: HttpClient) {}

  getAllSponsorships(): Observable<Sponsorship[]> {
    return this.http.get<Sponsorship[]>(this.apiUrl);
  }

  getSponsorshipById(id: number): Observable<Sponsorship> {
    return this.http.get<Sponsorship>(`${this.apiUrl}/${id}`);
  }

  createSponsorship(request: CreateSponsorshipRequest): Observable<Sponsorship> {
    return this.http.post<Sponsorship>(this.apiUrl, request);
  }

  updateSponsorship(id: number, request: CreateSponsorshipRequest): Observable<Sponsorship> {
    return this.http.put<Sponsorship>(`${this.apiUrl}/${id}`, request);
  }

  deleteSponsorship(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

