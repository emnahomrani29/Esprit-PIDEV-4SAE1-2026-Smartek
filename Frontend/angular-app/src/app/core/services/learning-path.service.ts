import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LearningPathRequest, LearningPathResponse, LearningPathStatus } from '../models/learning-path.model';

@Injectable({ providedIn: 'root' })
export class LearningPathService {
  private apiUrl = `${environment.apiUrl}/learning-paths`;

  constructor(private http: HttpClient) {}

  create(req: LearningPathRequest): Observable<LearningPathResponse> {
    return this.http.post<LearningPathResponse>(this.apiUrl, req);
  }

  getAll(): Observable<LearningPathResponse[]> {
    return this.http.get<LearningPathResponse[]>(this.apiUrl);
  }

  getByLearner(learnerId: number): Observable<LearningPathResponse[]> {
    return this.http.get<LearningPathResponse[]>(`${this.apiUrl}/learner/${learnerId}`);
  }

  getById(id: number): Observable<LearningPathResponse> {
    return this.http.get<LearningPathResponse>(`${this.apiUrl}/${id}`);
  }

  update(id: number, req: LearningPathRequest): Observable<LearningPathResponse> {
    return this.http.put<LearningPathResponse>(`${this.apiUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByStatus(status: LearningPathStatus): Observable<LearningPathResponse[]> {
    return this.http.get<LearningPathResponse[]>(`${this.apiUrl}/status/${status}`);
  }
}
