import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LearningPathService } from './learning-path.service';
import { LearningPathRequest, LearningPathResponse, LearningPathStatus } from '../models/learning-path.model';
import { environment } from '../../../environments/environment';

describe('LearningPathService', () => {
  let service: LearningPathService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/learning-paths`;

  const mockPath: LearningPathResponse = {
    pathId: 1, title: 'Spring Boot Mastery', description: 'Learn Spring Boot',
    learnerId: 5, learnerName: 'Bob', status: 'EN_COURS',
    startDate: '2026-01-01', endDate: '2026-06-30', progress: 40
  };

  const mockRequest: LearningPathRequest = {
    title: 'Spring Boot Mastery', description: 'Learn Spring Boot',
    learnerId: 5, learnerName: 'Bob', status: 'EN_COURS',
    startDate: '2026-01-01', endDate: '2026-06-30', progress: 40
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LearningPathService]
    });
    service = TestBed.inject(LearningPathService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ===== CRUD =====

  it('should create learning path via POST', () => {
    service.create(mockRequest).subscribe(res => {
      expect(res.pathId).toBe(1);
      expect(res.status).toBe('EN_COURS');
    });
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.title).toBe('Spring Boot Mastery');
    req.flush(mockPath);
  });

  it('should get all paths via GET', () => {
    service.getAll().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].title).toBe('Spring Boot Mastery');
    });
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([mockPath]);
  });

  it('should get paths by learner via GET', () => {
    service.getByLearner(5).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].learnerId).toBe(5);
    });
    const req = httpMock.expectOne(`${apiUrl}/learner/5`);
    expect(req.request.method).toBe('GET');
    req.flush([mockPath]);
  });

  it('should get path by id via GET', () => {
    service.getById(1).subscribe(res => {
      expect(res.pathId).toBe(1);
      expect(res.progress).toBe(40);
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPath);
  });

  it('should update path via PUT', () => {
    const updated = { ...mockPath, progress: 80 };
    service.update(1, { ...mockRequest, progress: 80 }).subscribe(res => {
      expect(res.progress).toBe(80);
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updated);
  });

  it('should delete path via DELETE', () => {
    service.delete(1).subscribe(res => expect(res).toBeNull());
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ===== LOGIQUE MÉTIER : Filtres par statut =====

  it('should get paths by status EN_COURS', () => {
    service.getByStatus('EN_COURS').subscribe(res => {
      expect(res.every(p => p.status === 'EN_COURS')).toBeTrue();
    });
    const req = httpMock.expectOne(`${apiUrl}/status/EN_COURS`);
    expect(req.request.method).toBe('GET');
    req.flush([mockPath]);
  });

  it('should get paths by status TERMINE', () => {
    const terminated = { ...mockPath, status: 'TERMINE' as LearningPathStatus, progress: 100 };
    service.getByStatus('TERMINE').subscribe(res => {
      expect(res[0].status).toBe('TERMINE');
      expect(res[0].progress).toBe(100);
    });
    const req = httpMock.expectOne(`${apiUrl}/status/TERMINE`);
    req.flush([terminated]);
  });

  it('should return empty array when no paths for learner', () => {
    service.getByLearner(999).subscribe(res => {
      expect(res).toEqual([]);
    });
    const req = httpMock.expectOne(`${apiUrl}/learner/999`);
    req.flush([]);
  });

  // ===== Gestion d'erreurs =====

  it('should handle 404 error on getById', () => {
    let errorReceived = false;
    service.getById(999).subscribe({
      next: () => fail('should have failed'),
      error: (err) => { errorReceived = true; expect(err.status).toBe(404); }
    });
    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
    expect(errorReceived).toBeTrue();
  });

  it('should handle duplicate title error on create', () => {
    let errorReceived = false;
    service.create(mockRequest).subscribe({
      next: () => fail('should have failed'),
      error: (err) => {
        errorReceived = true;
        expect(err.status).toBe(500);
      }
    });
    const req = httpMock.expectOne(apiUrl);
    req.flush({ message: 'Un parcours avec ce titre existe déjà' }, { status: 500, statusText: 'Internal Server Error' });
    expect(errorReceived).toBeTrue();
  });
});
