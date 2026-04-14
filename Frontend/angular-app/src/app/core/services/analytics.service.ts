import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LearnerAnalytics {
  learnerId: number;
  learnerName: string;
  totalCourses: number;
  completedCourses: number;
  totalExams: number;
  passedExams: number;
  averageScore: number;
}

export interface TrainingAnalytics {
  trainingId: number;
  trainingName: string;
  trainingTitle?: string;
  totalLearners: number;
  completedLearners: number;
  averageProgress: number;
  averageScore: number;
  totalEnrollments?: number;
  activeEnrollments?: number;
  completedEnrollments?: number;
}

export interface ExamAnalytics {
  examId: number;
  examTitle: string;
  learnerName: string;
  learnerId: number;
  score: number;
  maxScore: number;
  percentage: number;
  status: string;
  completedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getLearnerAnalytics(trainerId: number): Observable<LearnerAnalytics[]> {
    return this.http.get<LearnerAnalytics[]>(`${this.apiUrl}/trainer/${trainerId}/learners`);
  }

  getTrainingAnalytics(trainerId: number): Observable<TrainingAnalytics[]> {
    return this.http.get<TrainingAnalytics[]>(`${this.apiUrl}/trainer/${trainerId}/trainings`);
  }

  getTrainerExamAnalytics(trainerId: number): Observable<ExamAnalytics[]> {
    return this.http.get<ExamAnalytics[]>(`${this.apiUrl}/trainer/${trainerId}/exams`);
  }

  getTrainerTrainingAnalytics(trainerId: number): Observable<TrainingAnalytics[]> {
    return this.http.get<TrainingAnalytics[]>(`${this.apiUrl}/trainer/${trainerId}/training-enrollments`);
  }
}
