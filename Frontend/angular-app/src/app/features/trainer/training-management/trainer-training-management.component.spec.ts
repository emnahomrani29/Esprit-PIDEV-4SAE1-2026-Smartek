import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { TrainerTrainingManagementComponent } from './trainer-training-management.component';
import { TrainingService } from '../../../core/services/training.service';
import { CourseService } from '../../../core/services/course.service';
import { TrainingEnrollmentService } from '../../../core/services/training-enrollment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PermissionService } from '../../../core/services/permission.service';

describe('TrainerTrainingManagementComponent', () => {
  let component: TrainerTrainingManagementComponent;
  let fixture: ComponentFixture<TrainerTrainingManagementComponent>;
  let trainingServiceSpy: jasmine.SpyObj<TrainingService>;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;
  let enrollmentServiceSpy: jasmine.SpyObj<TrainingEnrollmentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let permissionServiceSpy: jasmine.SpyObj<PermissionService>;

  const mockTrainings = [
    {
      trainingId: 1, title: 'Formation Spring Boot', description: 'Microservices',
      category: 'Backend', level: 'Intermédiaire', duration: '2026-09-01',
      courseIds: [1, 2], courses: [], createdAt: '', updatedAt: ''
    }
  ];

  const mockCourses = [
    { courseId: 1, title: 'Spring Boot Avancé', trainerId: 1, deliveryMode: 'PRESENTIEL', chapters: [] }
  ];

  beforeEach(async () => {
    trainingServiceSpy = jasmine.createSpyObj('TrainingService', [
      'getAllTrainings', 'createTraining', 'updateTraining', 'deleteTraining',
      'addCourseToTraining', 'removeCourseFromTraining'
    ]);
    courseServiceSpy = jasmine.createSpyObj('CourseService', ['getAllCourses']);
    enrollmentServiceSpy = jasmine.createSpyObj('TrainingEnrollmentService', ['getUserEnrollments', 'enrollUser']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);
    permissionServiceSpy = jasmine.createSpyObj('PermissionService', ['hasRole']);

    trainingServiceSpy.getAllTrainings.and.returnValue(of(mockTrainings as any));
    courseServiceSpy.getAllCourses.and.returnValue(of(mockCourses as any));
    enrollmentServiceSpy.getUserEnrollments.and.returnValue(of([]));
    authServiceSpy.getUserInfo.and.returnValue({ userId: 1, email: 'trainer@test.com', role: 'TRAINER', token: 'tok', firstName: 'Trainer', message: '' });
    permissionServiceSpy.hasRole.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [
        TrainerTrainingManagementComponent,
        HttpClientTestingModule,
        ReactiveFormsModule,
        FormsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: TrainingService, useValue: trainingServiceSpy },
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: TrainingEnrollmentService, useValue: enrollmentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PermissionService, useValue: permissionServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TrainerTrainingManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load trainings on init', () => {
    expect(trainingServiceSpy.getAllTrainings).toHaveBeenCalled();
    expect(component.trainings.length).toBe(1);
  });

  it('should load courses on init', () => {
    expect(courseServiceSpy.getAllCourses).toHaveBeenCalled();
    expect(component.availableCourses.length).toBe(1);
  });

  it('should open create modal with empty form', () => {
    component.openCreateModal();
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeFalse();
    expect(component.selectedTrainingId).toBeNull();
  });

  it('should open edit modal with training data', () => {
    component.openEditModal(mockTrainings[0] as any);
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.selectedTrainingId).toBe(1);
    expect(component.trainingForm.value.title).toBe('Formation Spring Boot');
  });

  it('should close modal and reset form', () => {
    component.showModal = true;
    component.closeModal();
    expect(component.showModal).toBeFalse();
  });

  it('should toggle course selection', () => {
    component.toggleCourseSelection(1);
    expect(component.isCourseSelected(1)).toBeTrue();

    component.toggleCourseSelection(1);
    expect(component.isCourseSelected(1)).toBeFalse();
  });

  it('should return correct level color', () => {
    expect(component.getLevelColor('Débutant')).toContain('green');
    expect(component.getLevelColor('Intermédiaire')).toContain('yellow');
    expect(component.getLevelColor('Avancé')).toContain('red');
  });

  it('should return course title by ID', () => {
    expect(component.getCourseTitle(1)).toBe('Spring Boot Avancé');
    expect(component.getCourseTitle(99)).toBe('Cours #99');
  });

  it('should not submit if form is invalid', () => {
    component.trainingForm.reset();
    component.onSubmit();
    expect(trainingServiceSpy.createTraining).not.toHaveBeenCalled();
  });

  it('should call createTraining on valid form submit in create mode', () => {
    trainingServiceSpy.createTraining.and.returnValue(of(mockTrainings[0] as any));
    component.isEditMode = false;
    component.trainingForm.setValue({
      title: 'Nouvelle Formation',
      description: 'Description',
      category: 'Backend',
      level: 'Débutant',
      duration: '2026-12-01'
    });

    component.onSubmit();

    expect(trainingServiceSpy.createTraining).toHaveBeenCalled();
  });

  it('should call updateTraining on valid form submit in edit mode', () => {
    trainingServiceSpy.updateTraining.and.returnValue(of(mockTrainings[0] as any));
    component.isEditMode = true;
    component.selectedTrainingId = 1;
    component.trainingForm.setValue({
      title: 'Formation Mise à jour',
      description: 'Description',
      category: 'Backend',
      level: 'Avancé',
      duration: '2026-12-01'
    });

    component.onSubmit();

    expect(trainingServiceSpy.updateTraining).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  it('should call deleteTraining when confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    trainingServiceSpy.deleteTraining.and.returnValue(of(undefined));

    component.deleteTraining(1);

    expect(trainingServiceSpy.deleteTraining).toHaveBeenCalledWith(1);
  });

  it('should not delete if user cancels confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteTraining(1);

    expect(trainingServiceSpy.deleteTraining).not.toHaveBeenCalled();
  });

  it('should compute training stats correctly', () => {
    component.trainings = mockTrainings as any;
    const stats = component.getTrainingStats();
    expect(stats[0].value).toBe(1); // Total
  });

  it('isEnrolled should return false when not enrolled', () => {
    component.enrolledTrainings = [];
    expect(component.isEnrolled(1)).toBeFalse();
  });
});
