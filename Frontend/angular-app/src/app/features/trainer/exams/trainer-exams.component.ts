import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ExamService } from '../../../core/services/exam.service';
import { CourseService } from '../../../core/services/course.service';
import { TrainingService } from '../../../core/services/training.service';
import { AuthService } from '../../../core/services/auth.service';
import { Exam, Question, QuestionOption } from '../../../core/models/exam.model';
import { Course } from '../../../core/models/course.model';
import { Training } from '../../../core/models/training.model';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-trainer-exams',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, PageHeaderComponent],
  templateUrl: './trainer-exams.component.html',
  styleUrl: './trainer-exams.component.scss'
})
export class TrainerExamsComponent implements OnInit {
  exams: Exam[] = [];
  filteredExams: Exam[] = [];
  courses: Course[] = [];
  trainings: Training[] = [];
  examForm: FormGroup;
  isEditMode = false;
  selectedExamId: number | null = null;
  showModal = false;
  loading = false;
  currentStep: 'info' | 'questions' = 'info';

  // Recherche et tri
  searchTerm: string = '';
  sortBy: 'name' | 'type' | 'questions' = 'name';
  sortOrder: 'asc' | 'desc' = 'asc';

  constructor(
    private fb: FormBuilder,
    private examService: ExamService,
    private courseService: CourseService,
    private trainingService: TrainingService,
    private authService: AuthService
  ) {
    this.examForm = this.fb.group({
      courseId: [null],
      trainingId: [null],
      examType: ['QUIZ', Validators.required],
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      duration: ['', [Validators.required, Validators.min(1)]],
      passingScore: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
      totalMarks: [''],
      startDate: [''],
      endDate: [''],
      isActive: [true],
      questions: this.fb.array([])
    });

    // Écouter les changements du type d'examen
    this.examForm.get('examType')?.valueChanges.subscribe(type => {
      this.onExamTypeChange(type);
    });
  }

  ngOnInit(): void {
    this.loadExams();
    this.loadCourses();
    this.loadTrainings();
  }

  getExamStats() {
    return [
      { label: 'Total Exams', value: this.exams.length },
      { label: 'Questions', value: this.exams.reduce((sum, e) => sum + (e.questions?.length || 0), 0) }
    ];
  }

  onExamTypeChange(type: string): void {
    if (type === 'QUIZ') {
      // Pour QUIZ: courseId requis, trainingId null
      this.examForm.get('courseId')?.setValidators([Validators.required]);
      this.examForm.get('trainingId')?.clearValidators();
      this.examForm.patchValue({ trainingId: null });
    } else {
      // Pour EXAM: trainingId requis, courseId null
      this.examForm.get('trainingId')?.setValidators([Validators.required]);
      this.examForm.get('courseId')?.clearValidators();
      this.examForm.patchValue({ courseId: null });
    }
    this.examForm.get('courseId')?.updateValueAndValidity();
    this.examForm.get('trainingId')?.updateValueAndValidity();
  }

  loadExams(): void {
    this.loading = true;
    this.examService.getAllExams().subscribe({
      next: (data) => {
        this.exams = data;
        this.loading = false;
        this.applyFiltersAndSort();
      },
      error: (error) => {
        console.error('Error loading exams:', error);
        this.loading = false;
      }
    });
  }

