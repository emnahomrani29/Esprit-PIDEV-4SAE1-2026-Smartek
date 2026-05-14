import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TrainingService } from '../../../core/services/training.service';
import { CourseService } from '../../../core/services/course.service';
import { TrainingEnrollmentService } from '../../../core/services/training-enrollment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PermissionService } from '../../../core/services/permission.service';
import { Training, TrainingCreateRequest } from '../../../core/models/training.model';
import { Course } from '../../../core/models/course.model';
import { Role } from '../../../core/enums/role.enum';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-trainer-training-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink, PageHeaderComponent],
  templateUrl: './trainer-training-management.component.html',
  styleUrl: './trainer-training-management.component.scss'
})
export class TrainerTrainingManagementComponent implements OnInit, OnDestroy {
  trainings: Training[] = [];
  availableCourses: Course[] = [];
  trainingForm: FormGroup;
  isEditMode = false;
  selectedTrainingId: number | null = null;
  showModal = false;
  showCourseModal = false;
  loading = false;
  selectedCourseIds: number[] = [];
  currentTrainingForCourses: Training | null = null;
  isLearner = false;
  enrolledTrainings: Training[] = [];
  showEnrolledDropdown = false;

  categories = ['Développement', 'Design', 'Marketing', 'Management', 'Data Science', 'DevOps'];
  levels = ['Débutant', 'Intermédiaire', 'Avancé'];

