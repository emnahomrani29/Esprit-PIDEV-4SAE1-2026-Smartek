/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
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

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [ApplicationService] });
    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should POST application', () => {
    const req = { offerId: 100, learnerId: 2 } as ApplicationRequest;
    service.applyToOffer(req).subscribe(app => expect(app.status).toBe('PENDING'));
    httpMock.expectOne(apiUrl).flush(mockApp);
  });

  it('should GET applications by offer', () => {
    service.getApplicationsByOffer(100).subscribe(apps => expect(apps.length).toBe(1));
    httpMock.expectOne(`${apiUrl}/offer/100`).flush([mockApp]);
  });

  it('should GET applications by learner', () => {
    service.getApplicationsByLearner(2).subscribe(apps => expect(apps[0].learnerId).toBe(2));
    httpMock.expectOne(`${apiUrl}/learner/2`).flush([mockApp]);
  });

  it('should return true when learner has applied', () => {
    service.hasApplied(100, 2).subscribe(result => expect(result).toBeTrue());
    httpMock.expectOne(`${apiUrl}/check/100/2`).flush(true);
  });

  it('should return false when learner has not applied', () => {
    service.hasApplied(100, 99).subscribe(result => expect(result).toBeFalse());
    httpMock.expectOne(`${apiUrl}/check/100/99`).flush(false);
  });

  it('should PUT status update', () => {
    service.updateApplicationStatus(1, 'ACCEPTED').subscribe(app => expect(app.id).toBe(1));
    const req = httpMock.expectOne(`${apiUrl}/1/status?status=ACCEPTED`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockApp);
  });
});
