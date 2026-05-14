import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Sponsorship, StatusSummary, ApprovalRequest } from './models/sponsor.model';
import { environment } from '../../environments/environment';

interface ExpiringContract {
  id: number;
  contractNumber: string;
  endDate: string;
  sponsorName: string;
}

@Injectable({ providedIn: 'root' })
export class WorkflowService {
  private apiUrl = `${environment.apiUrl}/workflow`;

  constructor(private http: HttpClient) {}

  approveSponsorship(sponsorshipId: number, adminId: number): Observable<Sponsorship> {
    return this.http.post<Sponsorship>(
      `${this.apiUrl}/sponsorships/${sponsorshipId}/approve?adminId=${adminId}`,
      {}
    );
  }

  rejectSponsorship(sponsorshipId: number, request: ApprovalRequest): Observable<Sponsorship> {
    return this.http.post<Sponsorship>(
      `${this.apiUrl}/sponsorships/${sponsorshipId}/reject`,
      request
    );
  }

  cancelSponsorship(sponsorshipId: number): Observable<Sponsorship> {
    return this.http.post<Sponsorship>(
      `${this.apiUrl}/sponsorships/${sponsorshipId}/cancel`,
      {}
    );
  }

  getPendingSponsorships(): Observable<Sponsorship[]> {
    return this.http.get<Sponsorship[]>(`${this.apiUrl}/sponsorships/pending`);
  }

  getPendingCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/sponsorships/pending/count`);
  }

  getUrgentPendingCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/sponsorships/urgent-pending/count`);
  }

  getStatusSummary(sponsorId: number): Observable<StatusSummary> {
    return this.http.get<StatusSummary>(`${this.apiUrl}/sponsorships/status-summary/${sponsorId}`);
  }

  getRecentPending(): Observable<Sponsorship[]> {
    return this.http.get<Sponsorship[]>(`${this.apiUrl}/sponsorships/recent-pending`);
  }

  getExpiringContracts(): Observable<ExpiringContract[]> {
    return this.http.get<ExpiringContract[]>(`${this.apiUrl}/contracts/expiring`);
  }

  getSponsorExpiringContracts(sponsorId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/contracts/expiring/count/${sponsorId}`);
  }
}