  // Recherche et tri
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.applyFiltersAndSort();
  }

  onSortChange(sortBy: 'name' | 'type' | 'questions'): void {
    if (this.sortBy === sortBy) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortOrder = 'asc';
    }
    this.applyFiltersAndSort();
  }

  applyFiltersAndSort(): void {
    // Filtrer par recherche
    let filtered = this.exams.filter(e => 
      e.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (e.description && e.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );

    // Trier
    filtered.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'type':
          comparison = a.examType.localeCompare(b.examType);
          break;
        case 'questions':
          const questionsA = a.questions?.length || 0;
          const questionsB = b.questions?.length || 0;
          comparison = questionsA - questionsB;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredExams = filtered;
  }

  get displayedExams(): Exam[] {
    return this.filteredExams.length > 0 || this.searchTerm ? this.filteredExams : this.exams;
  }

  loadCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (data) => {
        this.courses = data;
      },
      error: (error) => {
        console.error('Error loading courses:', error);
      }
    });
  }

  loadTrainings(): void {
    this.trainingService.getAllTrainings().subscribe({
      next: (data) => {
        this.trainings = data;
      },
      error: (error) => {
        console.error('Error loading trainings:', error);
      }
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedExamId = null;
    this.currentStep = 'info';
    this.examForm.reset({ isActive: true, examType: 'QUIZ' });
    this.questions.clear();
    this.onExamTypeChange('QUIZ'); // Initialiser les validateurs
    this.showModal = true;
  }

  openEditModal(exam: any): void {
    this.isEditMode = true;
    this.selectedExamId = exam.id;
    this.currentStep = 'info';
    this.examForm.patchValue(exam);
    this.questions.clear();
    
    // Charger les questions si l'examen en a
    if (exam.id) {
      this.examService.getExamById(exam.id).subscribe({
        next: (examData) => {
          if (examData.questions && examData.questions.length > 0) {
            examData.questions.forEach(q => {
              const questionGroup = this.createQuestionGroup();
              questionGroup.patchValue(q);
              
              // Ajouter les options
              const optionsArray = questionGroup.get('options') as FormArray;
              optionsArray.clear();
              if (q.options && q.options.length > 0) {
                q.options.forEach(opt => {
                  const optionGroup = this.createOptionGroup();
                  optionGroup.patchValue(opt);
                  optionsArray.push(optionGroup);
                });
              }
              
              this.questions.push(questionGroup);
            });
          }
        }
      });
    }
    
    this.onExamTypeChange(exam.examType); // Mettre à jour les validateurs
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentStep = 'info';
    this.examForm.reset();
    this.questions.clear();
  }

  onSubmit(): void {
    if (this.examForm.invalid) return;

    this.loading = true;
    const formValue = this.examForm.value;
    
    // Récupérer l'ID du trainer connecté
    const userInfo = this.authService.getUserInfo();
    const trainerId = userInfo?.userId;
    
    // Préparer les données pour le backend (ExamRequest)
    const examData: any = {
      courseId: formValue.courseId,
      trainingId: formValue.trainingId,
      examType: formValue.examType,
      title: formValue.title,
      description: formValue.description,
      duration: formValue.duration,
      passingScore: formValue.passingScore,
      totalMarks: this.getTotalMarks() || formValue.totalMarks,
      startDate: formValue.startDate,
      endDate: formValue.endDate,
      isActive: formValue.isActive,
      questions: formValue.questions,
      createdBy: trainerId // Ajouter l'ID du trainer
    };

    const operation = this.isEditMode && this.selectedExamId
      ? this.examService.updateExam(this.selectedExamId, examData)
      : this.examService.createExam(examData);

    operation.subscribe({
      next: () => {
        this.loadExams();
        this.closeModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error saving exam:', error);
        this.loading = false;
      }
    });
  }

  deleteExam(id: number | undefined): void {
    if (!id) return;
    
    if (confirm('Êtes-vous sûr de vouloir supprimer cet examen?')) {
      this.loading = true;
      this.examService.deleteExam(id).subscribe({
        next: () => {
          this.loadExams();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error deleting exam:', error);
          this.loading = false;
        }
      });
    }
  }

  getCourseName(courseId: number | undefined): string {
    if (!courseId) return 'Non défini';
    const course = this.courses.find(c => c.courseId === courseId);
    return course ? course.title : `Cours #${courseId}`;
  }

  getTrainingName(trainingId: number | undefined): string {
    if (!trainingId) return 'Non défini';
    const training = this.trainings.find(t => t.trainingId === trainingId);
    return training ? training.title : `Formation #${trainingId}`;
  }

  getExamTypeLabel(type: string): string {
    return type === 'QUIZ' ? 'Quiz (Cours)' : 'Examen (Formation)';
  }

  getExamAssociation(exam: Exam): string {
    if (exam.examType === 'QUIZ' && exam.courseId) {
      return this.getCourseName(exam.courseId);
    } else if (exam.examType === 'EXAM' && exam.trainingId) {
      return this.getTrainingName(exam.trainingId);
    }
    return 'Non associé';
  }

  // Gestion des questions
  get questions(): FormArray {
    return this.examForm.get('questions') as FormArray;
  }

  createQuestionGroup(): FormGroup {
    return this.fb.group({
      id: [null],
      questionText: ['', Validators.required],
      questionType: ['MULTIPLE_CHOICE', Validators.required],
      marks: [1, [Validators.required, Validators.min(1)]],
      correctAnswer: [''],
      options: this.fb.array([])
    });
  }

  createOptionGroup(): FormGroup {
    return this.fb.group({
      id: [null],
      optionText: ['', Validators.required],
      isCorrect: [false]
    });
  }

  addQuestion(): void {
    const questionGroup = this.createQuestionGroup();
    
    // Si c'est un EXAM, forcer le type SHORT_ANSWER
    if (this.examForm.get('examType')?.value === 'EXAM') {
      questionGroup.patchValue({ questionType: 'SHORT_ANSWER' });
    } else {
      // Pour les QUIZ, ajouter 4 options par défaut pour les questions à choix multiples
      const optionsArray = questionGroup.get('options') as FormArray;
      for (let i = 0; i < 4; i++) {
        optionsArray.push(this.createOptionGroup());
      }
    }
    
    this.questions.push(questionGroup);
  }

  removeQuestion(index: number): void {
    this.questions.removeAt(index);
  }

  getQuestionOptions(questionIndex: number): FormArray {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  addOption(questionIndex: number): void {
    const options = this.getQuestionOptions(questionIndex);
    options.push(this.createOptionGroup());
  }

  removeOption(questionIndex: number, optionIndex: number): void {
    const options = this.getQuestionOptions(questionIndex);
    options.removeAt(optionIndex);
  }

  onQuestionTypeChange(questionIndex: number, type: string): void {
    const question = this.questions.at(questionIndex);
    const optionsArray = question.get('options') as FormArray;
    
    if (type === 'TRUE_FALSE') {
      // Pour vrai/faux, on a besoin de 2 options
      optionsArray.clear();
      const trueOption = this.createOptionGroup();
      trueOption.patchValue({ optionText: 'Vrai', isCorrect: false });
      const falseOption = this.createOptionGroup();
      falseOption.patchValue({ optionText: 'Faux', isCorrect: false });
      optionsArray.push(trueOption);
      optionsArray.push(falseOption);
    } else if (type === 'SHORT_ANSWER') {
      // Pour réponse courte, pas besoin d'options
      optionsArray.clear();
    } else if (type === 'MULTIPLE_CHOICE' && optionsArray.length === 0) {
      // Pour choix multiples, ajouter 4 options par défaut
      for (let i = 0; i < 4; i++) {
        optionsArray.push(this.createOptionGroup());
      }
    }
  }

  getTotalMarks(): number {
    return this.questions.controls.reduce((total, question) => {
      return total + (question.get('marks')?.value || 0);
    }, 0);
  }

  nextStep(): void {
    if (this.currentStep === 'info') {
      // Valider les informations de base
      const infoControls = ['courseId', 'trainingId', 'examType', 'title', 'duration', 'passingScore'];
      let isValid = true;
      
      infoControls.forEach(controlName => {
        const control = this.examForm.get(controlName);
        if (control && control.invalid) {
          control.markAsTouched();
          isValid = false;
        }
      });
      
      if (isValid) {
        this.currentStep = 'questions';
      }
    }
  }

  previousStep(): void {
    if (this.currentStep === 'questions') {
      this.currentStep = 'info';
    }
  }
}
