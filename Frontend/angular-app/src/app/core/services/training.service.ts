import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Training, TrainingCreateRequest, TrainingUpdateRequest } from '../models/training.model';

@Injectable({
  providedIn: 'root'
})
export class TrainingService {
  private apiUrl = `${environment.apiUrl}/trainings`;

  constructor(private http: HttpClient) {}

  getAllTrainings(): Observable<Training[]> {
    return this.http.get<Training[]>(this.apiUrl);
  }

  getTrainingById(id: number): Observable<Training> {
    return this.http.get<Training>(`${this.apiUrl}/${id}`);
  }

  getTrainingsByCategory(category: string): Observable<Training[]> {
    return this.http.get<Training[]>(`${this.apiUrl}/category/${category}`);
  }

  getTrainingsByLevel(level: string): Observable<Training[]> {
    return this.http.get<Training[]>(`${this.apiUrl}/level/${level}`);
  }

  createTraining(training: TrainingCreateRequest): Observable<Training> {
    return this.http.post<Training>(this.apiUrl, training);
  }

  updateTraining(id: number, training: TrainingUpdateRequest): Observable<Training> {
    return this.http.put<Training>(`${this.apiUrl}/${id}`, training);
  }

  addCourseToTraining(trainingId: number, courseId: number): Observable<Training> {
    return this.http.post<Training>(`${this.apiUrl}/${trainingId}/courses/${courseId}`, {});
  }

  removeCourseFromTraining(trainingId: number, courseId: number): Observable<Training> {
    return this.http.delete<Training>(`${this.apiUrl}/${trainingId}/courses/${courseId}`);
  }

  deleteTraining(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
