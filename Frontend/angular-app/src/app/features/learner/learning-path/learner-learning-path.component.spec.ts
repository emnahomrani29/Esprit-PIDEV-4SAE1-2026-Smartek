import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LearnerLearningPathComponent } from './learner-learning-path.component';
import { LearningPathService } from '../../../core/services/learning-path.service';
import { LearningStyleService } from '../../../core/services/learning-style.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { LearningPathResponse } from '../../../core/models/learning-path.model';
import { LearningStylePreferenceResponse } from '../../../core/models/learning-style.model';

describe('LearnerLearningPathComponent', () => {
  let component: LearnerLearningPathComponent;
  let fixture: ComponentFixture<LearnerLearningPathComponent>;
  let mockLearningPathService: jasmine.SpyObj<LearningPathService>;
  let mockLearningStyleService: jasmine.SpyObj<LearningStyleService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUser: AuthResponse = {
    token: 'fake-token', userId: 5, firstName: 'Bob',
    email: 'bob@test.com', role: 'LEARNER', message: 'OK'
  };

  const mockPaths: LearningPathResponse[] = [
    {
      pathId: 1, title: 'Spring Boot', description: 'Backend',
      learnerId: 5, learnerName: 'Bob', status: 'EN_COURS',
      startDate: '2026-01-01', endDate: '2026-06-30', progress: 40
    },
    {
      pathId: 2, title: 'Angular', description: 'Frontend',
      learnerId: 5, learnerName: 'Bob', status: 'TERMINE',
      startDate: '2025-01-01', endDate: '2025-12-31', progress: 100
    },
    {
      pathId: 3, title: 'DevOps', description: 'CI/CD',
      learnerId: 5, learnerName: 'Bob', status: 'PLANIFIE',
      startDate: '2026-07-01', progress: 0
    }
  ];

  const mockStyle: LearningStylePreferenceResponse = {
    id: 1, preferredStyle: 'VISUAL', videoPreferred: true,
    textPreferred: false, practicalWorkPreferred: true,
    lastUpdated: '2026-01-01', learnerId: 5, learnerName: 'Bob'
  };

  beforeEach(async () => {
    mockLearningPathService = jasmine.createSpyObj('LearningPathService', [
      'getByLearner', 'create', 'update', 'delete'
    ]);
    mockLearningStyleService = jasmine.createSpyObj('LearningStyleService', [
      'exists', 'getByLearner'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    mockAuthService.getUserInfo.and.returnValue(mockUser);
    mockLearningPathService.getByLearner.and.returnValue(of(mockPaths));
    mockLearningStyleService.exists.and.returnValue(of(true));
    mockLearningStyleService.getByLearner.and.returnValue(of(mockStyle));

    await TestBed.configureTestingModule({
      imports: [LearnerLearningPathComponent],
      providers: [
        { provide: LearningPathService, useValue: mockLearningPathService },
        { provide: LearningStyleService, useValue: mockLearningStyleService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LearnerLearningPathComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ===== Initialisation =====

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load paths on init', () => {
    expect(mockLearningPathService.getByLearner).toHaveBeenCalledWith(5);
    expect(component.paths.length).toBe(3);
  });

  it('should load learning style on init', () => {
    expect(mockLearningStyleService.exists).toHaveBeenCalledWith(5);
    expect(component.learningStyle).toEqual(mockStyle);
  });

  it('should initialize quickProgressMap from paths', () => {
    // quickProgressMap is populated from loaded paths
    expect(component.quickProgressMap[1]).toBeDefined();
    expect(component.quickProgressMap[2]).toBeDefined();
    expect(component.quickProgressMap[3]).toBeDefined();
  });

  // ===== LOGIQUE MÉTIER : Stats =====

  it('should count paths by status EN_COURS', () => {
    expect(component.getCountByStatus('EN_COURS')).toBe(1);
  });

  it('should count paths by status TERMINE', () => {
    expect(component.getCountByStatus('TERMINE')).toBe(1);
  });

  it('should count paths by status PLANIFIE', () => {
    expect(component.getCountByStatus('PLANIFIE')).toBe(1);
  });

  it('should calculate average progress correctly', () => {
    const avg = component.getAverageProgress();
    expect(avg).toBeGreaterThan(0);
    expect(avg).toBeLessThanOrEqual(100);
  });

  it('should return 0 average progress when no paths', () => {
    component.paths = [];
    expect(component.getAverageProgress()).toBe(0);
  });

  // ===== LOGIQUE MÉTIER : Filtres =====

  it('should filter paths by status', () => {
    component.filterStatus = 'EN_COURS';
    component.applyFilters();
    expect(component.filteredPaths.length).toBe(1);
    expect(component.filteredPaths[0].status).toBe('EN_COURS');
  });

  it('should filter paths by search term', () => {
    component.searchTerm = 'Angular';
    component.applyFilters();
    expect(component.filteredPaths.length).toBe(1);
    expect(component.filteredPaths[0].title).toBe('Angular');
  });

  it('should filter by both status and search term', () => {
    component.filterStatus = 'TERMINE';
    component.searchTerm = 'Angular';
    component.applyFilters();
    expect(component.filteredPaths.length).toBe(1);
  });

  it('should return all paths when no filter applied', () => {
    component.filterStatus = '';
    component.searchTerm = '';
    component.applyFilters();
    expect(component.filteredPaths.length).toBe(3);
  });

  it('should clear filters and show all paths', () => {
    component.filterStatus = 'EN_COURS';
    component.searchTerm = 'Spring';
    component.clearFilters();
    expect(component.filterStatus).toBe('');
    expect(component.searchTerm).toBe('');
    expect(component.filteredPaths.length).toBe(3);
  });

  // ===== LOGIQUE MÉTIER : Recommandations =====

  it('should return recommendations for VISUAL style', () => {
    const tips = component.getRecommendations();
    expect(tips.length).toBeGreaterThan(0);
    expect(tips[0]).toContain('mind maps');
  });

  it('should return empty recommendations when no learning style', () => {
    component.learningStyle = null;
    expect(component.getRecommendations()).toEqual([]);
  });

  it('should return correct icon for VISUAL style', () => {
    expect(component.getRecommendationIcon()).toBe('visibility');
  });

  it('should return lightbulb icon when no learning style', () => {
    component.learningStyle = null;
    expect(component.getRecommendationIcon()).toBe('lightbulb');
  });

  it('should return correct style label', () => {
    expect(component.getStyleLabel('VISUAL')).toBe('Visual');
    expect(component.getStyleLabel('AUDITORY')).toBe('Auditory');
    expect(component.getStyleLabel('READ_WRITE')).toBe('Read/Write');
    expect(component.getStyleLabel('KINESTHETIC')).toBe('Kinesthetic');
    expect(component.getStyleLabel('MULTIMODAL')).toBe('Multimodal');
  });

  // ===== LOGIQUE MÉTIER : Helpers de statut =====

  it('should return blue color for EN_COURS', () => {
    expect(component.getStatusColor('EN_COURS')).toContain('blue');
  });

  it('should return green color for TERMINE', () => {
    expect(component.getStatusColor('TERMINE')).toContain('green');
  });

  it('should return red color for ABANDONNE', () => {
    expect(component.getStatusColor('ABANDONNE')).toContain('red');
  });

  it('should return correct status labels', () => {
    expect(component.getStatusLabel('PLANIFIE')).toBe('Planifié');
    expect(component.getStatusLabel('EN_COURS')).toBe('En cours');
    expect(component.getStatusLabel('TERMINE')).toBe('Terminé');
    expect(component.getStatusLabel('ABANDONNE')).toBe('Abandonné');
  });

  // ===== LOGIQUE MÉTIER : Modal =====

  it('should open add modal with default form values', () => {
    component.openAddModal();
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeFalse();
    expect(component.form.title).toBe('');
    expect(component.form.status).toBe('PLANIFIE');
    expect(component.form.progress).toBe(0);
  });

  it('should open edit modal with path data', () => {
    component.openEditModal(mockPaths[0]);
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.form.title).toBe('Spring Boot');
    expect(component.form.pathId).toBe(1);
  });

  it('should close modal and clear messages', () => {
    component.showModal = true;
    component.successMessage = 'test';
    component.closeModal();
    expect(component.showModal).toBeFalse();
    expect(component.successMessage).toBe('');
  });

  it('should open detail modal', () => {
    component.openDetailModal(mockPaths[0]);
    expect(component.showDetailModal).toBeTrue();
    expect(component.selectedPath).toEqual(mockPaths[0]);
  });

  it('should close detail modal', () => {
    component.showDetailModal = true;
    component.closeDetailModal();
    expect(component.showDetailModal).toBeFalse();
    expect(component.selectedPath).toBeNull();
  });

  // ===== LOGIQUE MÉTIER : Validation savePath =====

  it('should show error when title is empty on save', () => {
    component.form = { title: '', description: '', status: 'PLANIFIE', startDate: '2026-01-01', endDate: '', progress: 0 };
    component.savePath();
    expect(component.errorMessage).toBe('Le titre est obligatoire');
    expect(mockLearningPathService.create).not.toHaveBeenCalled();
  });

  it('should show error when startDate is empty on save', () => {
    component.form = { title: 'Test', description: '', status: 'PLANIFIE', startDate: '', endDate: '', progress: 0 };
    component.savePath();
    expect(component.errorMessage).toBe('La date de début est obligatoire');
    expect(mockLearningPathService.create).not.toHaveBeenCalled();
  });

  it('should call create service in add mode', () => {
    mockLearningPathService.create.and.returnValue(of(mockPaths[0]));
    component.isEditMode = false;
    component.form = { title: 'New Path', description: '', status: 'PLANIFIE', startDate: '2026-01-01', endDate: '', progress: 0 };
    component.savePath();
    expect(mockLearningPathService.create).toHaveBeenCalled();
  });

  it('should call update service in edit mode', () => {
    mockLearningPathService.update.and.returnValue(of(mockPaths[0]));
    component.isEditMode = true;
    component.form = { pathId: 1, title: 'Updated', description: '', status: 'EN_COURS', startDate: '2026-01-01', endDate: '', progress: 50 };
    component.savePath();
    expect(mockLearningPathService.update).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  // ===== LOGIQUE MÉTIER : Quick progress update =====

  it('should not call update when progress unchanged', () => {
    // Set quickProgressMap to same value as path.progress to trigger early return
    const path = { ...mockPaths[0] };
    component.quickProgressMap[path.pathId] = path.progress;
    component.updateProgressQuick(path);
    expect(mockLearningPathService.update).not.toHaveBeenCalled();
  });

  it('should call update when progress changed', () => {
    mockLearningPathService.update.and.returnValue(of({ ...mockPaths[0], progress: 60 }));
    component.quickProgressMap[1] = 60;
    component.updateProgressQuick(mockPaths[0]);
    expect(mockLearningPathService.update).toHaveBeenCalled();
  });

  // ===== Delete =====

  it('should call delete and reload on confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockLearningPathService.delete.and.returnValue(of(undefined));
    component.deletePath(1);
    expect(mockLearningPathService.delete).toHaveBeenCalledWith(1);
  });

  it('should not delete when user cancels', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deletePath(1);
    expect(mockLearningPathService.delete).not.toHaveBeenCalled();
  });

  // ===== Gestion d'erreurs =====

  it('should set errorMessage on load failure', () => {
    mockLearningPathService.getByLearner.and.returnValue(throwError(() => new Error('Network error')));
    component.loadPaths();
    expect(component.errorMessage).toBe('Erreur lors du chargement');
    expect(component.isLoading).toBeFalse();
  });

  it('should set errorMessage on save failure', () => {
    mockLearningPathService.create.and.returnValue(throwError(() => ({ error: { message: 'Duplicate title' } })));
    component.isEditMode = false;
    component.form = { title: 'Test', description: '', status: 'PLANIFIE', startDate: '2026-01-01', endDate: '', progress: 0 };
    component.savePath();
    expect(component.errorMessage).toBe('Duplicate title');
  });

  it('should not load learning style when learner has none', () => {
    // Reset spy call count before this test
    mockLearningStyleService.exists.and.returnValue(of(false));
    mockLearningStyleService.getByLearner.calls.reset();
    component.loadLearningStyle();
    expect(mockLearningStyleService.getByLearner).not.toHaveBeenCalled();
  });
});
