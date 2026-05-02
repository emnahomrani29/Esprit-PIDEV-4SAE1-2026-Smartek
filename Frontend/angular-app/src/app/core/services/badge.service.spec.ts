import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BadgeService, PageResponse } from './badge.service';
import { BadgeTemplate, EarnedBadge, AwardBadgeRequest, BulkAwardBadgeRequest } from '../models/badge.model';

describe('BadgeService', () => {
  let service: BadgeService;
  let httpMock: HttpTestingController;

  const BASE = 'http://localhost:8083/api/certifications-badges/badge-templates';
  const EARNED = 'http://localhost:8083/api/certifications-badges/earned-badges';

  const mockTemplate: BadgeTemplate = {
    id: 1,
    name: 'Gold Badge',
    description: 'Awarded for 90%+ score',
    examId: 10,
    minimumScore: 90
  };

  const mockEarnedBadge: EarnedBadge = {
    id: 5,
    badgeTemplate: mockTemplate,
    learnerId: 42,
    awardDate: new Date('2026-01-15'),
    awardedBy: 0,
    verificationId: 'abc-123'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BadgeService]
    });
    service = TestBed.inject(BadgeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── getAllTemplates ────────────────────────────────────────────────────────

  it('should fetch all badge templates with GET', () => {
    service.getAllTemplates().subscribe(result => {
      expect(result.length).toBe(1);
      expect(result[0].name).toBe('Gold Badge');
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockTemplate]);
  });

  it('should return empty array when no badge templates exist', () => {
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

  it('should fetch paginated badge templates with correct query params', () => {
    const mockPage: PageResponse<BadgeTemplate> = {
      content: [mockTemplate],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getTemplatesPaginated(0, 10, 'id', 'DESC').subscribe(result => {
      expect(result.content.length).toBe(1);
      expect(result.totalElements).toBe(1);
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

  it('should fetch a badge template by id with GET', () => {
    service.getTemplateById(1).subscribe(result => {
      expect(result.id).toBe(1);
      expect(result.name).toBe('Gold Badge');
    });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTemplate);
  });

  it('should propagate 404 error when badge template not found', () => {
    service.getTemplateById(999).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${BASE}/999`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── createTemplate ────────────────────────────────────────────────────────

  it('should create a badge template with POST and return created template', () => {
    const newTemplate: BadgeTemplate = { name: 'Silver Badge', description: 'For 75%+', minimumScore: 75 };

    service.createTemplate(newTemplate).subscribe(result => {
      expect(result.id).toBe(2);
      expect(result.name).toBe('Silver Badge');
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newTemplate);
    req.flush({ ...newTemplate, id: 2 });
  });

  it('should propagate 400 error when creating invalid badge template', () => {
    const invalid: BadgeTemplate = { name: '', description: '' };

    service.createTemplate(invalid).subscribe({
      error: err => expect(err.status).toBe(400)
    });

    httpMock.expectOne(BASE).flush('Bad request', { status: 400, statusText: 'Bad Request' });
  });

  // ─── updateTemplate ────────────────────────────────────────────────────────

  it('should update a badge template with PUT', () => {
    const updated: BadgeTemplate = { ...mockTemplate, name: 'Platinum Badge' };

    service.updateTemplate(1, updated).subscribe(result => {
      expect(result.name).toBe('Platinum Badge');
    });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  it('should propagate 404 error when updating non-existent badge template', () => {
    service.updateTemplate(999, mockTemplate).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${BASE}/999`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── deleteTemplate ────────────────────────────────────────────────────────

  it('should delete a badge template with DELETE', () => {
    let completed = false;
    service.deleteTemplate(1).subscribe({ complete: () => (completed = true) });

    const req = httpMock.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBeTrue();
  });

  it('should propagate 404 error when deleting non-existent badge template', () => {
    service.deleteTemplate(999).subscribe({
      error: err => expect(err.status).toBe(404)
    });

    httpMock.expectOne(`${BASE}/999`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─── getBadgesByLearner ────────────────────────────────────────────────────

  it('should fetch earned badges for a learner with GET', () => {
    service.getBadgesByLearner(42).subscribe(result => {
      expect(result.length).toBe(1);
      expect(result[0].learnerId).toBe(42);
    });

    const req = httpMock.expectOne(`${EARNED}/learner/42`);
    expect(req.request.method).toBe('GET');
    req.flush([mockEarnedBadge]);
  });

  it('should return empty array when learner has no badges', () => {
    service.getBadgesByLearner(99).subscribe(result => {
      expect(result).toEqual([]);
    });

    httpMock.expectOne(`${EARNED}/learner/99`).flush([]);
  });

  it('should propagate 403 error when learner is not authorized', () => {
    service.getBadgesByLearner(42).subscribe({
      error: err => expect(err.status).toBe(403)
    });

    httpMock.expectOne(`${EARNED}/learner/42`).flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  // ─── getBadgesByLearnerPaginated ───────────────────────────────────────────

  it('should fetch paginated earned badges with correct query params', () => {
    const mockPage: PageResponse<EarnedBadge> = {
      content: [mockEarnedBadge],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getBadgesByLearnerPaginated(42, 0, 10, 'awardDate', 'DESC').subscribe(result => {
      expect(result.content[0].learnerId).toBe(42);
    });

    const req = httpMock.expectOne(r => r.url === `${EARNED}/learner/42/paginated`);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('sortBy')).toBe('awardDate');
    req.flush(mockPage);
  });

  // ─── awardBadge ────────────────────────────────────────────────────────────

  it('should award a badge with POST and return earned badge', () => {
    const request: AwardBadgeRequest = { badgeTemplateId: 1, learnerId: 42 };

    service.awardBadge(request).subscribe(result => {
      expect(result.id).toBe(5);
      expect(result.learnerId).toBe(42);
    });

    const req = httpMock.expectOne(EARNED);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockEarnedBadge);
  });

  it('should propagate 409 error when badge already awarded (duplicate)', () => {
    const request: AwardBadgeRequest = { badgeTemplateId: 1, learnerId: 42 };

    service.awardBadge(request).subscribe({
      error: err => expect(err.status).toBe(409)
    });

    httpMock.expectOne(EARNED).flush('Conflict', { status: 409, statusText: 'Conflict' });
  });

  // ─── bulkAwardBadge ────────────────────────────────────────────────────────

  it('should bulk award badges with POST to /award/bulk', () => {
    const request: BulkAwardBadgeRequest = { badgeTemplateId: 1, learnerIds: [42, 43, 44] };
    const mockResponse = { successCount: 3, failureCount: 0, results: [] };

    service.bulkAwardBadge(request).subscribe(result => {
      expect(result.successCount).toBe(3);
    });

    const req = httpMock.expectOne(`${EARNED}/award/bulk`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.learnerIds.length).toBe(3);
    req.flush(mockResponse);
  });

  // ─── revokeBadge ───────────────────────────────────────────────────────────

  it('should revoke a badge with DELETE', () => {
    let completed = false;
    service.revokeBadge(5).subscribe({ complete: () => (completed = true) });

    const req = httpMock.expectOne(`${EARNED}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBeTrue();
  });
});
