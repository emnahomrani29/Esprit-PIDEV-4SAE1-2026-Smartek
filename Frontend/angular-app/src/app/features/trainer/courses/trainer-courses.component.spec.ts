import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA, ChangeDetectorRef } from '@angular/core';
import { TrainerCoursesComponent } from './trainer-courses.component';
import { CourseService } from '../../../core/services/course.service';
import { ChapterService } from '../../../core/services/chapter.service';
import { LiveSessionService } from '../../../core/services/live-session.service';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Tests unitaires pour TrainerCoursesComponent.
 * Vérifie le chargement des cours, le filtrage, le tri et les opérations CRUD.
 */
describe('TrainerCoursesComponent', () => {
  let component: TrainerCoursesComponent;
  let fixture: ComponentFixture<TrainerCoursesComponent>;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;
  let chapterServiceSpy: jasmine.SpyObj<ChapterService>;
  let liveSessionServiceSpy: jasmine.SpyObj<LiveSessionService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockCourses = [
    {
      courseId: 1,
      title: 'Spring Boot Avancé',
      content: 'Contenu du cours Spring Boot',
      duration: '2025-12-31',
      trainerId: 10,
      deliveryMode: 'PRESENTIEL',
      chapters: [
        { chapterId: 1, title: 'Introduction', orderIndex: 1 },
        { chapterId: 2, title: 'Configuration', orderIndex: 2 }
      ],
      liveSessions: []
    },
    {
      courseId: 2,
      title: 'Angular Avancé',
      content: 'Contenu du cours Angular',
      duration: '2025-11-30',
      trainerId: 10,
      deliveryMode: 'EN_LIGNE',
      chapters: [],
      liveSessions: []
    }
  ];

  beforeEach(async () => {
    courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getCoursesByTrainer', 'createCourse', 'updateCourse', 'deleteCourse'
    ]);
    chapterServiceSpy = jasmine.createSpyObj('ChapterService', [
      'getChaptersByCourse', 'createChapter', 'deleteChapter', 'uploadPdf'
    ]);
    liveSessionServiceSpy = jasmine.createSpyObj('LiveSessionService', [
      'getSessionsByCourseId', 'createSession', 'updateSession', 'deleteSession',
      'updateSessionStatus', 'getSessionDuration'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    courseServiceSpy.getCoursesByTrainer.and.returnValue(of(mockCourses as any));
    authServiceSpy.getUserInfo.and.returnValue({
      userId: 10, email: 'trainer@smartek.com', firstName: 'Ahmed',
      role: 'TRAINER', token: 'test-token', message: ''
    });

    await TestBed.configureTestingModule({
      imports: [
        TrainerCoursesComponent,
        CommonModule,
        ReactiveFormsModule,
        FormsModule
      ],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: ChapterService, useValue: chapterServiceSpy },
        { provide: LiveSessionService, useValue: liveSessionServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        ChangeDetectorRef,
        provideRouter([])
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainerCoursesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ─── Chargement des cours ─────────────────────────────────────────────────
  describe('loadCourses()', () => {
    it('should load courses for the current trainer on init', () => {
      expect(courseServiceSpy.getCoursesByTrainer).toHaveBeenCalledWith(10);
      expect(component.courses.length).toBe(2);
    });

    it('should set loading to false after loading', () => {
      expect(component.loading).toBeFalse();
    });

    it('should handle error when loading courses fails', () => {
      courseServiceSpy.getCoursesByTrainer.and.returnValue(throwError(() => new Error('Network error')));
      component.loadCourses();
      expect(component.loading).toBeFalse();
    });
  });

  // ─── Statistiques ─────────────────────────────────────────────────────────
  describe('getCourseStats()', () => {
    it('should return correct total courses count', () => {
      const stats = component.getCourseStats();
      expect(stats[0].value).toBe(2);
    });

    it('should return correct total chapters count', () => {
      const stats = component.getCourseStats();
      expect(stats[1].value).toBe(2); // 2 chapitres dans le premier cours
    });
  });

  // ─── Filtrage et tri ──────────────────────────────────────────────────────
  describe('applyFiltersAndSort()', () => {
    it('should filter courses by title', () => {
      component.searchTerm = 'Spring';
      component.applyFiltersAndSort();
      expect(component.filteredCourses.length).toBe(1);
      expect(component.filteredCourses[0].title).toContain('Spring');
    });

    it('should filter courses by content', () => {
      component.searchTerm = 'Angular';
      component.applyFiltersAndSort();
      expect(component.filteredCourses.length).toBe(1);
    });

    it('should return empty list when search matches nothing', () => {
      component.searchTerm = 'XYZ_INEXISTANT';
      component.applyFiltersAndSort();
      expect(component.filteredCourses.length).toBe(0);
    });

    it('should sort courses by name ascending', () => {
      component.sortBy = 'name';
      component.sortOrder = 'asc';
      component.applyFiltersAndSort();
      expect(component.filteredCourses[0].title).toBe('Angular Avancé');
    });

    it('should sort courses by name descending', () => {
      component.sortBy = 'name';
      component.sortOrder = 'desc';
      component.applyFiltersAndSort();
      expect(component.filteredCourses[0].title).toBe('Spring Boot Avancé');
    });

    it('should sort courses by chapters count', () => {
      component.sortBy = 'chapters';
      component.sortOrder = 'desc';
      component.applyFiltersAndSort();
      expect(component.filteredCourses[0].chapters?.length).toBe(2);
    });
  });

  describe('onSearchChange()', () => {
    it('should update searchTerm and apply filters', () => {
      component.onSearchChange('Angular');
      expect(component.searchTerm).toBe('Angular');
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
      component.onSortChange('chapters');
      expect(component.sortBy).toBe('chapters');
      expect(component.sortOrder).toBe('asc');
    });
  });

  // ─── displayedCourses getter ──────────────────────────────────────────────
  describe('displayedCourses getter', () => {
    it('should return all courses when no search term', () => {
      component.searchTerm = '';
      component.filteredCourses = [];
      expect(component.displayedCourses).toEqual(component.courses);
    });

    it('should return filtered courses when search term is set', () => {
      component.searchTerm = 'Spring';
      component.filteredCourses = [mockCourses[0] as any];
      expect(component.displayedCourses).toEqual(component.filteredCourses);
    });
  });

  // ─── Modal CRUD ───────────────────────────────────────────────────────────
  describe('Modal management', () => {
    it('should open create modal with empty form', () => {
      component.openCreateModal();
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeFalse();
      expect(component.selectedCourseId).toBeNull();
    });

    it('should open edit modal with course data', () => {
      component.openEditModal(mockCourses[0] as any);
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeTrue();
      expect(component.selectedCourseId).toBe(1);
      expect(component.courseForm.value.title).toBe('Spring Boot Avancé');
    });

    it('should close modal and reset form', () => {
      component.openCreateModal();
      component.closeModal();
      expect(component.showModal).toBeFalse();
    });
  });

  // ─── Suppression de cours ─────────────────────────────────────────────────
  describe('deleteCourse()', () => {
    it('should call courseService.deleteCourse when confirmed', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      courseServiceSpy.deleteCourse.and.returnValue(of(undefined));

      component.deleteCourse(1);

      expect(courseServiceSpy.deleteCourse).toHaveBeenCalledWith(1);
    });

    it('should not call courseService.deleteCourse when cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.deleteCourse(1);

      expect(courseServiceSpy.deleteCourse).not.toHaveBeenCalled();
    });
  });

  // ─── Gestion des chapitres ────────────────────────────────────────────────
  describe('Chapter management', () => {
    it('should add a chapter to the list', () => {
      component.openChapterModal();
      component.chapterForm.patchValue({
        title: 'Nouveau Chapitre',
        description: 'Description',
        orderIndex: 1
      });
      component.saveChapter();
      expect(component.chapters.length).toBe(1);
      expect(component.chapters[0].title).toBe('Nouveau Chapitre');
    });

    it('should remove a chapter from the list', () => {
      component.chapters = [
        { title: 'Chapitre 1', description: '', orderIndex: 1, pdfFile: null }
      ];
      spyOn(window, 'confirm').and.returnValue(true);
      component.removeChapter(0);
      expect(component.chapters.length).toBe(0);
    });

    it('should not remove chapter when cancelled', () => {
      component.chapters = [
        { title: 'Chapitre 1', description: '', orderIndex: 1, pdfFile: null }
      ];
      spyOn(window, 'confirm').and.returnValue(false);
      component.removeChapter(0);
      expect(component.chapters.length).toBe(1);
    });
  });
});
