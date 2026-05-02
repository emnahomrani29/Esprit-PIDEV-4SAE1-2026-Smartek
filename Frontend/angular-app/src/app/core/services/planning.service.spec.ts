import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PlanningService, PlanningRequest } from './planning.service';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour PlanningService.
 * Vérifie les appels HTTP pour la gestion des plannings.
 */
describe('PlanningService', () => {
  let service: PlanningService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/plannings`;

  const mockPlanning = {
    planningId: 1,
    date: '2026-05-20',
    startTime: '09:00:00',
    endTime: '11:00:00',
    title: 'Formation Spring Boot',
    description: 'Session de formation',
    eventType: 'TRAINING',
    location: 'Salle A',
    color: '#3498db',
    status: 'DRAFT'
  };

  const mockRequest: PlanningRequest = {
    date: '2026-05-20',
    startTime: '09:00:00',
    endTime: '11:00:00',
    title: 'Formation Spring Boot',
    description: 'Session de formation',
    eventType: 'TRAINING',
    location: 'Salle A',
    color: '#3498db'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PlanningService]
    });
    service = TestBed.inject(PlanningService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAll ───────────────────────────────────────────────────────────────
  describe('getAll()', () => {
    it('should return all plannings via GET', () => {
      service.getAll().subscribe(plannings => {
        expect(plannings.length).toBe(1);
        expect(plannings[0].title).toBe('Formation Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockPlanning]);
    });

    it('should return empty array when no plannings exist', () => {
      service.getAll().subscribe(plannings => {
        expect(plannings).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });

  // ─── getById ──────────────────────────────────────────────────────────────
  describe('getById()', () => {
    it('should return a planning by ID via GET', () => {
      service.getById(1).subscribe(planning => {
        expect(planning.planningId).toBe(1);
        expect(planning.status).toBe('DRAFT');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPlanning);
    });
  });

  // ─── getByDateRange ───────────────────────────────────────────────────────
  describe('getByDateRange()', () => {
    it('should return plannings in a date range via GET', () => {
      service.getByDateRange('2026-05-01', '2026-05-31').subscribe(plannings => {
        expect(plannings.length).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/range?startDate=2026-05-01&endDate=2026-05-31`);
      expect(req.request.method).toBe('GET');
      req.flush([mockPlanning]);
    });
  });

  // ─── create ───────────────────────────────────────────────────────────────
  describe('create()', () => {
    it('should create a planning via POST and return it with DRAFT status', () => {
      service.create(mockRequest).subscribe(planning => {
        expect(planning.planningId).toBe(1);
        expect(planning.status).toBe('DRAFT');
        expect(planning.title).toBe('Formation Spring Boot');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      req.flush(mockPlanning);
    });
  });

  // ─── update ───────────────────────────────────────────────────────────────
  describe('update()', () => {
    it('should update a planning via PUT', () => {
      const updatedRequest = { ...mockRequest, title: 'Formation Mise à jour' };

      service.update(1, updatedRequest).subscribe(planning => {
        expect(planning.title).toBe('Formation Mise à jour');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockPlanning, title: 'Formation Mise à jour' });
    });
  });

  // ─── delete ───────────────────────────────────────────────────────────────
  describe('delete()', () => {
    it('should delete a planning via DELETE', () => {
      service.delete(1).subscribe(() => {
        // DELETE réussi — pas de valeur de retour attendue
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── publish ──────────────────────────────────────────────────────────────
  describe('publish()', () => {
    it('should publish a planning via POST and return PUBLISHED status', () => {
      const publishedPlanning = { ...mockPlanning, status: 'PUBLISHED' };

      service.publish(1).subscribe(planning => {
        expect(planning.status).toBe('PUBLISHED');
      });

      const req = httpMock.expectOne(`${apiUrl}/1/publish`);
      expect(req.request.method).toBe('POST');
      req.flush(publishedPlanning);
    });
  });

  // ─── unpublish ────────────────────────────────────────────────────────────
  describe('unpublish()', () => {
    it('should unpublish a planning via POST and return DRAFT status', () => {
      const draftPlanning = { ...mockPlanning, status: 'DRAFT' };

      service.unpublish(1).subscribe(planning => {
        expect(planning.status).toBe('DRAFT');
      });

      const req = httpMock.expectOne(`${apiUrl}/1/unpublish`);
      expect(req.request.method).toBe('POST');
      req.flush(draftPlanning);
    });
  });

  // ─── getPublished ─────────────────────────────────────────────────────────
  describe('getPublished()', () => {
    it('should return only published plannings via GET', () => {
      const publishedPlanning = { ...mockPlanning, status: 'PUBLISHED' };

      service.getPublished().subscribe(plannings => {
        expect(plannings.length).toBe(1);
        expect(plannings[0].status).toBe('PUBLISHED');
      });

      const req = httpMock.expectOne(`${apiUrl}/published`);
      expect(req.request.method).toBe('GET');
      req.flush([publishedPlanning]);
    });
  });

  // ─── publishWeek / unpublishWeek ──────────────────────────────────────────
  describe('publishWeek()', () => {
    it('should publish all sessions of a week via POST', () => {
      service.publishWeek(10, '2026-05-18').subscribe();

      const req = httpMock.expectOne(
        `${apiUrl}/weekly/publish?trainerId=10&weekStartDate=2026-05-18`
      );
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('unpublishWeek()', () => {
    it('should unpublish all sessions of a week via POST', () => {
      service.unpublishWeek(10, '2026-05-18').subscribe();

      const req = httpMock.expectOne(
        `${apiUrl}/weekly/unpublish?trainerId=10&weekStartDate=2026-05-18`
      );
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });
});
