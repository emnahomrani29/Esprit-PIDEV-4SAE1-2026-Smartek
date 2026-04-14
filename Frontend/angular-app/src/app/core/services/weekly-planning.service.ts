import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { 
  WeeklyPlanningRequest, 
  WeeklyPlanningResponse, 
  TrainingItem, 
  ExamItem, 
  EventItem 
} from '../models/weekly-planning.model';

@Injectable({
  providedIn: 'root'
})
export class WeeklyPlanningService {
  private planningApiUrl = 'http://localhost:8090/plannings/weekly';
  private trainingApiUrl = 'http://localhost:8090/trainings';
  private examApiUrl = 'http://localhost:8090/exams';
  private eventApiUrl = 'http://localhost:8090/events';

  constructor(private http: HttpClient) {}

  // Planning hebdomadaire
  getWeeklyPlanning(trainerId: number, weekStartDate: string): Observable<WeeklyPlanningResponse> {
    return this.http.get<WeeklyPlanningResponse>(
      `${this.planningApiUrl}/trainer/${trainerId}?weekStartDate=${weekStartDate}`
    );
  }

  createOrUpdateWeeklyPlanning(request: WeeklyPlanningRequest): Observable<WeeklyPlanningResponse> {
    return this.http.post<WeeklyPlanningResponse>(this.planningApiUrl, request);
  }

  publishWeeklyPlanning(trainerId: number, weekStartDate: string): Observable<WeeklyPlanningResponse> {
    return this.http.post<WeeklyPlanningResponse>(
      `${this.planningApiUrl}/publish?trainerId=${trainerId}&weekStartDate=${weekStartDate}`, {}
    );
  }

  unpublishWeeklyPlanning(trainerId: number, weekStartDate: string): Observable<WeeklyPlanningResponse> {
    return this.http.post<WeeklyPlanningResponse>(
      `${this.planningApiUrl}/unpublish?trainerId=${trainerId}&weekStartDate=${weekStartDate}`, {}
    );
  }

  // Récupération des données pour le planning
  getTrainings(): Observable<TrainingItem[]> {
    return this.http.get<TrainingItem[]>(`${this.planningApiUrl}/trainings`);
  }

  getExams(): Observable<ExamItem[]> {
    return this.http.get<ExamItem[]>(`${this.planningApiUrl}/exams`);
  }

  getEvents(): Observable<EventItem[]> {
    return this.http.get<EventItem[]>(`${this.planningApiUrl}/events`);
  }

  // Méthodes pour les learners
  getPublishedPlannings(weekStartDate: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.planningApiUrl}/published?weekStartDate=${weekStartDate}`);
  }

  // Méthodes d'inscription
  registerToSession(planningId: number, learnerId: number): Observable<string> {
    return this.http.post(`${this.planningApiUrl}/register?planningId=${planningId}&learnerId=${learnerId}`, {}, 
      { responseType: 'text' as 'json' }).pipe(
      map((response: any) => response as string)
    );
  }

  unregisterFromSession(planningId: number, learnerId: number): Observable<string> {
    return this.http.post(`${this.planningApiUrl}/unregister?planningId=${planningId}&learnerId=${learnerId}`, {}, 
      { responseType: 'text' as 'json' }).pipe(
      map((response: any) => response as string)
    );
  }

  isRegistered(planningId: number, learnerId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.planningApiUrl}/is-registered?planningId=${planningId}&learnerId=${learnerId}`);
  }

  getLearnerRegistrations(learnerId: number, weekStartDate: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.planningApiUrl}/learner/${learnerId}/registrations?weekStartDate=${weekStartDate}`);
  }
}