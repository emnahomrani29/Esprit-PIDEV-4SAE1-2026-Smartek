import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Course, CourseCreateRequest, CourseUpdateRequest } from '../models/course.model';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = `${environment.apiUrl}/courses`;

  constructor(private http: HttpClient) {}

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/${id}`);
  }

  getCoursesByTrainer(trainerId: number): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.apiUrl}/trainer/${trainerId}`);
  }

  createCourse(course: CourseCreateRequest): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, course);
  }

  updateCourse(id: number, course: CourseUpdateRequest): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/${id}`, course);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  completeCourse(courseId: number, userId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${courseId}/complete?userId=${userId}`, {}, { responseType: 'text' });
  }

  uncompleteCourse(courseId: number, userId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${courseId}/uncomplete?userId=${userId}`, { responseType: 'text' });
  }
}
