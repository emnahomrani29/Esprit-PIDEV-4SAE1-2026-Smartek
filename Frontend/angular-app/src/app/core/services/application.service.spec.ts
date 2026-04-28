/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationService } from './application.service';
import { Application, ApplicationRequest } from '../models/application.model';
import { environment } from '../../../environments/environment';

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/applications`;

  const mockApp: Application = {
    id: 1, offerId: 100, learnerId: 2,
    learnerName: 'Alice', learnerEmail: 'alice@test.com',
    status: 'PENDING', appliedAt: '2026-01-01T10:00:00'
  };

  const mockAcceptedApp: Application = { ...mockApp, id: 2, status: 'ACCEPTED' };
  const mockRejectedApp: Application = { ...mockApp, id: 3, status: 'REJECTED' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApplicationService]
    });
    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── applyToOffer ──────────────────────────────────────────────────────────

  describe('applyToOffer()', () => {
    it('should POST to /applications and return the created application', () => {
      const req: ApplicationRequest = {
        offerId: 100, learnerId: 2,
        learnerName: 'Alice', learnerEmail: 'alice@test.com',
        coverLetter: 'Je suis motivée pour ce poste.'
      };

      service.applyToOffer(req).subscribe(app => {
        expect(app.id).toBe(1);
        expect(app.status).toBe('PENDING');
        expect(app.learnerId).toBe(2);
      });

      const httpReq = httpMock.expectOne(apiUrl);
      expect(httpReq.request.method).toBe('POST');
      expect(httpReq.request.body).toEqual(req);
      httpReq.flush(mockApp);
    });

    it('should send the full request body including coverLetter and cvBase64', () => {
      const req: ApplicationRequest = {
        offerId: 100, learnerId: 2,
        learnerName: 'Alice', learnerEmail: 'alice@test.com',
        coverLetter: 'Ma lettre de motivation.',
        cvBase64: 'base64encodedcv==',
        cvFileName: 'cv_alice.pdf'
      };

      service.applyToOffer(req).subscribe();

      const httpReq = httpMock.expectOne(apiUrl);
      expect(httpReq.request.body.cvBase64).toBe('base64encodedcv==');
      expect(httpReq.request.body.cvFileName).toBe('cv_alice.pdf');
      httpReq.flush(mockApp);
    });

    it('should propagate 422 error when already applied (duplicate)', () => {
      const req: ApplicationRequest = {
        offerId: 100, learnerId: 2,
        learnerName: 'Alice', learnerEmail: 'alice@test.com'
      };

      let error: HttpErrorResponse | undefined;
      service.applyToOffer(req).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(apiUrl).flush(
        { message: 'Vous avez déjà postulé à cette offre' },
        { status: 422, statusText: 'Unprocessable Entity' }
      );

      expect(error).toBeDefined();
      expect(error!.status).toBe(422);
    });

    it('should propagate 404 error when offer not found', () => {
      const req: ApplicationRequest = {
        offerId: 99999, learnerId: 2,
        learnerName: 'Alice', learnerEmail: 'alice@test.com'
      };

      let error: HttpErrorResponse | undefined;
      service.applyToOffer(req).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(apiUrl).flush(
        { message: 'Offer not found' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(error!.status).toBe(404);
    });
  });

  // ─── getApplicationsByOffer ────────────────────────────────────────────────

  describe('getApplicationsByOffer()', () => {
    it('should GET /applications/offer/{offerId}', () => {
      service.getApplicationsByOffer(100).subscribe(apps => {
        expect(apps.length).toBe(1);
        expect(apps[0].offerId).toBe(100);
      });

      const req = httpMock.expectOne(`${apiUrl}/offer/100`);
      expect(req.request.method).toBe('GET');
      req.flush([mockApp]);
    });

    it('should return empty array when no applications exist', () => {
      service.getApplicationsByOffer(999).subscribe(apps => {
        expect(apps).toEqual([]);
      });
      httpMock.expectOne(`${apiUrl}/offer/999`).flush([]);
    });

    it('should return multiple applications for an offer', () => {
      const apps = [mockApp, mockAcceptedApp, mockRejectedApp];
      service.getApplicationsByOffer(100).subscribe(result => {
        expect(result.length).toBe(3);
        expect(result.map(a => a.status)).toContain('PENDING');
        expect(result.map(a => a.status)).toContain('ACCEPTED');
        expect(result.map(a => a.status)).toContain('REJECTED');
      });
      httpMock.expectOne(`${apiUrl}/offer/100`).flush(apps);
    });
  });

  // ─── getApplicationsByLearner ──────────────────────────────────────────────

  describe('getApplicationsByLearner()', () => {
    it('should GET /applications/learner/{learnerId}', () => {
      service.getApplicationsByLearner(2).subscribe(apps => {
        expect(apps[0].learnerId).toBe(2);
      });

      const req = httpMock.expectOne(`${apiUrl}/learner/2`);
      expect(req.request.method).toBe('GET');
      req.flush([mockApp]);
    });

    it('should return empty array when learner has no applications', () => {
      service.getApplicationsByLearner(999).subscribe(apps => {
        expect(apps).toEqual([]);
      });
      httpMock.expectOne(`${apiUrl}/learner/999`).flush([]);
    });

    it('should return all applications for a learner across multiple offers', () => {
      const app2: Application = { ...mockApp, id: 2, offerId: 200 };
      service.getApplicationsByLearner(2).subscribe(apps => {
        expect(apps.length).toBe(2);
        const offerIds = apps.map(a => a.offerId);
        expect(offerIds).toContain(100);
        expect(offerIds).toContain(200);
      });
      httpMock.expectOne(`${apiUrl}/learner/2`).flush([mockApp, app2]);
    });
  });

  // ─── hasApplied ────────────────────────────────────────────────────────────

  describe('hasApplied()', () => {
    it('should return true when learner has applied', () => {
      service.hasApplied(100, 2).subscribe(result => expect(result).toBeTrue());
      httpMock.expectOne(`${apiUrl}/check/100/2`).flush(true);
    });

    it('should return false when learner has not applied', () => {
      service.hasApplied(100, 99).subscribe(result => expect(result).toBeFalse());
      httpMock.expectOne(`${apiUrl}/check/100/99`).flush(false);
    });

    it('should call the correct URL with offerId and learnerId', () => {
      service.hasApplied(42, 7).subscribe();
      const req = httpMock.expectOne(`${apiUrl}/check/42/7`);
      expect(req.request.method).toBe('GET');
      req.flush(false);
    });
  });

  // ─── updateApplicationStatus ───────────────────────────────────────────────

  describe('updateApplicationStatus()', () => {
    it('should PUT to /applications/{id}/status?status=ACCEPTED', () => {
      service.updateApplicationStatus(1, 'ACCEPTED').subscribe(app => {
        expect(app.id).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1/status?status=ACCEPTED`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(mockAcceptedApp);
    });

    it('should PUT with status REJECTED', () => {
      service.updateApplicationStatus(3, 'REJECTED').subscribe(app => {
        expect(app.status).toBe('REJECTED');
      });

      const req = httpMock.expectOne(`${apiUrl}/3/status?status=REJECTED`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockRejectedApp);
    });

    it('should PUT with status PENDING (reset)', () => {
      service.updateApplicationStatus(1, 'PENDING').subscribe(app => {
        expect(app.status).toBe('PENDING');
      });

      const req = httpMock.expectOne(`${apiUrl}/1/status?status=PENDING`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockApp);
    });

    it('should propagate 404 when application not found', () => {
      let error: HttpErrorResponse | undefined;
      service.updateApplicationStatus(99999, 'ACCEPTED').subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/99999/status?status=ACCEPTED`).flush(
        { message: 'Application not found' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(error!.status).toBe(404);
    });

    it('should propagate 403 when unauthorized', () => {
      let error: HttpErrorResponse | undefined;
      service.updateApplicationStatus(1, 'ACCEPTED').subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/1/status?status=ACCEPTED`).flush(
        { message: 'Forbidden' },
        { status: 403, statusText: 'Forbidden' }
      );

      expect(error!.status).toBe(403);
    });
  });
});
