import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ExamService } from './exam.service';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour ExamService.
 * Vérifie les appels HTTP pour les examens, questions, résultats et enrollments.
 */
describe('ExamService', () => {
  let service: ExamService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/exams`;

  const mockExam = {
    id: 1,
    courseId: 5,
    examType: 'QUIZ',
    title: 'Quiz Spring Boot',
    description: 'Quiz sur les bases',
    duration: 60,
    passingScore: 70,
    totalMarks: 100,
    isActive: true,
    questions: []
  };

  const mockResult = {
    id: 1,
    examId: 1,
    examTitle: 'Quiz Spring Boot',
    userId: 5,
    obtainedMarks: 80,
    totalMarks: 100,
    percentage: 80.0,
    passed: true,
    submittedAt: '2026-04-16T10:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ExamService]
    });
    service = TestBed.inject(ExamService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAllExams ──────────────────────────────────────────────────────────
  describe('getAllExams()', () => {
    it('should return all exams via GET', () => {
      service.getAllExams().subscribe(exams => {
        expect(exams.length).toBe(1);
        expect(exams[0].title).toBe('Quiz Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockExam]);
    });

    it('should return empty array when no exams exist', () => {
      service.getAllExams().subscribe(exams => {
        expect(exams).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });

  // ─── getExamById ──────────────────────────────────────────────────────────
  describe('getExamById()', () => {
    it('should return an exam by ID via GET', () => {
      service.getExamById(1).subscribe(exam => {
        expect(exam.id).toBe(1);
        expect(exam.examType).toBe('QUIZ');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockExam);
    });
  });

  // ─── getExamsByCourse ─────────────────────────────────────────────────────
  describe('getExamsByCourse()', () => {
    it('should return exams for a specific course via GET', () => {
      service.getExamsByCourse(5).subscribe(exams => {
        expect(exams.length).toBe(1);
        expect(exams[0].courseId).toBe(5);
      });

      const req = httpMock.expectOne(`${apiUrl}/course/5`);
      expect(req.request.method).toBe('GET');
      req.flush([mockExam]);
    });
  });

  // ─── createExam ───────────────────────────────────────────────────────────
  describe('createExam()', () => {
    it('should create an exam via POST', () => {
      service.createExam(mockExam as any).subscribe(exam => {
        expect(exam.id).toBe(1);
        expect(exam.title).toBe('Quiz Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      req.flush(mockExam);
    });
  });

  // ─── updateExam ───────────────────────────────────────────────────────────
  describe('updateExam()', () => {
    it('should update an exam via PUT', () => {
      const updated = { ...mockExam, title: 'Quiz Mis à jour' };

      service.updateExam(1, updated as any).subscribe(exam => {
        expect(exam.title).toBe('Quiz Mis à jour');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(updated);
    });
  });

  // ─── deleteExam ───────────────────────────────────────────────────────────
  describe('deleteExam()', () => {
    it('should delete an exam via DELETE', () => {
      service.deleteExam(1).subscribe(() => {
        // DELETE réussi — pas de valeur de retour attendue
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── submitExam ───────────────────────────────────────────────────────────
  describe('submitExam()', () => {
    it('should submit exam answers via POST and return result', () => {
      const submission = {
        examId: 1,
        userId: 5,
        answers: [{ questionId: 1, selectedAnswer: 'A' }]
      };

      service.submitExam(submission).subscribe(result => {
        expect(result.passed).toBeTrue();
        expect(result.percentage).toBe(80.0);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/exam-results/submit`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(submission);
      req.flush(mockResult);
    });
  });

  // ─── getMyExams ───────────────────────────────────────────────────────────
  describe('getMyExams()', () => {
    it('should return exams for a learner via GET with userId param', () => {
      service.getMyExams(5).subscribe(exams => {
        expect(exams.length).toBe(1);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${environment.apiUrl}/exam-enrollments/my-exams` &&
        r.params.get('userId') === '5'
      );
      expect(req.request.method).toBe('GET');
      req.flush([mockExam]);
    });
  });

  // ─── getExamResult ────────────────────────────────────────────────────────
  describe('getExamResult()', () => {
    it('should return exam result by ID via GET', () => {
      service.getExamResult(1).subscribe(result => {
        expect(result.id).toBe(1);
        expect(result.passed).toBeTrue();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/exam-results/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  // ─── getDraft / saveDraft ─────────────────────────────────────────────────
  describe('getDraft()', () => {
    it('should retrieve a draft via GET', () => {
      const mockDraft = { examId: 1, userId: 5, answers: { '1': 'A' } };

      service.getDraft(1, 5).subscribe(draft => {
        expect(draft.answers['1']).toBe('A');
      });

      const req = httpMock.expectOne(`${apiUrl}/1/draft?userId=5`);
      expect(req.request.method).toBe('GET');
      req.flush(mockDraft);
    });
  });

  describe('saveDraft()', () => {
    it('should save a draft via POST', () => {
      const answers = new Map<number, string>([[1, 'A'], [2, 'B']]);

      service.saveDraft(1, 5, answers).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/1/draft`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.userId).toBe(5);
      expect(req.request.body.answers['1']).toBe('A');
      req.flush({});
    });
  });

  // ─── startExam / pauseExam / resumeExam ───────────────────────────────────
  describe('startExam()', () => {
    it('should start an exam via POST', () => {
      service.startExam(1, 5).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/exam-enrollments/1/start?userId=5`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('pauseExam()', () => {
    it('should pause an exam via POST', () => {
      service.pauseExam(1, 5).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/exam-enrollments/1/pause?userId=5`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('getTimeRemaining()', () => {
    it('should get remaining time via GET', () => {
      service.getTimeRemaining(1, 5).subscribe(response => {
        expect(response.timeRemaining).toBe(3600);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/exam-enrollments/1/time-remaining?userId=5`);
      expect(req.request.method).toBe('GET');
      req.flush({ timeRemaining: 3600 });
    });
  });
});
