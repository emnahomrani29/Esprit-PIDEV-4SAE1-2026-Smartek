import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SkillEvidenceRequest, SkillEvidenceResponse,
  ApprovalRequest, RejectionRequest,
  LearnerAnalytics, GlobalAnalytics, EvidenceStatus, EvidenceCategory
} from '../models/skill-evidence.model';

@Injectable({ providedIn: 'root' })
export class SkillEvidenceService {
  private apiUrl = `${environment.apiUrl}/skill-evidence`;
  private directUrl = 'http://localhost:8091/api/skill-evidence';

  constructor(private http: HttpClient) {}

  create(req: SkillEvidenceRequest): Observable<SkillEvidenceResponse> {
    return this.http.post<SkillEvidenceResponse>(this.apiUrl, req);
  }

  getAll(): Observable<SkillEvidenceResponse[]> {
    return this.http.get<SkillEvidenceResponse[]>(this.apiUrl);
  }

  getByLearner(learnerId: number): Observable<SkillEvidenceResponse[]> {
    return this.http.get<SkillEvidenceResponse[]>(`${this.apiUrl}/learner/${learnerId}`);
  }

  getById(id: number): Observable<SkillEvidenceResponse> {
    return this.http.get<SkillEvidenceResponse>(`${this.apiUrl}/${id}`);
  }

  update(id: number, req: SkillEvidenceRequest): Observable<SkillEvidenceResponse> {
    return this.http.put<SkillEvidenceResponse>(`${this.apiUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  approve(id: number, req: ApprovalRequest): Observable<SkillEvidenceResponse> {
    return this.http.post<SkillEvidenceResponse>(`${this.apiUrl}/${id}/approve`, req);
  }

  reject(id: number, req: RejectionRequest): Observable<SkillEvidenceResponse> {
    return this.http.post<SkillEvidenceResponse>(`${this.apiUrl}/${id}/reject`, req);
  }

  getLearnerAnalytics(learnerId: number): Observable<LearnerAnalytics> {
    return this.http.get<LearnerAnalytics>(`${this.apiUrl}/analytics/learner/${learnerId}`);
  }

  getGlobalAnalytics(): Observable<GlobalAnalytics> {
    return this.http.get<GlobalAnalytics>(`${this.apiUrl}/analytics/global`);
  }

  getByStatus(status: EvidenceStatus): Observable<SkillEvidenceResponse[]> {
    return this.http.get<SkillEvidenceResponse[]>(`${this.apiUrl}/status/${status}`);
  }

  getByCategory(category: EvidenceCategory): Observable<SkillEvidenceResponse[]> {
    return this.http.get<SkillEvidenceResponse[]>(`${this.apiUrl}/category/${category}`);
  }
}
