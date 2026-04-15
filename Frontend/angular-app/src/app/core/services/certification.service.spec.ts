import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CertificationService, PageResponse } from './certification.service';
import {
  CertificationTemplate,
  EarnedCertification,
  AwardCertificationRequest,
  BulkAwardCertificationRequest
} from '../models/certification.model';

describe('CertificationService', () => {
  let service: CertificationService;
  let httpMock: HttpTestingController;

  const BASE = 'http://localhost:8083/api/certifications-badges/certification-templates';
  const EARNED = 'http://localhost:8083/api/certifications-badges/earned-certifications';

  const mockTemplate: CertificationTemplate = {
    id: 1,
    title: 'Spring Boot Certification',
    description: 'For Spring Boot developers'
  };

  const mockEarnedCert: EarnedCertification = {
    id: 10,
    certificationTemplate: mockTemplate,
    learnerId: 42,
    issueDate: '2026-01-15',
    expiryDate: '2028-01-15',
    awardedBy: 0,
    verificationCode: 'cert-xyz-789'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CertificationService]
    });
    service = TestBed.inject(CertificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── getAllTemplates ────────────────────────────────────────────────────────

  it('should fetch all certification templates with GET', () => {
    service.getAllTemplates().subscribe(result => {
      expect(result.length).toBe(1);
      expect(result[0].title).toBe('Spring Boot Certification');
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockTemplate]);
  });

  it('should return empty array when no certification templates exist', () => {
    service.getAllTemplates().subscribe(result => {
      expect(result).toEqual([]);
    });

    httpMock.expectOne(BASE).flush([]);
  });

  it('should propagate 500 error on getAllTemplates', () => {
    service.getAllTemplates().subscribe({
      error: err => expect(err.status).toBe(500)
    });

    httpMock.expectOne(BASE).flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });

  // ─── getTemplatesPaginated ─────────────────────────────────────────────────

  it('should fetch paginated certification templates with correct query params', () => {
    const mockPage: PageResponse<CertificationTemplate> = {
      content: [mockTemplate],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getTemplatesPaginated(0, 10, 'id', 'DESC').subscribe(result => {
      expect(result.content.length).toBe(1);
      expect(result.totalPages).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url === `${BASE}/paginated`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sortBy')).toBe('id');
    expect(req.request.params.get('sortDirection')).toBe('DESC');
    req.flush(mockPage);
  });

  // ─── getTemplateById ───────────────────────────────────────────────────────

  it('should fetch a certification template by id with GET', () => {
    service.getTemplateById(1).subscribe(result => {
      expect(result.id).toBe(1);
      expect(result.title).toBe('Spring Boot Certification');
    });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTemplate);
  });

  it('should propagate 404 error when certification template not found', () => {
    service.getTemplateById(999).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${BASE}/999`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── createTemplate ────────────────────────────────────────────────────────

  it('should create a certification template with POST and return created template', () => {
    const newTemplate: CertificationTemplate = { title: 'Docker Certification', description: 'For Docker experts' };

    service.createTemplate(newTemplate).subscribe(result => {
      expect(result.id).toBe(2);
      expect(result.title).toBe('Docker Certification');
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newTemplate);
    req.flush({ ...newTemplate, id: 2 });
  });

  it('should propagate 400 error when creating certification template with empty title', () => {
    const invalid: CertificationTemplate = { title: '', description: '' };

    service.createTemplate(invalid).subscribe({
      error: err => expect(err.status).toBe(400)
    });

    httpMock.expectOne(BASE).flush('Bad request', { status: 400, statusText: 'Bad Request' });
  });

  // ─── updateTemplate ────────────────────────────────────────────────────────

  it('should update a certification template with PUT', () => {
    const updated: CertificationTemplate = { ...mockTemplate, title: 'Advanced Spring Boot' };

    service.updateTemplate(1, updated).subscribe(result => {
      expect(result.title).toBe('Advanced Spring Boot');
    });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  it('should propagate 404 error when updating non-existent certification template', () => {
    service.updateTemplate(999, mockTemplate).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${BASE}/999`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── deleteTemplate ────────────────────────────────────────────────────────

  it('should delete a certification template with DELETE', () => {
    let completed = false;
    service.deleteTemplate(1).subscribe({ complete: () => (completed = true) });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBeTrue();
  });

  it('should propagate 400 error when deleting template with existing earned certifications', () => {
    service.deleteTemplate(1).subscribe({
      error: err => expect(err.status).toBe(400)
    });

    httpMock.expectOne(`${BASE}/1`).flush(
      'Cannot delete: learners have earned this certification',
      { status: 400, statusText: 'Bad Request' }
    );
  });

  // ─── getCertificationsByLearner ────────────────────────────────────────────

  it('should fetch earned certifications for a learner with GET', () => {
    service.getCertificationsByLearner(42).subscribe(result => {
      expect(result.length).toBe(1);
      expect(result[0].learnerId).toBe(42);
    });

    const req = httpMock.expectOne(`${EARNED}/learner/42`);
    expect(req.request.method).toBe('GET');
    req.flush([mockEarnedCert]);
  });

  it('should return empty array when learner has no certifications', () => {
    service.getCertificationsByLearner(99).subscribe(result => {
      expect(result).toEqual([]);
    });

    httpMock.expectOne(`${EARNED}/learner/99`).flush([]);
  });

  it('should propagate 403 error when learner is not authorized', () => {
    service.getCertificationsByLearner(42).subscribe({
      error: err => expect(err.status).toBe(403)
    });

    httpMock.expectOne(`${EARNED}/learner/42`).flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  // ─── getCertificationsByLearnerPaginated ───────────────────────────────────

  it('should fetch paginated certifications with correct query params', () => {
    const mockPage: PageResponse<EarnedCertification> = {
      content: [mockEarnedCert],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getCertificationsByLearnerPaginated(42, 0, 10, 'issueDate', 'DESC').subscribe(result => {
      expect(result.content[0].learnerId).toBe(42);
      expect(result.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url === `${EARNED}/learner/42/paginated`);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('sortBy')).toBe('issueDate');
    expect(req.request.params.get('sortDirection')).toBe('DESC');
    req.flush(mockPage);
  });

  // ─── getEarnedCertificationById ────────────────────────────────────────────

  it('should fetch earned certification details by id from /details endpoint', () => {
    service.getEarnedCertificationById(10).subscribe(result => {
      expect(result.id).toBe(10);
      expect(result.verificationCode).toBe('cert-xyz-789');
    });

    const req = httpMock.expectOne(`${EARNED}/10/details`);
    expect(req.request.method).toBe('GET');
    req.flush(mockEarnedCert);
  });

  it('should propagate 404 error when earned certification not found', () => {
    service.getEarnedCertificationById(999).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${EARNED}/999/details`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── awardCertification ────────────────────────────────────────────────────

  it('should award a certification with POST and return earned certification', () => {
    const request: AwardCertificationRequest = {
      certificationTemplateId: 1,
      learnerId: 42,
      issueDate: '2026-01-15',
      expiryDate: '2028-01-15'
    };

    service.awardCertification(request).subscribe(result => {
      expect(result.id).toBe(10);
      expect(result.learnerId).toBe(42);
    });

    const req = httpMock.expectOne(EARNED);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockEarnedCert);
  });

  it('should propagate 400 error when expiry date is before issue date', () => {
    const request: AwardCertificationRequest = {
      certificationTemplateId: 1,
      learnerId: 42,
      issueDate: '2026-01-15',
      expiryDate: '2025-01-01'
    };

    service.awardCertification(request).subscribe({
      error: err => expect(err.status).toBe(400)
    });

    httpMock.expectOne(EARNED).flush('Expiry date cannot be before issue date', { status: 400, statusText: 'Bad Request' });
  });

  it('should propagate 409 error when certification already awarded (duplicate)', () => {
    const request: AwardCertificationRequest = {
      certificationTemplateId: 1,
      learnerId: 42,
      issueDate: '2026-01-15'
    };

    service.awardCertification(request).subscribe({
      error: err => expect(err.status).toBe(409)
    });

    httpMock.expectOne(EARNED).flush('Already awarded', { status: 409, statusText: 'Conflict' });
  });

  // ─── bulkAwardCertification ────────────────────────────────────────────────

  it('should bulk award certifications with POST to /award/bulk', () => {
    const request: BulkAwardCertificationRequest = {
      certificationTemplateId: 1,
      learnerIds: [42, 43, 44]
    };
    const mockResponse = { successCount: 3, failureCount: 0, results: [] };

    service.bulkAwardCertification(request).subscribe(result => {
      expect(result.successCount).toBe(3);
      expect(result.failureCount).toBe(0);
    });

    const req = httpMock.expectOne(`${EARNED}/award/bulk`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.learnerIds.length).toBe(3);
    req.flush(mockResponse);
  });

  it('should propagate 404 error when bulk awarding with non-existent template', () => {
    const request: BulkAwardCertificationRequest = { certificationTemplateId: 999, learnerIds: [42] };

    service.bulkAwardCertification(request).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${EARNED}/award/bulk`).flush('Template not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── revokeCertification ───────────────────────────────────────────────────

  it('should revoke a certification with DELETE', () => {
    let completed = false;
    service.revokeCertification(10).subscribe({ complete: () => (completed = true) });

    const req = httpMock.expectOne(`${EARNED}/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBeTrue();
  });
});
