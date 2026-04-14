import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TrainingEnrollmentRequest {
  trainingId: number;
  userId: number;
}

export interface TrainingEnrollmentResponse {
  id: number;
  trainingId: number;
  trainingTitle: string;
  userId: number;
  enrolledAt: string;
  isActive: boolean;
  progress: number;
  completedAt?: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class TrainingEnrollmentService {
  private apiUrl = `${environment.apiUrl}/trainings/enrollments`;

  constructor(private http: HttpClient) {}

  enrollUser(request: TrainingEnrollmentRequest): Observable<TrainingEnrollmentResponse> {
    return this.http.post<TrainingEnrollmentResponse>(this.apiUrl, request);
  }

  getUserEnrollments(userId: number): Observable<TrainingEnrollmentResponse[]> {
    return this.http.get<TrainingEnrollmentResponse[]>(`${this.apiUrl}/user/${userId}`);
  }

  getTrainingEnrollments(trainingId: number): Observable<TrainingEnrollmentResponse[]> {
    return this.http.get<TrainingEnrollmentResponse[]>(`${this.apiUrl}/training/${trainingId}`);
  }

  unenrollUser(userId: number, trainingId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/user/${userId}/training/${trainingId}`);
  }

  updateProgress(userId: number, trainingId: number, progress: number): Observable<TrainingEnrollmentResponse> {
    return this.http.put<TrainingEnrollmentResponse>(
      `${this.apiUrl}/user/${userId}/training/${trainingId}/progress`, 
      { progress }
    );
  }
}
