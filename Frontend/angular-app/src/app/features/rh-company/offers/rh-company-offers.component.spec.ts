import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RhCompanyOffersComponent } from './rh-company-offers.component';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

describe('RhCompanyOffersComponent', () => {
  let component: RhCompanyOffersComponent;
  let fixture: ComponentFixture<RhCompanyOffersComponent>;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const apiUrl = environment.apiUrl;

  const mockOffer = {
    id: 1, title: 'Dev Java', description: 'Poste Java',
    companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
    companyId: 5, status: 'ACTIVE', positions: 2, remote: false,
    applicationCount: 3, viewCount: 10
  };

  const mockApplication = {
    id: 1, offerId: 1, learnerId: 10,
    learnerName: 'Alice', learnerEmail: 'alice@test.com',
    status: 'PENDING', score: 75, appliedAt: '2026-01-01T10:00:00'
  };

  const mockInterview = {
    id: 1, applicationId: 1, offerId: 1, learnerId: 10,
    learnerName: 'Alice', learnerEmail: 'alice@test.com',
    interviewDate: '2030-06-15T10:00:00', location: 'Paris',
    status: 'SCHEDULED', createdBy: 5, hasFeedback: false
  };

  const mockUserInfo = {
    token: 'jwt', userId: 5, email: 'rh@test.com',
    firstName: 'RH User', role: 'RH_COMPANY', experience: 5, message: 'OK'
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);
    authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

    await TestBed.configureTestingModule({
      imports: [
        RhCompanyOffersComponent, CommonModule, FormsModule,
        RouterModule.forRoot([]), HttpClientTestingModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RhCompanyOffersComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── Initialisation ────────────────────────────────────────────────────────

  describe('ngOnInit()', () => {
    it('should set companyId and userId from AuthService', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([mockInterview]);
      expect(component.companyId).toBe(5);
      expect(component.userId).toBe(5);
    });

    it('should load offers on init', () => {
      fixture.detectChanges();
      const req = httpMock.expectOne(`${apiUrl}/offers/company/5`);
      expect(req.request.method).toBe('GET');
      req.flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
      expect(component.offers.length).toBe(1);
    });

    it('should load interviews on init', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      const req = httpMock.expectOne(`${apiUrl}/interviews/company/5`);
      req.flush([mockInterview]);
      expect(component.interviews.length).toBe(1);
    });
  });

  // ─── Filtres ───────────────────────────────────────────────────────────────

  describe('applyFilter()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer, { ...mockOffer, id: 2, status: 'CLOSED' }]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should show all offers when filter is ALL', () => {
      component.filterStatus = 'ALL';
      component.applyFilter();
      expect(component.filteredOffers.length).toBe(2);
    });

    it('should filter ACTIVE offers only', () => {
      component.filterStatus = 'ACTIVE';
      component.applyFilter();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].status).toBe('ACTIVE');
    });

    it('should filter CLOSED offers only', () => {
      component.filterStatus = 'CLOSED';
      component.applyFilter();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].status).toBe('CLOSED');
    });
  });

  // ─── Offer form ────────────────────────────────────────────────────────────

  describe('emptyOfferForm()', () => {
    it('should return form with default values', () => {
      const form = component.emptyOfferForm();
      expect(form.title).toBe('');
      expect(form.contractType).toBe('CDI');
      expect(form.status).toBe('ACTIVE');
      expect(form.positions).toBe(1);
      expect(form.remote).toBeFalse();
    });
  });

  describe('openCreateOffer()', () => {
    it('should open modal in create mode with empty form', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);

      component.openCreateOffer();
      expect(component.showOfferModal).toBeTrue();
      expect(component.isEditMode).toBeFalse();
      expect(component.offerForm.title).toBe('');
    });
  });

  describe('openEditOffer()', () => {
    it('should open modal in edit mode with offer data', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);

      component.openEditOffer(mockOffer);
      expect(component.showOfferModal).toBeTrue();
      expect(component.isEditMode).toBeTrue();
      expect(component.offerForm.title).toBe('Dev Java');
      expect(component.offerForm.contractType).toBe('CDI');
      expect(component.selectedOffer).toEqual(mockOffer);
    });
  });

  // ─── saveOffer validation ──────────────────────────────────────────────────

  describe('saveOffer() — validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should POST offer when all required fields are filled', () => {
      component.offerForm = {
        title: 'Dev Java', description: 'Poste Java',
        companyName: 'TechCorp', location: 'Paris',
        contractType: 'CDI', status: 'ACTIVE', positions: 1,
        remote: false, salary: '', salaryMin: null, salaryMax: null,
        domain: '', experienceLevel: 'MID', expiresAt: null
      };
      component.isEditMode = false;
      component.saveOffer();

      const req = httpMock.expectOne(`${apiUrl}/offers`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.companyId).toBe(5);
      req.flush(mockOffer);
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
    });
  });

  // ─── duplicateOffer ────────────────────────────────────────────────────────

  describe('duplicateOffer()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should POST duplicate with "(copie)" suffix and DRAFT status', () => {
      component.duplicateOffer(mockOffer);
      const req = httpMock.expectOne(`${apiUrl}/offers`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.title).toContain('copie');
      expect(req.request.body.status).toBe('DRAFT');
      req.flush({ ...mockOffer, id: 2, title: 'Dev Java (copie)', status: 'DRAFT' });
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
    });
  });

  // ─── toggleOfferStatus ─────────────────────────────────────────────────────

  describe('toggleOfferStatus()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should toggle ACTIVE offer to CLOSED', () => {
      component.toggleOfferStatus(mockOffer);
      const req = httpMock.expectOne(`${apiUrl}/offers/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body.status).toBe('CLOSED');
      req.flush({ ...mockOffer, status: 'CLOSED' });
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
    });

    it('should toggle CLOSED offer to ACTIVE', () => {
      const closedOffer = { ...mockOffer, status: 'CLOSED' };
      component.toggleOfferStatus(closedOffer);
      const req = httpMock.expectOne(`${apiUrl}/offers/1`);
      expect(req.request.body.status).toBe('ACTIVE');
      req.flush({ ...closedOffer, status: 'ACTIVE' });
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
    });
  });

  // ─── deleteOffer ───────────────────────────────────────────────────────────

  describe('deleteOffer()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should DELETE offer after confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      component.deleteOffer(1);
      const req = httpMock.expectOne(`${apiUrl}/offers/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
    });

    it('should not DELETE if user cancels', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      component.deleteOffer(1);
      httpMock.expectNone(`${apiUrl}/offers/1`);
      expect(component.offers.length).toBe(1); // offers unchanged
    });
  });

  // ─── viewApplications ─────────────────────────────────────────────────────

  describe('viewApplications()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should GET applications for offer and show modal', () => {
      component.viewApplications(mockOffer);
      const req = httpMock.expectOne(`${apiUrl}/applications/offer/1`);
      req.flush([mockApplication]);
      expect(component.applications.length).toBe(1);
      expect(component.showApplicationsModal).toBeTrue();
    });
  });

  // ─── updateAppStatus ───────────────────────────────────────────────────────

  describe('updateAppStatus()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
      component.applications = [mockApplication];
    });

    it('should PUT application status and update local list', () => {
      const updated = { ...mockApplication, status: 'ACCEPTED' };
      component.updateAppStatus(1, 'ACCEPTED');
      const req = httpMock.expectOne(`${apiUrl}/applications/1/status?status=ACCEPTED`);
      expect(req.request.method).toBe('PUT');
      req.flush(updated);
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      expect(component.applications[0].status).toBe('ACCEPTED');
    });
  });

  // ─── Interview form ────────────────────────────────────────────────────────

  describe('emptyInterviewForm()', () => {
    it('should return form with default location "En ligne"', () => {
      const form = component.emptyInterviewForm();
      expect(form.location).toBe('En ligne');
      expect(form.applicationId).toBe(0);
    });
  });

  describe('openScheduleInterview()', () => {
    it('should set selectedApplication and open interview modal', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);

      component.openScheduleInterview(mockApplication);
      expect(component.showInterviewModal).toBeTrue();
      expect(component.selectedApplication).toEqual(mockApplication);
      expect(component.interviewForm.applicationId).toBe(1);
    });
  });

  // ─── Feedback form ─────────────────────────────────────────────────────────

  describe('emptyFeedbackForm()', () => {
    it('should return form with default rating 3 and PENDING decision', () => {
      const form = component.emptyFeedbackForm();
      expect(form.rating).toBe(3);
      expect(form.decision).toBe('PENDING');
    });
  });

  describe('openFeedback()', () => {
    it('should populate feedbackForm with interview data', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);

      component.openFeedback(mockInterview);
      expect(component.showFeedbackModal).toBeTrue();
      expect(component.feedbackForm.interviewId).toBe(1);
      expect(component.feedbackForm.applicationId).toBe(1);
      expect(component.feedbackForm.submittedBy).toBe(5);
    });
  });

  // ─── statusColor ───────────────────────────────────────────────────────────

  describe('statusColor()', () => {
    it('should return green class for ACTIVE', () => {
      expect(component.statusColor('ACTIVE')).toContain('green');
    });

    it('should return red class for CLOSED', () => {
      expect(component.statusColor('CLOSED')).toContain('red');
    });

    it('should return gray class for DRAFT', () => {
      expect(component.statusColor('DRAFT')).toContain('gray');
    });

    it('should return blue class for SCHEDULED', () => {
      expect(component.statusColor('SCHEDULED')).toContain('blue');
    });

    it('should return yellow class for PENDING', () => {
      expect(component.statusColor('PENDING')).toContain('yellow');
    });
  });

  // ─── interviewsByStatus ────────────────────────────────────────────────────

  describe('interviewsByStatus getter', () => {
    it('should count interviews by status', () => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);

      component.interviews = [
        { ...mockInterview, status: 'SCHEDULED' },
        { ...mockInterview, id: 2, status: 'COMPLETED' },
        { ...mockInterview, id: 3, status: 'CANCELLED' },
        { ...mockInterview, id: 4, status: 'SCHEDULED' }
      ];
      const counts = component.interviewsByStatus;
      expect(counts.scheduled).toBe(2);
      expect(counts.completed).toBe(1);
      expect(counts.cancelled).toBe(1);
    });
  });

  // ─── scoreColor / scoreLabel ──────────────────────────────────────────────

  describe('scoreColor() / scoreLabel()', () => {
    it('should return green class for score >= 75', () => {
      expect(component.scoreColor(75)).toContain('green');
      expect(component.scoreColor(100)).toContain('green');
    });

    it('should return yellow class for score 50-74', () => {
      expect(component.scoreColor(50)).toContain('yellow');
      expect(component.scoreColor(74)).toContain('yellow');
    });

    it('should return orange class for score 25-49', () => {
      expect(component.scoreColor(25)).toContain('orange');
      expect(component.scoreColor(49)).toContain('orange');
    });

    it('should return red class for score < 25', () => {
      expect(component.scoreColor(0)).toContain('red');
      expect(component.scoreColor(24)).toContain('red');
    });

    it('should return Excellent label for score >= 75', () => {
      expect(component.scoreLabel(75)).toBe('Excellent');
      expect(component.scoreLabel(100)).toBe('Excellent');
    });

    it('should return Bon label for score 50-74', () => {
      expect(component.scoreLabel(50)).toBe('Bon');
    });

    it('should return Moyen label for score 25-49', () => {
      expect(component.scoreLabel(25)).toBe('Moyen');
    });

    it('should return Faible label for score < 25', () => {
      expect(component.scoreLabel(0)).toBe('Faible');
    });
  });

  // ─── loadMatchAnalysis ────────────────────────────────────────────────────

  describe('loadMatchAnalysis()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([mockOffer]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should load match analysis for an application', () => {
      const mockAnalysis = {
        totalScore: 75, skillScore: 40, experienceScore: 20, coverLetterScore: 15,
        matchedSkills: ['Java'], missingSkills: ['Docker'],
        suggestions: ['Mentionnez Docker'], matchLevel: 'EXCELLENT'
      };
      component.loadMatchAnalysis(1);
      const req = httpMock.expectOne(`${apiUrl}/applications/1/match-analysis`);
      req.flush(mockAnalysis);
      expect(component.matchAnalysis[1]).toEqual(mockAnalysis);
    });

    it('should not reload if already loaded', () => {
      component.matchAnalysis[1] = { totalScore: 50 };
      component.loadMatchAnalysis(1);
      httpMock.expectNone(`${apiUrl}/applications/1/match-analysis`);
    });

    it('should handle error gracefully', () => {
      component.loadMatchAnalysis(999);
      const req = httpMock.expectOne(`${apiUrl}/applications/999/match-analysis`);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
      expect(component.matchAnalysis[999]).toBeUndefined();
    });
  });

  describe('onTabChange()', () => {
    beforeEach(() => {
      fixture.detectChanges();
      httpMock.expectOne(`${apiUrl}/offers/company/5`).flush([]);
      httpMock.expectOne(`${apiUrl}/interviews/company/5`).flush([]);
    });

    it('should switch to interviews tab', () => {
      component.onTabChange('interviews');
      expect(component.activeTab).toBe('interviews');
    });

    it('should load stats when switching to stats tab', () => {
      component.onTabChange('stats');
      expect(component.activeTab).toBe('stats');
      const req = httpMock.expectOne(`${apiUrl}/offers/stats/company/5`);
      req.flush({ totalOffers: 5, activeOffers: 3 });
      expect(component.stats).toBeTruthy();
    });

    it('should not reload stats if already loaded', () => {
      component.stats = { totalOffers: 5 };
      component.onTabChange('stats');
      httpMock.expectNone(`${apiUrl}/offers/stats/company/5`);
    });
  });
});
