import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { JobOffersLearnerComponent } from './job-offers-learner.component';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { Offer } from '../../../core/models/offer.model';
import { Application } from '../../../core/models/application.model';

describe('JobOffersLearnerComponent', () => {
  let component: JobOffersLearnerComponent;
  let fixture: ComponentFixture<JobOffersLearnerComponent>;
  let offerServiceSpy: jasmine.SpyObj<OfferService>;
  let applicationServiceSpy: jasmine.SpyObj<ApplicationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockOffers: Offer[] = [
    {
      id: 1, title: 'Dev Java Senior', description: 'Poste Java',
      companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
      companyId: 10, status: 'ACTIVE', positions: 2, remote: false,
      domain: 'Informatique', experienceLevel: 'SENIOR',
      requiredSkills: ['Java', 'Spring']
    },
    {
      id: 2, title: 'Data Scientist', description: 'ML/AI',
      companyName: 'DataCo', location: 'Lyon', contractType: 'CDD',
      companyId: 11, status: 'ACTIVE', positions: 1, remote: true,
      domain: 'Informatique', experienceLevel: 'MID'
    }
  ];

  const mockApplications: Application[] = [
    {
      id: 1, offerId: 1, offerTitle: 'Dev Java Senior',
      learnerId: 5, learnerName: 'Alice', learnerEmail: 'alice@test.com',
      status: 'PENDING', score: 75, appliedAt: '2026-01-01T10:00:00'
    }
  ];

  const mockUserInfo = {
    token: 'jwt-token', userId: 5, email: 'alice@test.com',
    firstName: 'Alice', role: 'LEARNER', experience: 3, message: 'OK'
  };

  beforeEach(async () => {
    offerServiceSpy = jasmine.createSpyObj('OfferService', ['getOffersByStatus', 'getOfferById']);
    applicationServiceSpy = jasmine.createSpyObj('ApplicationService', [
      'getApplicationsByLearner', 'applyToOffer', 'withdrawApplication'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo', 'isAuthenticated']);

    offerServiceSpy.getOffersByStatus.and.returnValue(of(mockOffers));
    applicationServiceSpy.getApplicationsByLearner.and.returnValue(of(mockApplications));
    authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);
    authServiceSpy.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [JobOffersLearnerComponent, CommonModule, FormsModule, RouterModule.forRoot([])],
      providers: [
        { provide: OfferService, useValue: offerServiceSpy },
        { provide: ApplicationService, useValue: applicationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(JobOffersLearnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ─── Initialisation ────────────────────────────────────────────────────────

  describe('ngOnInit()', () => {
    it('should load user info from AuthService', () => {
      expect(component.currentUserId).toBe(5);
      expect(component.currentUserName).toBe('Alice');
      expect(component.currentUserEmail).toBe('alice@test.com');
      expect(component.currentUserExperience).toBe(3);
    });

    it('should pre-fill yearsOfExperience from user profile', () => {
      expect(component.yearsOfExperience).toBe(3);
    });

    it('should load ACTIVE offers only', () => {
      expect(offerServiceSpy.getOffersByStatus).toHaveBeenCalledWith('ACTIVE');
      expect(component.offers.length).toBe(2);
    });

    it('should load user applications and build applicationMap', () => {
      expect(applicationServiceSpy.getApplicationsByLearner).toHaveBeenCalledWith(5);
      expect(component.applicationMap.size).toBe(1);
      // Check the status from the map (not the mutated mockApplications array)
      const app = component.applicationMap.get(1);
      expect(app).toBeTruthy();
      expect(app?.offerId).toBe(1);
    });

    it('should apply filters after loading offers', () => {
      expect(component.filteredOffers.length).toBe(2);
    });
  });

  // ─── Filtres ───────────────────────────────────────────────────────────────

  describe('applyFilters()', () => {
    it('should filter by contract type CDI', () => {
      component.filterContractType = 'CDI';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].contractType).toBe('CDI');
    });

    it('should filter by experience level SENIOR', () => {
      component.filterExperienceLevel = 'SENIOR';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].experienceLevel).toBe('SENIOR');
    });

    it('should filter remote offers only', () => {
      component.filterRemote = true;
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].remote).toBeTrue();
    });

    it('should filter by search term (title)', () => {
      component.searchTerm = 'java';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].title).toContain('Java');
    });

    it('should filter by search term (company name)', () => {
      component.searchTerm = 'dataco';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].companyName).toBe('DataCo');
    });

    it('should return all offers when no filter applied', () => {
      component.filterContractType = 'ALL';
      component.filterExperienceLevel = 'ALL';
      component.filterRemote = false;
      component.searchTerm = '';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(2);
    });

    it('should return empty array when no match', () => {
      component.searchTerm = 'xyz-inexistant';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(0);
    });
  });

  // ─── hasApplied / getApplication ──────────────────────────────────────────

  describe('hasApplied() / getApplication()', () => {
    beforeEach(() => {
      // Ensure applicationMap is populated fresh for each test
      component.applicationMap.clear();
      component.applicationMap.set(1, { ...mockApplications[0] });
    });

    it('should return true for offer already applied', () => {
      expect(component.hasApplied(1)).toBeTrue();
    });

    it('should return false for offer not applied', () => {
      expect(component.hasApplied(999)).toBeFalse();
    });

    it('should return the application for a given offerId', () => {
      const app = component.getApplication(1);
      expect(app?.offerId).toBe(1);
      expect(app?.score).toBe(75);
    });

    it('should return undefined for unknown offerId', () => {
      expect(component.getApplication(999)).toBeUndefined();
    });
  });

  // ─── Modals ────────────────────────────────────────────────────────────────

  describe('viewDetails() / closeDetailModal()', () => {
    it('should open detail modal with selected offer', () => {
      component.viewDetails(mockOffers[0]);
      expect(component.showDetailModal).toBeTrue();
      expect(component.selectedOffer?.id).toBe(1);
    });

    it('should close detail modal and clear selection', () => {
      component.viewDetails(mockOffers[0]);
      component.closeDetailModal();
      expect(component.showDetailModal).toBeFalse();
      expect(component.selectedOffer).toBeNull();
    });
  });

  describe('openApplyModal() / closeApplicationModal()', () => {
    it('should open application modal and reset form', () => {
      component.openApplyModal(mockOffers[0]);
      expect(component.showApplicationModal).toBeTrue();
      expect(component.selectedOffer?.id).toBe(1);
      expect(component.coverLetter).toBe('');
      expect(component.candidateSkills).toEqual([]);
    });

    it('should close detail modal when opening apply modal', () => {
      component.showDetailModal = true;
      component.openApplyModal(mockOffers[0]);
      expect(component.showDetailModal).toBeFalse();
    });

    it('should pre-fill yearsOfExperience from user profile', () => {
      component.openApplyModal(mockOffers[0]);
      expect(component.yearsOfExperience).toBe(3);
    });

    it('should close application modal and clear state', () => {
      component.openApplyModal(mockOffers[0]);
      component.closeApplicationModal();
      expect(component.showApplicationModal).toBeFalse();
      expect(component.selectedOffer).toBeNull();
    });
  });

  // ─── Skills management ─────────────────────────────────────────────────────

  describe('parseSkills() / removeSkill()', () => {
    it('should parse comma-separated skills', () => {
      component.skillsInput = 'Java, Spring, MySQL';
      component.parseSkills();
      expect(component.candidateSkills).toEqual(['Java', 'Spring', 'MySQL']);
    });

    it('should trim whitespace from skills', () => {
      component.skillsInput = '  Java  ,  Spring  ';
      component.parseSkills();
      expect(component.candidateSkills).toEqual(['Java', 'Spring']);
    });

    it('should filter empty entries', () => {
      component.skillsInput = 'Java,,Spring,';
      component.parseSkills();
      expect(component.candidateSkills).toEqual(['Java', 'Spring']);
    });

    it('should remove a specific skill', () => {
      component.candidateSkills = ['Java', 'Spring', 'MySQL'];
      component.removeSkill('Spring');
      expect(component.candidateSkills).toEqual(['Java', 'MySQL']);
    });

    it('should update skillsInput after removing a skill', () => {
      component.candidateSkills = ['Java', 'Spring'];
      component.removeSkill('Java');
      expect(component.skillsInput).toBe('Spring');
    });
  });

  // ─── submitApplication ─────────────────────────────────────────────────────

  describe('submitApplication()', () => {
    it('should show error if no CV file selected', () => {
      component.selectedOffer = mockOffers[0];
      component.cvFile = null;
      component.submitApplication();
      expect(component.errorMessage).toContain('CV');
    });

    it('should not call applyToOffer if no CV', () => {
      component.selectedOffer = mockOffers[0];
      component.cvFile = null;
      component.submitApplication();
      expect(applicationServiceSpy.applyToOffer).not.toHaveBeenCalled();
    });
  });

  // ─── withdrawApplication ───────────────────────────────────────────────────

  describe('withdrawApplication()', () => {
    it('should call withdrawApplication service and update applicationMap', () => {
      const withdrawn: Application = { ...mockApplications[0], status: 'WITHDRAWN' };
      applicationServiceSpy.withdrawApplication.and.returnValue(of(withdrawn));
      spyOn(window, 'confirm').and.returnValue(true);

      component.withdrawApplication(mockApplications[0]);

      expect(applicationServiceSpy.withdrawApplication).toHaveBeenCalledWith(1, 5);
      expect(component.applicationMap.get(1)?.status).toBe('WITHDRAWN');
    });

    it('should not call service if user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      component.withdrawApplication(mockApplications[0]);
      expect(applicationServiceSpy.withdrawApplication).not.toHaveBeenCalled();
    });

    it('should show error message on service failure', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      applicationServiceSpy.withdrawApplication.and.returnValue(
        throwError(() => ({ error: { message: 'Impossible de retirer' } }))
      );
      component.withdrawApplication(mockApplications[0]);
      expect(component.errorMessage).toContain('Impossible de retirer');
    });
  });

  // ─── Helpers ───────────────────────────────────────────────────────────────

  describe('statusLabel() / statusClass() / scoreColor() / canWithdraw()', () => {
    it('should return correct label for PENDING', () => {
      expect(component.statusLabel('PENDING')).toContain('attente');
    });

    it('should return correct label for ACCEPTED', () => {
      expect(component.statusLabel('ACCEPTED')).toContain('Acceptée');
    });

    it('should return correct label for WITHDRAWN', () => {
      expect(component.statusLabel('WITHDRAWN')).toContain('Retirée');
    });

    it('should return green class for score >= 70', () => {
      expect(component.scoreColor(70)).toBe('text-green-600');
      expect(component.scoreColor(100)).toBe('text-green-600');
    });

    it('should return yellow class for score 40-69', () => {
      expect(component.scoreColor(40)).toBe('text-yellow-600');
      expect(component.scoreColor(69)).toBe('text-yellow-600');
    });

    it('should return red class for score < 40', () => {
      expect(component.scoreColor(0)).toBe('text-red-500');
      expect(component.scoreColor(39)).toBe('text-red-500');
    });

    it('should allow withdrawal for PENDING status', () => {
      expect(component.canWithdraw('PENDING')).toBeTrue();
    });

    it('should allow withdrawal for REVIEWED status', () => {
      expect(component.canWithdraw('REVIEWED')).toBeTrue();
    });

    it('should not allow withdrawal for ACCEPTED status', () => {
      expect(component.canWithdraw('ACCEPTED')).toBeFalse();
    });

    it('should not allow withdrawal for REJECTED status', () => {
      expect(component.canWithdraw('REJECTED')).toBeFalse();
    });
  });

  // ─── Computed properties ───────────────────────────────────────────────────

  describe('activeApplicationsCount / acceptedCount', () => {
    it('should count non-withdrawn applications', () => {
      component.myApplications = [
        { ...mockApplications[0], status: 'PENDING' },
        { ...mockApplications[0], id: 2, status: 'WITHDRAWN' }
      ];
      expect(component.activeApplicationsCount).toBe(1);
    });

    it('should count accepted applications', () => {
      component.myApplications = [
        { ...mockApplications[0], status: 'ACCEPTED' },
        { ...mockApplications[0], id: 2, status: 'PENDING' }
      ];
      expect(component.acceptedCount).toBe(1);
    });
  });
});
