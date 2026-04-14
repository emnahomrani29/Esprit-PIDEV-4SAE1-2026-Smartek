import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Interview, InterviewRequest } from '../models/interview.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = `${environment.apiUrl}/interviews`;

  constructor(private http: HttpClient) {}

  createInterview(interview: InterviewRequest): Observable<Interview> {
    return this.http.post<Interview>(this.apiUrl, interview);
  }

  getAllInterviews(): Observable<Interview[]> {
    return this.http.get<Interview[]>(this.apiUrl);
  }

  getInterviewsByOffer(offerId: number): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.apiUrl}/offer/${offerId}`);
  }

  getInterviewsByLearner(learnerId: number): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.apiUrl}/learner/${learnerId}`);
  }

  getInterviewsByApplication(applicationId: number): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.apiUrl}/application/${applicationId}`);
  }

  updateInterviewStatus(interviewId: number, status: string): Observable<Interview> {
    return this.http.put<Interview>(`${this.apiUrl}/${interviewId}/status?status=${status}`, {});
  }

  updateInterview(interviewId: number, interview: InterviewRequest): Observable<Interview> {
    return this.http.put<Interview>(`${this.apiUrl}/${interviewId}`, interview);
  }

  deleteInterview(interviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${interviewId}`);
  }
}
