import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LearningStylePreferenceRequest, LearningStylePreferenceResponse } from '../models/learning-style.model';

@Injectable({ providedIn: 'root' })
export class LearningStyleService {
  private apiUrl = `${environment.apiUrl}/learning-style-preferences`;

  constructor(private http: HttpClient) {}

  save(req: LearningStylePreferenceRequest): Observable<LearningStylePreferenceResponse> {
    return this.http.post<LearningStylePreferenceResponse>(this.apiUrl, req);
  }

  getByLearner(learnerId: number): Observable<LearningStylePreferenceResponse> {
    return this.http.get<LearningStylePreferenceResponse>(`${this.apiUrl}/learner/${learnerId}`);
  }

  exists(learnerId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/learner/${learnerId}/exists`);
  }

  delete(learnerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/learner/${learnerId}`);
  }
}
