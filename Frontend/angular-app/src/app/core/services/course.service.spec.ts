import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CourseService } from './course.service';
import { DeliveryMode } from '../models/live-session.model';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour CourseService.
 * Vérifie les appels HTTP, les URLs et les méthodes CRUD.
 */
describe('CourseService', () => {
  let service: CourseService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/courses`;

  const mockCourse = {
    courseId: 1,
    title: 'Spring Boot Avancé',
    content: 'Contenu du cours',
    duration: '2025-12-31',
    trainerId: 10,
    deliveryMode: 'PRESENTIEL',
    chapters: []
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CourseService]
    });
    service = TestBed.inject(CourseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Vérifie qu'aucune requête non attendue n'est en attente
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAllCourses ────────────────────────────────────────────────────────
  describe('getAllCourses()', () => {
    it('should return all courses via GET', () => {
      service.getAllCourses().subscribe(courses => {
        expect(courses.length).toBe(1);
        expect(courses[0].title).toBe('Spring Boot Avancé');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockCourse]);
    });

    it('should return empty array when no courses exist', () => {
      service.getAllCourses().subscribe(courses => {
        expect(courses).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });

  // ─── getCourseById ────────────────────────────────────────────────────────
  describe('getCourseById()', () => {
    it('should return a course by ID via GET', () => {
      service.getCourseById(1).subscribe(course => {
        expect(course.courseId).toBe(1);
        expect(course.title).toBe('Spring Boot Avancé');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCourse);
    });
  });

  // ─── getCoursesByTrainer ──────────────────────────────────────────────────
  describe('getCoursesByTrainer()', () => {
    it('should return courses for a specific trainer via GET', () => {
      service.getCoursesByTrainer(10).subscribe(courses => {
        expect(courses.length).toBe(1);
        expect(courses[0].trainerId).toBe(10);
      });

      const req = httpMock.expectOne(`${apiUrl}/trainer/10`);
      expect(req.request.method).toBe('GET');
      req.flush([mockCourse]);
    });

    it('should return empty array when trainer has no courses', () => {
      service.getCoursesByTrainer(99).subscribe(courses => {
        expect(courses).toEqual([]);
      });

      const req = httpMock.expectOne(`${apiUrl}/trainer/99`);
      req.flush([]);
    });
  });

  // ─── createCourse ─────────────────────────────────────────────────────────
  describe('createCourse()', () => {
    it('should create a course via POST and return the created course', () => {
      const createRequest = {
        title: 'Spring Boot Avancé',
        content: 'Contenu',
        duration: '2025-12-31',
        trainerId: 10,
        deliveryMode: DeliveryMode.PRESENTIEL
      };

      service.createCourse(createRequest).subscribe(course => {
        expect(course.courseId).toBe(1);
        expect(course.title).toBe('Spring Boot Avancé');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockCourse);
    });
  });

  // ─── updateCourse ─────────────────────────────────────────────────────────
  describe('updateCourse()', () => {
    it('should update a course via PUT', () => {
      const updateRequest = { title: 'Spring Boot Mis à jour', content: 'Nouveau contenu', duration: '2025-12-31' };

      service.updateCourse(1, updateRequest).subscribe(course => {
        expect(course.courseId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush({ ...mockCourse, ...updateRequest });
    });
  });

  // ─── deleteCourse ─────────────────────────────────────────────────────────
  describe('deleteCourse()', () => {
    it('should delete a course via DELETE', () => {
      service.deleteCourse(1).subscribe(() => {
        // DELETE réussi — pas de valeur de retour attendue
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── completeCourse ───────────────────────────────────────────────────────
  describe('completeCourse()', () => {
    it('should mark a course as complete via POST', () => {
      service.completeCourse(1, 5).subscribe(response => {
        expect(response).toBe('Cours complété avec succès');
      });

      const req = httpMock.expectOne(`${apiUrl}/1/complete?userId=5`);
      expect(req.request.method).toBe('POST');
      req.flush('Cours complété avec succès');
    });
  });

  // ─── uncompleteCourse ─────────────────────────────────────────────────────
  describe('uncompleteCourse()', () => {
    it('should unmark a course completion via DELETE', () => {
      service.uncompleteCourse(1, 5).subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne(`${apiUrl}/1/uncomplete?userId=5`);
      expect(req.request.method).toBe('DELETE');
      req.flush('Complétion supprimée');
    });
  });
});
