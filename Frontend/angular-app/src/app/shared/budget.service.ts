import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BudgetSummary } from './models/sponsor.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private apiUrl = `${environment.apiUrl}/budget`;

  constructor(private http: HttpClient) {}

  getAvailableBudget(contractId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/available/${contractId}`);
  }

  getContractBudgetSummary(contractId: number): Observable<BudgetSummary> {
    return this.http.get<BudgetSummary>(`${this.apiUrl}/summary/contract/${contractId}`);
  }

  getSponsorBudgetSummary(sponsorId: number): Observable<BudgetSummary> {
    return this.http.get<BudgetSummary>(`${this.apiUrl}/summary/sponsor/${sponsorId}`);
  }
}
