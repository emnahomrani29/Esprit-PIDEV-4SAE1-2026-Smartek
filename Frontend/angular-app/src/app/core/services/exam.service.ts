import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exam, Question, Exercise, ExamResult, ExerciseAnswer } from '../models/exam.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExamService {
  private apiUrl = `${environment.apiUrl}/exams`;

  constructor(private http: HttpClient) {}

  // Exam CRUD
  getAllExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(this.apiUrl);
  }

  getExamById(id: number): Observable<Exam> {
    return this.http.get<Exam>(`${this.apiUrl}/${id}`);
  }

  getExamsByCourse(courseId: number): Observable<Exam[]> {
    return this.http.get<Exam[]>(`${this.apiUrl}/course/${courseId}`);
  }

  createExam(exam: Exam): Observable<Exam> {
    return this.http.post<Exam>(this.apiUrl, exam);
  }

  updateExam(id: number, exam: Exam): Observable<Exam> {
    return this.http.put<Exam>(`${this.apiUrl}/${id}`, exam);
  }

  deleteExam(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Questions (for QUIZ)
  getQuestionsByExam(examId: number): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/${examId}/questions`);
  }

  createQuestion(examId: number, question: Question): Observable<Question> {
    return this.http.post<Question>(`${this.apiUrl}/${examId}/questions`, question);
  }

  updateQuestion(examId: number, questionId: number, question: Question): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/${examId}/questions/${questionId}`, question);
  }

  deleteQuestion(examId: number, questionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${examId}/questions/${questionId}`);
  }

  // Exercises (for EXAM)
  getExercisesByExam(examId: number): Observable<Exercise[]> {
    return this.http.get<Exercise[]>(`${this.apiUrl}/${examId}/exercises`);
  }

  createExercise(examId: number, exercise: Exercise): Observable<Exercise> {
    return this.http.post<Exercise>(`${this.apiUrl}/${examId}/exercises`, exercise);
  }

  updateExercise(examId: number, exerciseId: number, exercise: Exercise): Observable<Exercise> {
    return this.http.put<Exercise>(`${this.apiUrl}/${examId}/exercises/${exerciseId}`, exercise);
  }

  deleteExercise(examId: number, exerciseId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${examId}/exercises/${exerciseId}`);
  }

  // Exam Results
  submitExam(submissionData: any): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${environment.apiUrl}/exam-results/submit`, submissionData);
  }

  submitQuiz(examId: number, answers: any): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${this.apiUrl}/${examId}/submit-quiz`, answers);
  }

  submitExamOld(examId: number, answers: ExerciseAnswer[]): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${this.apiUrl}/${examId}/submit-exam`, answers);
  }

  getResultsByUser(userId: number): Observable<ExamResult[]> {
    return this.http.get<ExamResult[]>(`${this.apiUrl}/results/user/${userId}`);
  }

  getResultById(resultId: number): Observable<ExamResult> {
    return this.http.get<ExamResult>(`${this.apiUrl}/results/${resultId}`);
  }

  // Correction (for TRAINER)
  getPendingCorrections(): Observable<ExamResult[]> {
    return this.http.get<ExamResult[]>(`${this.apiUrl}/results/pending`);
  }

  correctExercise(answerId: number, marks: number, feedback: string): Observable<ExerciseAnswer> {
    return this.http.put<ExerciseAnswer>(`${this.apiUrl}/exercise-answers/${answerId}/correct`, {
      marksObtained: marks,
      trainerFeedback: feedback
    });
  }

  finalizeCorrection(resultId: number): Observable<ExamResult> {
    return this.http.put<ExamResult>(`${this.apiUrl}/results/${resultId}/finalize`, {});
  }

  // Exam Enrollments
  getMyExams(userId: number): Observable<Exam[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<Exam[]>(`${environment.apiUrl}/exam-enrollments/my-exams`, { params });
  }

  completeCourse(courseId: number, userId: number): Observable<string> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.post<string>(`${environment.apiUrl}/courses/${courseId}/complete`, null, { params });
  }

  getExamResult(resultId: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/exam-results/${resultId}`);
  }

  getUserAnswers(resultId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/exam-results/${resultId}/answers`);
  }

  getDraft(examId: number, userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${examId}/draft?userId=${userId}`);
  }

  saveDraft(examId: number, userId: number, answers: Map<number, string>): Observable<any> {
    const answersObj: any = {};
    answers.forEach((value, key) => {
      answersObj[key] = value;
    });
    return this.http.post(`${this.apiUrl}/${examId}/draft`, {
      userId,
      answers: answersObj
    });
  }

  // Démarrer un examen
  startExam(examId: number, userId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/exam-enrollments/${examId}/start?userId=${userId}`, {});
  }

  // Repasser un examen
  retakeExam(examId: number, userId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/exam-enrollments/${examId}/retake?userId=${userId}`, {});
  }

  // Obtenir le temps restant
  getTimeRemaining(examId: number, userId: number): Observable<{ timeRemaining: number }> {
    return this.http.get<{ timeRemaining: number }>(`${environment.apiUrl}/exam-enrollments/${examId}/time-remaining?userId=${userId}`);
  }

  // Mettre en pause un examen
  pauseExam(examId: number, userId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/exam-enrollments/${examId}/pause?userId=${userId}`, {});
  }

  // Reprendre un examen
  resumeExam(examId: number, userId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/exam-enrollments/${examId}/resume?userId=${userId}`, {});
  }
}
