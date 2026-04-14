import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PerformanceStats {
  courses: {
    totalEnrolled: number;
    inProgress: number;
    completed: number;
    completionRate: number;
    totalChapters: number;
    completedChapters: number;
  };
  trainings: {
    totalEnrolled: number;
    inProgress: number;
    completed: number;
    averageProgress: number;
    statusBreakdown: { [key: string]: number };
  };
  exams: {
    totalAvailable: number;
    attempted: number;
    passed: number;
    failed: number;
    averageScore: number;
    successRate: number;
    totalAttempts: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  private apiUrl = `${environment.apiUrl}/performance`;

  constructor(private http: HttpClient) {}

  getUserPerformance(userId: number): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${this.apiUrl}/user/${userId}`);
  }

  getAllStats(userId: number): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${this.apiUrl}/user/${userId}/stats`);
  }
}
