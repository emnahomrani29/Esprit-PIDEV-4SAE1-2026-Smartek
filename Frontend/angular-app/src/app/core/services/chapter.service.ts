import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Chapter, ChapterCreateRequest, ChapterUpdateRequest } from '../models/chapter.model';

@Injectable({
  providedIn: 'root'
})
export class ChapterService {
  private apiUrl = `${environment.apiUrl}/courses`;

  constructor(private http: HttpClient) {}

  getChaptersByCourse(courseId: number): Observable<Chapter[]> {
    return this.http.get<Chapter[]>(`${this.apiUrl}/${courseId}/chapters`);
  }

  getChapterById(courseId: number, chapterId: number): Observable<Chapter> {
    return this.http.get<Chapter>(`${this.apiUrl}/${courseId}/chapters/${chapterId}`);
  }

  createChapter(courseId: number, chapter: ChapterCreateRequest): Observable<Chapter> {
    return this.http.post<Chapter>(`${this.apiUrl}/${courseId}/chapters`, chapter);
  }

  updateChapter(courseId: number, chapterId: number, chapter: ChapterUpdateRequest): Observable<Chapter> {
    return this.http.put<Chapter>(`${this.apiUrl}/${courseId}/chapters/${chapterId}`, chapter);
  }

  uploadPdf(courseId: number, chapterId: number, file: File): Observable<Chapter> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Chapter>(`${this.apiUrl}/${courseId}/chapters/${chapterId}/upload-pdf`, formData);
  }

  deleteChapter(courseId: number, chapterId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${courseId}/chapters/${chapterId}`);
  }
}
