import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TrainingService } from '../../../core/services/training.service';
import { TrainingEnrollmentService } from '../../../core/services/training-enrollment.service';
import { AuthService } from '../../../core/services/auth.service';
import { ExamService } from '../../../core/services/exam.service';
import { Training, CourseInfo } from '../../../core/models/training.model';
import { SafePipe } from '../../../core/pipes/safe.pipe';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule, SafePipe],
  templateUrl: './my-courses.component.html',
  styleUrl: './my-courses.component.scss'
})
export class MyCoursesComponent implements OnInit {
  training: Training | null = null;
  courses: CourseInfo[] = [];
  completedCourses: Set<number> = new Set();
  isLoading = false;
  trainingId: number | null = null;
  progress = 0;
  showPdfModal = false;
  selectedCourse: CourseInfo | null = null;
  pdfUrl: string | null = null;
  courseQuizzes: Map<number, any> = new Map(); // Map courseId -> quiz info

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trainingService: TrainingService,
    private enrollmentService: TrainingEnrollmentService,
    private authService: AuthService,
    private examService: ExamService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.trainingId = params['trainingId'] ? +params['trainingId'] : null;
      if (this.trainingId) {
        this.loadTrainingCourses();
      }
    });
  }

  loadTrainingCourses(): void {
    if (!this.trainingId) return;

    this.isLoading = true;
    this.trainingService.getTrainingById(this.trainingId).subscribe({
      next: (training) => {
        this.training = training;
        this.courses = training.courses || [];
        this.loadProgress();
        this.loadQuizzesForCourses();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading training:', error);
        this.isLoading = false;
      }
    });
  }

  loadQuizzesForCourses(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    // Charger tous les quiz du learner
    this.examService.getMyExams(currentUser.userId).subscribe({
      next: (exams) => {
        // Filtrer les quiz (pas les examens) et les associer aux cours
        const quizzes = exams.filter(exam => exam.examType === 'QUIZ');
        
        quizzes.forEach(quiz => {
          if (quiz.courseId) {
            this.courseQuizzes.set(quiz.courseId, {
              id: quiz.id,
              title: quiz.title,
              hasAttempted: quiz.hasAttempted,
              bestScore: quiz.bestScore,
              totalMarks: quiz.totalMarks,
              passingScore: quiz.passingScore,
              isLocked: quiz.isLocked
            });
          }
        });
      },
      error: (error) => {
        console.error('Error loading quizzes:', error);
      }
    });
  }

  loadProgress(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !this.trainingId) return;

    this.enrollmentService.getUserEnrollments(currentUser.userId).subscribe({
      next: (enrollments) => {
        const enrollment = enrollments.find(e => e.trainingId === this.trainingId);
        if (enrollment) {
          this.progress = enrollment.progress;
          // Charger les cours complétés depuis le localStorage ou backend
          this.loadCompletedCourses();
        }
      },
      error: (error) => {
        console.error('Error loading progress:', error);
      }
    });
  }

  loadCompletedCourses(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !this.trainingId) return;

    const key = `completed_courses_${currentUser.userId}_${this.trainingId}`;
    const saved = localStorage.getItem(key);
    if (saved) {
      const savedCourseIds = JSON.parse(saved);
      // Filtrer pour ne garder que les cours qui sont toujours dans la formation
      const validCourseIds = savedCourseIds.filter((id: number) => 
        this.courses.some(course => course.courseId === id)
      );
      this.completedCourses = new Set(validCourseIds);
      
      // Si des cours ont été retirés, sauvegarder la liste nettoyée et recalculer la progression
      if (validCourseIds.length !== savedCourseIds.length) {
        this.saveCompletedCourses();
        this.updateProgress();
      }
    }
  }

  saveCompletedCourses(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !this.trainingId) return;

    const key = `completed_courses_${currentUser.userId}_${this.trainingId}`;
    localStorage.setItem(key, JSON.stringify(Array.from(this.completedCourses)));
  }

  isCourseCompleted(courseId: number | undefined): boolean {
    return courseId ? this.completedCourses.has(courseId) : false;
  }

  toggleCourseCompletion(courseId: number | undefined): void {
    if (!courseId) return;

    if (this.completedCourses.has(courseId)) {
      this.completedCourses.delete(courseId);
    } else {
      this.completedCourses.add(courseId);
      // Déverrouiller l'examen associé au cours
      this.unlockExamForCourse(courseId);
    }

    this.saveCompletedCourses();
    this.updateProgress();
  }

  unlockExamForCourse(courseId: number): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    this.examService.completeCourse(courseId, currentUser.userId).subscribe({
      next: (response) => {
        console.log('Examen déverrouillé:', response);
      },
      error: (error) => {
        console.error('Erreur lors du déverrouillage de l\'examen:', error);
      }
    });
  }

  updateProgress(): void {
    if (this.courses.length === 0) return;

    const newProgress = Math.round((this.completedCourses.size / this.courses.length) * 100);
    this.progress = newProgress;

    // Mettre à jour la progression dans le backend
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !this.trainingId) return;

    this.enrollmentService.updateProgress(currentUser.userId, this.trainingId, newProgress).subscribe({
      next: () => {
        console.log('Progress updated successfully');
      },
      error: (error: any) => {
        console.error('Error updating progress:', error);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/my-training']);
  }

  openCoursePdf(course: CourseInfo): void {
    this.selectedCourse = course;
    
    // Si le cours a des chapitres avec des PDFs, ouvrir le premier
    if (course.chapters && course.chapters.length > 0) {
      const firstChapterWithPdf = course.chapters.find(ch => ch.pdfFilePath);
      if (firstChapterWithPdf) {
        // Utiliser l'URL avec le préfixe /api
        this.pdfUrl = `http://localhost:8082/api/courses/${course.courseId}/chapters/${firstChapterWithPdf.chapterId}/pdf`;
        this.showPdfModal = true;
        // Empêcher le scroll du body
        document.body.style.overflow = 'hidden';
      } else {
        alert('Aucun PDF disponible pour ce cours');
      }
    } else {
      alert('Ce cours n\'a pas encore de contenu PDF');
    }
  }

  closePdfModal(): void {
    this.showPdfModal = false;
    this.pdfUrl = null;
    this.selectedCourse = null;
    // Réactiver le scroll du body
    document.body.style.overflow = 'auto';
  }

  // Quiz related methods
  getCourseQuiz(courseId: number | undefined): any {
    return courseId ? this.courseQuizzes.get(courseId) : null;
  }

  hasQuiz(courseId: number | undefined): boolean {
    return courseId ? this.courseQuizzes.has(courseId) : false;
  }

  getQuizScorePercentage(courseId: number | undefined): number {
    const quiz = this.getCourseQuiz(courseId);
    if (!quiz || !quiz.bestScore || !quiz.totalMarks) return 0;
    return Math.round((quiz.bestScore / quiz.totalMarks) * 100);
  }

  getQuizScoreColor(courseId: number | undefined): string {
    const quiz = this.getCourseQuiz(courseId);
    if (!quiz) return 'text-gray-600';
    const percentage = this.getQuizScorePercentage(courseId);
    if (percentage >= quiz.passingScore) return 'text-green-600';
    return 'text-red-600';
  }

  getQuizScoreBgColor(courseId: number | undefined): string {
    const quiz = this.getCourseQuiz(courseId);
    if (!quiz) return 'bg-gray-50 border-gray-200';
    const percentage = this.getQuizScorePercentage(courseId);
    if (percentage >= quiz.passingScore) return 'bg-green-50 border-green-200';
    return 'bg-red-50 border-red-200';
  }

  navigateToQuiz(courseId: number | undefined): void {
    this.router.navigate(['/dashboard/my-exams']);
  }
}
