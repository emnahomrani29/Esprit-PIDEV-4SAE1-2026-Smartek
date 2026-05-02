import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TrainingService } from './training.service';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour TrainingService.
 * Vérifie les appels HTTP : CRUD formations, gestion des cours associés.
 */
describe('TrainingService', () => {
  let service: TrainingService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/trainings`;

  const mockTraining = {
    trainingId: 1,
    title: 'Formation Spring Boot',
    description: 'Microservices avec Spring Boot',
    category: 'Backend',
    level: 'Intermédiaire',
    duration: '2026-09-01',
    courseIds: [1, 2],
    courses: [],
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00'
  };

  const mockCreateRequest = {
    title: 'Formation Spring Boot',
    description: 'Microservices avec Spring Boot',
    category: 'Backend',
    level: 'Intermédiaire',
    duration: '2026-09-01',
    courseIds: [1, 2],
    createdBy: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TrainingService]
    });
    service = TestBed.inject(TrainingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAllTrainings ───────────────────────────────────────────────────────
  describe('getAllTrainings()', () => {
    it('should return all trainings via GET', () => {
      service.getAllTrainings().subscribe(trainings => {
        expect(trainings.length).toBe(1);
        expect(trainings[0].title).toBe('Formation Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockTraining]);
    });

    it('should return empty array when no trainings exist', () => {
      service.getAllTrainings().subscribe(trainings => {
        expect(trainings).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });

  // ─── getTrainingById ──────────────────────────────────────────────────────
  describe('getTrainingById()', () => {
    it('should return a training by ID via GET', () => {
      service.getTrainingById(1).subscribe(training => {
        expect(training.trainingId).toBe(1);
        expect(training.category).toBe('Backend');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockTraining);
    });
  });

  // ─── getTrainingsByCategory ───────────────────────────────────────────────
  describe('getTrainingsByCategory()', () => {
    it('should return trainings filtered by category via GET', () => {
      service.getTrainingsByCategory('Backend').subscribe(trainings => {
        expect(trainings.length).toBe(1);
        expect(trainings[0].category).toBe('Backend');
      });

      const req = httpMock.expectOne(`${apiUrl}/category/Backend`);
      expect(req.request.method).toBe('GET');
      req.flush([mockTraining]);
    });

    it('should return empty array for unknown category', () => {
      service.getTrainingsByCategory('Unknown').subscribe(trainings => {
        expect(trainings).toEqual([]);
      });

      const req = httpMock.expectOne(`${apiUrl}/category/Unknown`);
      req.flush([]);
    });
  });

  // ─── getTrainingsByLevel ──────────────────────────────────────────────────
  describe('getTrainingsByLevel()', () => {
    it('should return trainings filtered by level via GET', () => {
      service.getTrainingsByLevel('Intermédiaire').subscribe(trainings => {
        expect(trainings.length).toBe(1);
        expect(trainings[0].level).toBe('Intermédiaire');
      });

      const req = httpMock.expectOne(`${apiUrl}/level/Intermédiaire`);
      expect(req.request.method).toBe('GET');
      req.flush([mockTraining]);
    });
  });

  // ─── createTraining ───────────────────────────────────────────────────────
  describe('createTraining()', () => {
    it('should create a training via POST', () => {
      service.createTraining(mockCreateRequest as any).subscribe(training => {
        expect(training.trainingId).toBe(1);
        expect(training.title).toBe('Formation Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockCreateRequest);
      req.flush(mockTraining);
    });
  });

  // ─── updateTraining ───────────────────────────────────────────────────────
  describe('updateTraining()', () => {
    it('should update a training via PUT', () => {
      const updateRequest = { title: 'Formation Mise à jour', category: 'DevOps', level: 'Avancé', duration: '2026-12-01' };

      service.updateTraining(1, updateRequest as any).subscribe(training => {
        expect(training.trainingId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(mockTraining);
    });
  });

  // ─── deleteTraining ───────────────────────────────────────────────────────
  describe('deleteTraining()', () => {
    it('should delete a training via DELETE', () => {
      service.deleteTraining(1).subscribe(() => {
        // DELETE réussi
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── addCourseToTraining ──────────────────────────────────────────────────
  describe('addCourseToTraining()', () => {
    it('should add a course to a training via POST', () => {
      service.addCourseToTraining(1, 99).subscribe(training => {
        expect(training.trainingId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1/courses/99`);
      expect(req.request.method).toBe('POST');
      req.flush(mockTraining);
    });
  });

  // ─── removeCourseFromTraining ─────────────────────────────────────────────
  describe('removeCourseFromTraining()', () => {
    it('should remove a course from a training via DELETE', () => {
      service.removeCourseFromTraining(1, 2).subscribe(training => {
        expect(training.trainingId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1/courses/2`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockTraining);
    });
  });
});
