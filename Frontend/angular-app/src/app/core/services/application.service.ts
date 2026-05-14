import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application, ApplicationRequest } from '../models/application.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private apiUrl = `${environment.apiUrl}/applications`;

  constructor(private http: HttpClient) {}

  applyToOffer(application: ApplicationRequest): Observable<Application> {
    return this.http.post<Application>(this.apiUrl, application);
  }

  getApplicationsByOffer(offerId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/offer/${offerId}`);
  }

  getApplicationsByLearner(learnerId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/learner/${learnerId}`);
  }

  hasApplied(offerId: number, learnerId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check/${offerId}/${learnerId}`);
  }

  updateApplicationStatus(applicationId: number, status: string): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/${applicationId}/status?status=${status}`, {});
  }
}
