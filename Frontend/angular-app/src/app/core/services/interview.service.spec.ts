import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { InterviewService } from './interview.service';
import { Interview, InterviewRequest } from '../models/interview.model';
import { environment } from '../../../environments/environment';

describe('InterviewService', () => {
  let service: InterviewService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/interviews`;

  const mockInterview: Interview = {
    id: 1, applicationId: 10, offerId: 5,
    learnerId: 2, learnerName: 'Alice', learnerEmail: 'alice@test.com',
    interviewDate: '2030-06-15T10:00:00', location: 'Paris',
    meetingLink: 'https://meet.google.com/abc', notes: 'Préparer Java',
    status: 'SCHEDULED', createdBy: 3
  };

  const mockRequest: InterviewRequest = {
    applicationId: 10, interviewDate: '2030-06-15T10:00:00',
    location: 'Paris', meetingLink: 'https://meet.google.com/abc',
    notes: 'Préparer Java', createdBy: 3
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [InterviewService]
    });
    service = TestBed.inject(InterviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── createInterview ───────────────────────────────────────────────────────

  describe('createInterview()', () => {
    it('should POST new interview with all fields', () => {
      service.createInterview(mockRequest).subscribe(iv => {
        expect(iv.id).toBe(1);
        expect(iv.status).toBe('SCHEDULED');
        expect(iv.learnerName).toBe('Alice');
      });
      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.applicationId).toBe(10);
      expect(req.request.body.location).toBe('Paris');
      req.flush(mockInterview);
    });
  });

  // ─── getAllInterviews ──────────────────────────────────────────────────────

  describe('getAllInterviews()', () => {
    it('should GET all interviews', () => {
      service.getAllInterviews().subscribe(ivs => {
        expect(ivs.length).toBe(1);
        expect(ivs[0].status).toBe('SCHEDULED');
      });
      httpMock.expectOne(apiUrl).flush([mockInterview]);
    });
  });

  // ─── getInterviewsByOffer ──────────────────────────────────────────────────

  describe('getInterviewsByOffer()', () => {
    it('should GET interviews for a given offer', () => {
      service.getInterviewsByOffer(5).subscribe(ivs => {
        expect(ivs.length).toBe(1);
        expect(ivs[0].offerId).toBe(5);
      });
      httpMock.expectOne(`${apiUrl}/offer/5`).flush([mockInterview]);
    });
  });

  // ─── getInterviewsByLearner ────────────────────────────────────────────────

  describe('getInterviewsByLearner()', () => {
    it('should GET interviews for a learner sorted by date', () => {
      service.getInterviewsByLearner(2).subscribe(ivs => {
        expect(ivs.length).toBe(1);
        expect(ivs[0].learnerId).toBe(2);
      });
      httpMock.expectOne(`${apiUrl}/learner/2`).flush([mockInterview]);
    });
  });

  // ─── getInterviewsByApplication ────────────────────────────────────────────

  describe('getInterviewsByApplication()', () => {
    it('should GET interviews for a specific application', () => {
      service.getInterviewsByApplication(10).subscribe(ivs => {
        expect(ivs[0].applicationId).toBe(10);
      });
      httpMock.expectOne(`${apiUrl}/application/10`).flush([mockInterview]);
    });
  });

  // ─── updateInterviewStatus ─────────────────────────────────────────────────

  describe('updateInterviewStatus()', () => {
    it('should PUT status COMPLETED', () => {
      const completed = { ...mockInterview, status: 'COMPLETED' };
      service.updateInterviewStatus(1, 'COMPLETED').subscribe(iv => {
        expect(iv.status).toBe('COMPLETED');
      });
      const req = httpMock.expectOne(`${apiUrl}/1/status?status=COMPLETED`);
      expect(req.request.method).toBe('PUT');
      req.flush(completed);
    });

    it('should PUT status CANCELLED', () => {
      service.updateInterviewStatus(1, 'CANCELLED').subscribe(iv => {
        expect(iv.status).toBe('CANCELLED');
      });
      httpMock.expectOne(`${apiUrl}/1/status?status=CANCELLED`).flush({ ...mockInterview, status: 'CANCELLED' });
    });

    it('should PUT status RESCHEDULED', () => {
      service.updateInterviewStatus(1, 'RESCHEDULED').subscribe(iv => {
        expect(iv.status).toBe('RESCHEDULED');
      });
      httpMock.expectOne(`${apiUrl}/1/status?status=RESCHEDULED`).flush({ ...mockInterview, status: 'RESCHEDULED' });
    });
  });

  // ─── updateInterview ───────────────────────────────────────────────────────

  describe('updateInterview()', () => {
    it('should PUT updated interview data', () => {
      const updatedRequest = { ...mockRequest, location: 'Visioconférence' };
      const updatedIv = { ...mockInterview, location: 'Visioconférence', status: 'RESCHEDULED' };
      service.updateInterview(1, updatedRequest).subscribe(iv => {
        expect(iv.location).toBe('Visioconférence');
        expect(iv.status).toBe('RESCHEDULED');
      });
      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(updatedIv);
    });
  });

  // ─── deleteInterview ───────────────────────────────────────────────────────

  describe('deleteInterview()', () => {
    it('should DELETE interview by id', () => {
      let completed = false;
      service.deleteInterview(1).subscribe(() => completed = true);
      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
      expect(completed).toBeTrue();
    });
  });
});
