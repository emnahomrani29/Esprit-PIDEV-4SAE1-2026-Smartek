import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CertificationTemplate, EarnedCertification, AwardCertificationRequest, BulkAwardCertificationRequest } from '../models/certification.model';

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
export class CertificationService {
  private apiUrl = 'http://localhost:8083/api/certifications-badges/certification-templates';
  private earnedApiUrl = 'http://localhost:8083/api/certifications-badges/earned-certifications';

  constructor(private http: HttpClient) {}

  // Certification Template CRUD
  getAllTemplates(): Observable<CertificationTemplate[]> {
    return this.http.get<CertificationTemplate[]>(this.apiUrl);
  }

  getTemplatesPaginated(page: number = 0, size: number = 10, sortBy: string = 'id', sortDirection: string = 'DESC'): Observable<PageResponse<CertificationTemplate>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<PageResponse<CertificationTemplate>>(`${this.apiUrl}/paginated`, { params });
  }

  getActiveTemplates(): Observable<CertificationTemplate[]> {
    return this.http.get<CertificationTemplate[]>(`${this.apiUrl}/active`);
  }

  getTemplateById(id: number): Observable<CertificationTemplate> {
    return this.http.get<CertificationTemplate>(`${this.apiUrl}/${id}`);
  }

  createTemplate(template: CertificationTemplate): Observable<CertificationTemplate> {
    return this.http.post<CertificationTemplate>(this.apiUrl, template);
  }

  updateTemplate(id: number, template: CertificationTemplate): Observable<CertificationTemplate> {
    return this.http.put<CertificationTemplate>(`${this.apiUrl}/${id}`, template);
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Earned Certifications
  getAllEarnedCertifications(): Observable<EarnedCertification[]> {
    return this.http.get<EarnedCertification[]>(this.earnedApiUrl);
  }

  getEarnedCertificationById(id: number): Observable<EarnedCertification> {
    return this.http.get<EarnedCertification>(`${this.earnedApiUrl}/${id}/details`);
  }

  getCertificationsByLearner(learnerId: number): Observable<EarnedCertification[]> {
    return this.http.get<EarnedCertification[]>(`${this.earnedApiUrl}/learner/${learnerId}`);
  }

  getCertificationsByLearnerPaginated(learnerId: number, page: number = 0, size: number = 10, sortBy: string = 'issueDate', sortDirection: string = 'DESC'): Observable<PageResponse<EarnedCertification>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<PageResponse<EarnedCertification>>(`${this.earnedApiUrl}/learner/${learnerId}/paginated`, { params });
  }

  getCertificationsByTemplate(templateId: number): Observable<EarnedCertification[]> {
    return this.http.get<EarnedCertification[]>(`${this.earnedApiUrl}/template/${templateId}`);
  }

  awardCertification(request: AwardCertificationRequest): Observable<EarnedCertification> {
    return this.http.post<EarnedCertification>(this.earnedApiUrl, request);
  }

  bulkAwardCertification(request: BulkAwardCertificationRequest): Observable<any> {
    return this.http.post<any>(`${this.earnedApiUrl}/award/bulk`, request);
  }

  revokeCertification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.earnedApiUrl}/${id}`);
  }
}