  constructor(
    private fb: FormBuilder,
    private trainingService: TrainingService,
    private courseService: CourseService,
    private enrollmentService: TrainingEnrollmentService,
    private authService: AuthService,
    private permissionService: PermissionService
  ) {
    this.trainingForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      category: ['', Validators.required],
      level: ['', Validators.required],
      duration: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.isLearner = this.permissionService.hasRole(Role.LEARNER);
    this.loadTrainings();
    this.loadCourses();
    this.loadEnrolledTrainings();
    
    // Fermer le dropdown quand on clique en dehors
    document.addEventListener('click', this.closeDropdownOnClickOutside.bind(this));
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.closeDropdownOnClickOutside.bind(this));
  }

  getTrainingStats() {
    return [
      { label: 'Total Trainings', value: this.trainings.length },
      { label: 'Associated Courses', value: this.trainings.reduce((sum, t) => sum + (t.courses?.length || 0), 0) }
    ];
  }

  closeDropdownOnClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.relative') && this.showEnrolledDropdown) {
      this.showEnrolledDropdown = false;
    }
  }

  loadTrainings(): void {
    this.loading = true;
    this.trainingService.getAllTrainings().subscribe({
      next: (trainings) => {
        this.trainings = trainings;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading trainings:', error);
        this.loading = false;
      }
    });
  }

  loadCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        this.availableCourses = courses;
      },
      error: (error) => {
        console.error('Error loading courses:', error);
      }
    });
  }

  loadEnrolledTrainings(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    this.enrollmentService.getUserEnrollments(currentUser.userId).subscribe({
      next: (enrollments) => {
        // Extraire les IDs des formations inscrites
        const enrolledTrainingIds = enrollments.map(e => e.trainingId);
        
        // Filtrer les formations pour obtenir celles auxquelles l'utilisateur est inscrit
        this.trainingService.getAllTrainings().subscribe({
          next: (allTrainings) => {
            this.enrolledTrainings = allTrainings.filter(t => 
              enrolledTrainingIds.includes(t.trainingId!)
            );
          },
          error: (error) => {
            console.error('Error loading enrolled trainings:', error);
          }
        });
      },
      error: (error) => {
        console.error('Error loading enrollments:', error);
      }
    });
  }

  toggleEnrolledDropdown(): void {
    this.showEnrolledDropdown = !this.showEnrolledDropdown;
  }

  isEnrolled(trainingId: number | undefined): boolean {
    if (!trainingId) return false;
    return this.enrolledTrainings.some(t => t.trainingId === trainingId);
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedTrainingId = null;
    this.selectedCourseIds = [];
    this.trainingForm.reset();
    this.showModal = true;
  }

  openEditModal(training: Training): void {
    this.isEditMode = true;
    this.selectedTrainingId = training.trainingId || null;
    this.selectedCourseIds = training.courseIds || [];
    this.trainingForm.patchValue({
      title: training.title,
      description: training.description,
      category: training.category,
      level: training.level,
      duration: training.duration
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.trainingForm.reset();
    this.selectedCourseIds = [];
  }

  openCourseSelectionModal(training: Training): void {
    this.currentTrainingForCourses = training;
    this.selectedCourseIds = [...(training.courseIds || [])];
    this.showCourseModal = true;
  }

  openCourseSelectionFromForm(): void {
    this.currentTrainingForCourses = null;
    this.showCourseModal = true;
  }

  closeCourseModal(): void {
    this.showCourseModal = false;
    if (!this.currentTrainingForCourses) {
      // Si on est dans le formulaire, on ne réinitialise pas selectedCourseIds
      // car on veut garder la sélection pour la création
    } else {
      this.currentTrainingForCourses = null;
    }
  }

  toggleCourseSelection(courseId: number): void {
    const index = this.selectedCourseIds.indexOf(courseId);
    if (index > -1) {
      this.selectedCourseIds.splice(index, 1);
    } else {
      this.selectedCourseIds.push(courseId);
    }
  }

  isCourseSelected(courseId: number): boolean {
    return this.selectedCourseIds.includes(courseId);
  }

  saveCourseSelection(): void {
    if (this.currentTrainingForCourses?.trainingId) {
      // Mode édition d'une formation existante
      const trainingId = this.currentTrainingForCourses.trainingId;
      const currentCourseIds = this.currentTrainingForCourses.courseIds || [];
      
      // Cours à ajouter
      const toAdd = this.selectedCourseIds.filter(id => !currentCourseIds.includes(id));
      // Cours à retirer
      const toRemove = currentCourseIds.filter(id => !this.selectedCourseIds.includes(id));

      this.loading = true;

      // Ajouter les nouveaux cours
      const addPromises = toAdd.map(courseId => 
        this.trainingService.addCourseToTraining(trainingId, courseId).toPromise()
      );

      // Retirer les cours désélectionnés
      const removePromises = toRemove.map(courseId => 
        this.trainingService.removeCourseFromTraining(trainingId, courseId).toPromise()
      );

      Promise.all([...addPromises, ...removePromises])
        .then(() => {
          this.loadTrainings();
          this.closeCourseModal();
          this.loading = false;
          alert('Cours mis à jour avec succès!');
        })
        .catch((error) => {
          console.error('Error updating courses:', error);
          this.loading = false;
          alert('Erreur lors de la mise à jour des cours');
        });
    } else {
      // Mode création - on ferme juste le modal, les cours seront sauvegardés avec la formation
      this.closeCourseModal();
    }
  }

  onSubmit(): void {
    if (this.trainingForm.invalid) return;

    this.loading = true;
    
    // Récupérer l'ID du trainer connecté
    const userInfo = this.authService.getUserInfo();
    const trainerId = userInfo?.userId;
    
    const trainingData: TrainingCreateRequest = {
      ...this.trainingForm.value,
      courseIds: this.selectedCourseIds,
      createdBy: trainerId // Ajouter l'ID du trainer
    };

    if (this.isEditMode && this.selectedTrainingId) {
      this.trainingService.updateTraining(this.selectedTrainingId, trainingData).subscribe({
        next: () => {
          this.loadTrainings();
          this.closeModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error updating training:', error);
          this.loading = false;
        }
      });
    } else {
      this.trainingService.createTraining(trainingData).subscribe({
        next: () => {
          this.loadTrainings();
          this.closeModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error creating training:', error);
          this.loading = false;
        }
      });
    }
  }

  deleteTraining(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette formation?')) {
      this.loading = true;
      this.trainingService.deleteTraining(id).subscribe({
        next: () => {
          this.loadTrainings();
          this.loading = false;
          alert('Formation supprimée avec succès');
        },
        error: (error) => {
          console.error('Error deleting training:', error);
          this.loading = false;
          if (error.status === 404) {
            alert('Cette formation n\'existe plus. Actualisation de la liste...');
            this.loadTrainings(); // Recharger pour synchroniser
          } else {
            alert('Erreur lors de la suppression de la formation');
          }
        }
      });
    }
  }

  getLevelColor(level: string): string {
    switch (level) {
      case 'Débutant': return 'bg-green-100 text-green-800';
      case 'Intermédiaire': return 'bg-yellow-100 text-yellow-800';
      case 'Avancé': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getCourseTitle(courseId: number): string {
    const course = this.availableCourses.find(c => c.courseId === courseId);
    return course ? course.title : `Cours #${courseId}`;
  }

  enrollTraining(trainingId: number | undefined): void {
    if (!trainingId) return;
    
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) {
      alert('Utilisateur non connecté');
      return;
    }

    this.loading = true;
    this.enrollmentService.enrollUser({
      trainingId: trainingId,
      userId: currentUser.userId
    }).subscribe({
      next: () => {
        alert('Inscription réussie!');
        this.loadEnrolledTrainings(); // Recharger les inscriptions
        this.loading = false;
      },
      error: (error) => {
        console.error('Error enrolling:', error);
        alert('Erreur lors de l\'inscription. Vous êtes peut-être déjà inscrit.');
        this.loading = false;
      }
    });
  }
}
