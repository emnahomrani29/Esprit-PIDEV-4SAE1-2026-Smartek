import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LearnerExamsComponent } from './learner-exams.component';
import { ExamService } from '../../../core/services/exam.service';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Tests unitaires pour LearnerExamsComponent.
 * Vérifie le chargement des examens, le filtrage, le tri et la logique métier.
 */
describe('LearnerExamsComponent', () => {
  let component: LearnerExamsComponent;
  let fixture: ComponentFixture<LearnerExamsComponent>;
  let examServiceSpy: jasmine.SpyObj<ExamService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockExams = [
    {
      id: 1,
      examType: 'QUIZ',
      title: 'Quiz Spring Boot',
      description: 'Quiz sur les bases',
      duration: 60,
      passingScore: 70,
      totalMarks: 100,
      isActive: true,
      isLocked: false,
      hasAttempted: true,
      attemptsCount: 1,
      bestScore: 80,
      courseName: 'Spring Boot',
      trainingName: 'Formation Java'
    },
    {
      id: 2,
      examType: 'EXAM',
      title: 'Examen Final Java',
      description: 'Examen de fin de formation',
      duration: 120,
      passingScore: 60,
      totalMarks: 200,
      isActive: true,
      isLocked: true,
      hasAttempted: false,
      attemptsCount: 0,
      bestScore: null,
      courseName: 'Java Avancé',
      trainingName: 'Formation Java'
    }
  ];

  beforeEach(async () => {
    examServiceSpy = jasmine.createSpyObj('ExamService', ['getMyExams', 'startExam', 'retakeExam']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    examServiceSpy.getMyExams.and.returnValue(of(mockExams as any));
    authServiceSpy.getUserInfo.and.returnValue({
      userId: 5, email: 'learner@smartek.com', firstName: 'Sara',
      role: 'LEARNER', token: 'test-token', message: ''
    });

    await TestBed.configureTestingModule({
      imports: [
        LearnerExamsComponent,
        CommonModule,
        FormsModule
      ],
      providers: [
        { provide: ExamService, useValue: examServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LearnerExamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ─── Chargement des examens ───────────────────────────────────────────────
  describe('loadMyExams()', () => {
    it('should load exams on init', () => {
      expect(examServiceSpy.getMyExams).toHaveBeenCalledWith(5);
    });

    it('should separate quizzes and exams by type', () => {
      expect(component.quizzes.length).toBe(1);
      expect(component.exams.length).toBe(1);
      expect(component.quizzes[0].examType).toBe('QUIZ');
      expect(component.exams[0].examType).toBe('EXAM');
    });

    it('should set loading to false after loading', () => {
      expect(component.isLoading).toBeFalse();
    });

    it('should set error message when user is not logged in', () => {
      authServiceSpy.getUserInfo.and.returnValue(null);
      component.loadMyExams();
      expect(component.errorMessage).toBe('Utilisateur non connecté');
    });

    it('should handle error when loading fails', () => {
      examServiceSpy.getMyExams.and.returnValue(throwError(() => new Error('Network error')));
      component.loadMyExams();
      expect(component.errorMessage).toBe('Erreur lors du chargement des examens');
      expect(component.isLoading).toBeFalse();
    });
  });

  // ─── Calcul du score ──────────────────────────────────────────────────────
  describe('getScorePercentage()', () => {
    it('should calculate score percentage correctly', () => {
      const exam = mockExams[0] as any;
      const percentage = component.getScorePercentage(exam);
      expect(percentage).toBe(80); // 80/100 * 100
    });

    it('should return 0 when no score exists', () => {
      const exam = mockExams[1] as any;
      const percentage = component.getScorePercentage(exam);
      expect(percentage).toBe(0);
    });
  });

  describe('getScoreColor()', () => {
    it('should return green color when score >= passing score', () => {
      const exam = mockExams[0] as any; // 80% >= 70%
      expect(component.getScoreColor(exam)).toBe('text-green-600');
    });

    it('should return red color when score < passing score', () => {
      const failedExam = { ...mockExams[0], bestScore: 50, totalMarks: 100, passingScore: 70 } as any;
      expect(component.getScoreColor(failedExam)).toBe('text-red-600');
    });
  });

  // ─── Filtrage et tri ──────────────────────────────────────────────────────
  describe('applyFiltersAndSort()', () => {
    it('should filter quizzes by search term', () => {
      component.searchTerm = 'Spring';
      component.applyFiltersAndSort();
      expect(component.filteredQuizzes.length).toBe(1);
      expect(component.filteredQuizzes[0].title).toContain('Spring');
    });

    it('should return empty filtered list when search term matches nothing', () => {
      component.searchTerm = 'XYZ_INEXISTANT';
      component.applyFiltersAndSort();
      expect(component.filteredQuizzes.length).toBe(0);
      expect(component.filteredExams.length).toBe(0);
    });

    it('should sort quizzes by name ascending', () => {
      component.quizzes = [
        { ...mockExams[0], title: 'Z Quiz' } as any,
        { ...mockExams[0], id: 3, title: 'A Quiz' } as any
      ];
      component.sortBy = 'name';
      component.sortOrder = 'asc';
      component.applyFiltersAndSort();
      expect(component.filteredQuizzes[0].title).toBe('A Quiz');
    });

    it('should sort quizzes by name descending', () => {
      component.quizzes = [
        { ...mockExams[0], title: 'A Quiz' } as any,
        { ...mockExams[0], id: 3, title: 'Z Quiz' } as any
      ];
      component.sortBy = 'name';
      component.sortOrder = 'desc';
      component.applyFiltersAndSort();
      expect(component.filteredQuizzes[0].title).toBe('Z Quiz');
    });
  });

  describe('onSearchChange()', () => {
    it('should update searchTerm and apply filters', () => {
      component.onSearchChange('Java');
      expect(component.searchTerm).toBe('Java');
    });
  });

  describe('onSortChange()', () => {
    it('should toggle sort order when same field is selected', () => {
      component.sortBy = 'name';
      component.sortOrder = 'asc';
      component.onSortChange('name');
      expect(component.sortOrder).toBe('desc');
    });

    it('should reset to asc when new field is selected', () => {
      component.sortBy = 'name';
      component.sortOrder = 'desc';
      component.onSortChange('score');
      expect(component.sortBy).toBe('score');
      expect(component.sortOrder).toBe('asc');
    });
  });

  // ─── displayedQuizzes / displayedExams getters ────────────────────────────
  describe('displayedQuizzes getter', () => {
    it('should return all quizzes when no search term', () => {
      component.searchTerm = '';
      component.filteredQuizzes = [];
      expect(component.displayedQuizzes).toEqual(component.quizzes);
    });

    it('should return filtered quizzes when search term is set', () => {
      component.searchTerm = 'Spring';
      component.filteredQuizzes = [mockExams[0] as any];
      expect(component.displayedQuizzes).toEqual(component.filteredQuizzes);
    });
  });

  // ─── startExam - logique métier ───────────────────────────────────────────
  describe('startExam()', () => {
    it('should show alert when exam is locked', () => {
      spyOn(window, 'alert');
      const lockedExam = mockExams[1] as any;
      component.startExam(lockedExam);
      expect(window.alert).toHaveBeenCalled();
      expect(examServiceSpy.startExam).not.toHaveBeenCalled();
    });

    it('should call startExam service when exam is unlocked', () => {
      examServiceSpy.startExam.and.returnValue(of({}));
      const unlockedExam = mockExams[0] as any;
      component.startExam(unlockedExam);
      expect(examServiceSpy.startExam).toHaveBeenCalledWith(1, 5);
    });
  });

  // ─── retakeExam - logique métier ──────────────────────────────────────────
  describe('retakeExam()', () => {
    it('should show alert when max attempts reached (>= 2)', () => {
      spyOn(window, 'alert');
      const maxAttemptsExam = { ...mockExams[0], attemptsCount: 2 } as any;
      component.retakeExam(maxAttemptsExam);
      expect(window.alert).toHaveBeenCalled();
      expect(examServiceSpy.retakeExam).not.toHaveBeenCalled();
    });

    it('should call retakeExam service when confirmed and attempts < 2', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      examServiceSpy.retakeExam.and.returnValue(of({}));
      spyOn(window, 'alert');

      const exam = { ...mockExams[0], attemptsCount: 1 } as any;
      component.retakeExam(exam);

      expect(examServiceSpy.retakeExam).toHaveBeenCalledWith(1, 5);
    });

    it('should not call retakeExam when user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      const exam = { ...mockExams[0], attemptsCount: 1 } as any;
      component.retakeExam(exam);
      expect(examServiceSpy.retakeExam).not.toHaveBeenCalled();
    });
  });
});
