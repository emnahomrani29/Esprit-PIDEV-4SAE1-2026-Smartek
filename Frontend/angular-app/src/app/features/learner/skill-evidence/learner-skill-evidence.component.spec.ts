import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LearnerSkillEvidenceComponent } from './learner-skill-evidence.component';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { SkillEvidenceResponse } from '../../../core/models/skill-evidence.model';

describe('LearnerSkillEvidenceComponent', () => {
  let component: LearnerSkillEvidenceComponent;
  let fixture: ComponentFixture<LearnerSkillEvidenceComponent>;
  let mockSkillEvidenceService: jasmine.SpyObj<SkillEvidenceService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUser: AuthResponse = {
    token: 'fake-token', userId: 10, firstName: 'Alice',
    email: 'alice@test.com', role: 'LEARNER', message: 'OK'
  };

  const mockEvidences: SkillEvidenceResponse[] = [
    {
      evidenceId: 1, title: 'Java Cert', fileUrl: 'http://file.pdf',
      description: 'Oracle Java', uploadDate: '2026-01-01',
      learnerId: 10, learnerName: 'Alice', learnerEmail: 'alice@test.com',
      status: 'PENDING', category: 'PROGRAMMING'
    },
    {
      evidenceId: 2, title: 'Angular Skills', fileUrl: '',
      description: 'Frontend', uploadDate: '2026-02-01',
      learnerId: 10, learnerName: 'Alice', learnerEmail: 'alice@test.com',
      status: 'APPROVED', score: 90, category: 'PROGRAMMING'
    },
    {
      evidenceId: 3, title: 'Design Portfolio', fileUrl: '',
      description: 'UI/UX', uploadDate: '2026-03-01',
      learnerId: 10, learnerName: 'Alice', learnerEmail: 'alice@test.com',
      status: 'REJECTED', category: 'DESIGN'
    }
  ];

  beforeEach(async () => {
    mockSkillEvidenceService = jasmine.createSpyObj('SkillEvidenceService', [
      'getByLearner', 'create', 'update', 'delete'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    mockAuthService.getUserInfo.and.returnValue(mockUser);
    mockSkillEvidenceService.getByLearner.and.returnValue(of(mockEvidences));

    await TestBed.configureTestingModule({
      imports: [LearnerSkillEvidenceComponent],
      providers: [
        { provide: SkillEvidenceService, useValue: mockSkillEvidenceService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LearnerSkillEvidenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ===== Initialisation =====

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load evidences on init', () => {
    expect(mockSkillEvidenceService.getByLearner).toHaveBeenCalledWith(10);
    expect(component.evidences.length).toBe(3);
  });

  it('should set loading to false after load', () => {
    expect(component.loading).toBeFalse();
  });

  // ===== LOGIQUE MÉTIER : Compteurs de statut =====

  it('should count pending evidences correctly', () => {
    expect(component.getPendingCount()).toBe(1);
  });

  it('should count approved evidences correctly', () => {
    expect(component.getApprovedCount()).toBe(1);
  });

  it('should count rejected evidences correctly', () => {
    expect(component.getRejectedCount()).toBe(1);
  });

  it('should return 0 counts when evidences list is empty', () => {
    component.evidences = [];
    expect(component.getPendingCount()).toBe(0);
    expect(component.getApprovedCount()).toBe(0);
    expect(component.getRejectedCount()).toBe(0);
  });

  // ===== LOGIQUE MÉTIER : Helpers de statut =====

  it('should return correct color for APPROVED status', () => {
    expect(component.getStatusColor('APPROVED')).toContain('green');
  });

  it('should return correct color for REJECTED status', () => {
    expect(component.getStatusColor('REJECTED')).toContain('red');
  });

  it('should return yellow color for PENDING status', () => {
    expect(component.getStatusColor('PENDING')).toContain('yellow');
  });

  it('should return correct label for APPROVED', () => {
    expect(component.getStatusLabel('APPROVED')).toBe('Approuvé');
  });

  it('should return correct label for REJECTED', () => {
    expect(component.getStatusLabel('REJECTED')).toBe('Rejeté');
  });

  it('should return correct label for PENDING', () => {
    expect(component.getStatusLabel('PENDING')).toBe('En attente');
  });

  // ===== LOGIQUE MÉTIER : Catégories =====

  it('should return correct category label for PROGRAMMING', () => {
    expect(component.getCategoryLabel('PROGRAMMING')).toBe('Programming');
  });

  it('should return Other for unknown category', () => {
    expect(component.getCategoryLabel(undefined)).toBe('Other');
  });

  // ===== LOGIQUE MÉTIER : shouldShowScore =====

  it('should show score for APPROVED evidence with score', () => {
    expect(component.shouldShowScore(mockEvidences[1])).toBeTrue();
  });

  it('should not show score for PENDING evidence', () => {
    expect(component.shouldShowScore(mockEvidences[0])).toBeFalse();
  });

  it('should not show score for REJECTED evidence', () => {
    expect(component.shouldShowScore(mockEvidences[2])).toBeFalse();
  });

  // ===== LOGIQUE MÉTIER : Modal =====

  it('should open add modal with empty form', () => {
    component.openAddModal();
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeFalse();
    expect(component.currentEvidence.title).toBe('');
  });

  it('should open edit modal with evidence data', () => {
    component.openEditModal(mockEvidences[0]);
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.currentEvidence.title).toBe('Java Cert');
    expect(component.currentEvidence.evidenceId).toBe(1);
  });

  it('should close modal and clear messages', () => {
    component.showModal = true;
    component.successMessage = 'test';
    component.closeModal();
    expect(component.showModal).toBeFalse();
    expect(component.successMessage).toBe('');
  });

  it('should open detail modal with selected evidence', () => {
    component.openDetailModal(mockEvidences[0]);
    expect(component.showDetailModal).toBeTrue();
    expect(component.selectedEvidence).toEqual(mockEvidences[0]);
  });

  it('should close detail modal', () => {
    component.showDetailModal = true;
    component.closeDetailModal();
    expect(component.showDetailModal).toBeFalse();
    expect(component.selectedEvidence).toBeNull();
  });

  // ===== LOGIQUE MÉTIER : Validation saveEvidence =====

  it('should show error when title is empty on save', () => {
    component.currentEvidence = { title: '', fileUrl: '', description: '', category: '' };
    component.saveEvidence();
    expect(component.errorMessage).toBe('Le titre est obligatoire');
    expect(mockSkillEvidenceService.create).not.toHaveBeenCalled();
  });

  it('should show error when category is empty on save', () => {
    component.currentEvidence = { title: 'Test', fileUrl: '', description: '', category: '' };
    component.saveEvidence();
    expect(component.errorMessage).toBe('La catégorie est obligatoire');
    expect(mockSkillEvidenceService.create).not.toHaveBeenCalled();
  });

  it('should call create service on save in add mode', () => {
    mockSkillEvidenceService.create.and.returnValue(of(mockEvidences[0]));
    component.isEditMode = false;
    component.currentEvidence = { title: 'New Evidence', fileUrl: '', description: '', category: 'PROGRAMMING' };
    component.saveEvidence();
    expect(mockSkillEvidenceService.create).toHaveBeenCalled();
  });

  it('should call update service on save in edit mode', () => {
    mockSkillEvidenceService.update.and.returnValue(of(mockEvidences[0]));
    component.isEditMode = true;
    component.currentEvidence = { evidenceId: 1, title: 'Updated', fileUrl: '', description: '', category: 'PROGRAMMING' };
    component.saveEvidence();
    expect(mockSkillEvidenceService.update).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  // ===== LOGIQUE MÉTIER : Delete =====

  it('should call delete service and reload on delete', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockSkillEvidenceService.delete.and.returnValue(of(undefined));
    component.deleteEvidence(1);
    expect(mockSkillEvidenceService.delete).toHaveBeenCalledWith(1);
    expect(mockSkillEvidenceService.getByLearner).toHaveBeenCalledTimes(2);
  });

  it('should not delete when user cancels confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteEvidence(1);
    expect(mockSkillEvidenceService.delete).not.toHaveBeenCalled();
  });

  // ===== Gestion d'erreurs =====

  it('should set errorMessage on load failure', () => {
    mockSkillEvidenceService.getByLearner.and.returnValue(throwError(() => new Error('Network error')));
    component.loadEvidences();
    expect(component.errorMessage).toBe('Erreur lors du chargement');
    expect(component.loading).toBeFalse();
  });

  it('should set errorMessage on save failure', () => {
    mockSkillEvidenceService.create.and.returnValue(throwError(() => ({ error: { message: 'Duplicate title' } })));
    component.isEditMode = false;
    component.currentEvidence = { title: 'Test', fileUrl: '', description: '', category: 'PROGRAMMING' };
    component.saveEvidence();
    expect(component.errorMessage).toBe('Duplicate title');
  });

  // ===== formatDate =====

  it('should format date correctly', () => {
    const formatted = component.formatDate('2026-01-15');
    expect(formatted).toContain('2026');
  });

  it('should return empty string for empty date', () => {
    expect(component.formatDate('')).toBe('');
  });
});
