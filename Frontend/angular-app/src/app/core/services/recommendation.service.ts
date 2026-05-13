import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RecommendationItem {
  pathId: number;
  title: string;
  score: number;
  reason: string;
}

export interface RecommendationResponse {
  learnerId: number;
  recommendations: RecommendationItem[];
}

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  // En prod : passe par l'API Gateway. En dev : appel direct au service Python
  private apiUrl = environment.production
    ? `${environment.apiUrl}/recommendations`
    : 'http://localhost:8095/api/recommendations';

  constructor(private http: HttpClient) {}

  getRecommendations(learnerId: number, topN: number = 5): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(
      `${this.apiUrl}/learner/${learnerId}?topN=${topN}`
    );
  }
}
