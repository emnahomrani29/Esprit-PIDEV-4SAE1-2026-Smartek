import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BadgeTemplate, EarnedBadge, AwardBadgeRequest, BulkAwardBadgeRequest } from '../models/badge.model';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class BadgeService {
  private apiUrl = 'http://localhost:8083/api/certifications-badges/badge-templates';
  private earnedApiUrl = 'http://localhost:8083/api/certifications-badges/earned-badges';

  constructor(private http: HttpClient) {}

  // Badge Template CRUD
  getAllTemplates(): Observable<BadgeTemplate[]> {
    return this.http.get<BadgeTemplate[]>(this.apiUrl);
  }

  getTemplatesPaginated(page: number = 0, size: number = 10, sortBy: string = 'id', sortDirection: string = 'DESC'): Observable<PageResponse<BadgeTemplate>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<PageResponse<BadgeTemplate>>(`${this.apiUrl}/paginated`, { params });
  }

  getActiveTemplates(): Observable<BadgeTemplate[]> {
    return this.http.get<BadgeTemplate[]>(`${this.apiUrl}/active`);
  }

  getTemplateById(id: number): Observable<BadgeTemplate> {
    return this.http.get<BadgeTemplate>(`${this.apiUrl}/${id}`);
  }

  createTemplate(template: BadgeTemplate): Observable<BadgeTemplate> {
    return this.http.post<BadgeTemplate>(this.apiUrl, template);
  }

  updateTemplate(id: number, template: BadgeTemplate): Observable<BadgeTemplate> {
    return this.http.put<BadgeTemplate>(`${this.apiUrl}/${id}`, template);
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Earned Badges
  getAllEarnedBadges(): Observable<EarnedBadge[]> {
    return this.http.get<EarnedBadge[]>(this.earnedApiUrl);
  }

  getEarnedBadgeById(id: number): Observable<EarnedBadge> {
    return this.http.get<EarnedBadge>(`${this.earnedApiUrl}/${id}`);
  }

  getBadgesByLearner(learnerId: number): Observable<EarnedBadge[]> {
    return this.http.get<EarnedBadge[]>(`${this.earnedApiUrl}/learner/${learnerId}`);
  }

  getBadgesByLearnerPaginated(learnerId: number, page: number = 0, size: number = 10, sortBy: string = 'awardDate', sortDirection: string = 'DESC'): Observable<PageResponse<EarnedBadge>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<PageResponse<EarnedBadge>>(`${this.earnedApiUrl}/learner/${learnerId}/paginated`, { params });
  }

  getBadgesByTemplate(templateId: number): Observable<EarnedBadge[]> {
    return this.http.get<EarnedBadge[]>(`${this.earnedApiUrl}/template/${templateId}`);
  }

  awardBadge(request: AwardBadgeRequest): Observable<EarnedBadge> {
    return this.http.post<EarnedBadge>(this.earnedApiUrl, request);
  }

  bulkAwardBadge(request: BulkAwardBadgeRequest): Observable<any> {
    return this.http.post<any>(`${this.earnedApiUrl}/award/bulk`, request);
  }

  revokeBadge(id: number): Observable<void> {
    return this.http.delete<void>(`${this.earnedApiUrl}/${id}`);
  }
}
