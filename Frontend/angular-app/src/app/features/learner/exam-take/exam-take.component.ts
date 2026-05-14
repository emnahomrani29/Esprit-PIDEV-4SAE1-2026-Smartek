import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ExamService } from '../../../core/services/exam.service';
import { AuthService } from '../../../core/services/auth.service';
import { Exam, Question, UserAnswer } from '../../../core/models/exam.model';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-exam-take',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './exam-take.component.html',
  styleUrl: './exam-take.component.scss'
})
export class ExamTakeComponent implements OnInit, OnDestroy {
  exam: Exam | null = null;
  examId: number = 0;
  isLoading = false;
  errorMessage = '';
  
  // Timer
  timeRemaining: number = 0;
  timerInterval: any;
  
  // Answers
  userAnswers: Map<number, string> = new Map();
  
  // Navigation
  currentQuestionIndex = 0;
  
  // Submission
  isSubmitting = false;

  // Auto-save
  private saveSubject = new Subject<void>();
  private isSaving = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private examService: ExamService,
    private authService: AuthService
  ) {
    // Debounce pour sauvegarder toutes les 2 secondes
    this.saveSubject.pipe(
      debounceTime(2000)
    ).subscribe(() => {
      this.saveDraftNow();
    });
  }

  // Sauvegarder avant de quitter la page
  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.userAnswers.size > 0 && !this.isSubmitting) {
      this.saveDraftNow();
      $event.returnValue = 'Vos réponses sont en cours de sauvegarde...';
    }
  }

  ngOnInit(): void {
    this.examId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.examId) {
      this.loadExam();
    }
  }

  loadExam(): void {
    this.isLoading = true;
    const currentUser = this.authService.getUserInfo();
    
    this.examService.getExamById(this.examId).subscribe({
      next: (exam) => {
        this.exam = exam;
        
        // Récupérer le temps restant depuis le serveur
        if (currentUser?.userId) {
          // D'abord, reprendre l'examen (si en pause)
          this.examService.resumeExam(this.examId, currentUser.userId).subscribe({
            next: () => {
              console.log('Examen repris');
              this.loadTimeAndStart(currentUser.userId);
            },
            error: (error) => {
              console.log('Pas besoin de reprendre (première fois ou erreur):', error);
              this.loadTimeAndStart(currentUser.userId);
            }
          });
        } else {
          // Pas d'utilisateur connecté, utiliser la durée totale
          this.timeRemaining = exam.duration * 60;
          this.startTimer();
          this.loadDraft();
          this.isLoading = false;
        }
      },
      error: (error) => {
        console.error('Error loading exam:', error);
        this.errorMessage = 'Erreur lors du chargement de l\'examen';
        this.isLoading = false;
      }
    });
  }

  private loadTimeAndStart(userId: number): void {
    this.examService.getTimeRemaining(this.examId, userId).subscribe({
      next: (response) => {
        this.timeRemaining = response.timeRemaining;
        
        // Si le temps est écoulé, soumettre automatiquement
        if (this.timeRemaining <= 0) {
          this.autoSubmit();
          return;
        }
        
        this.startTimer();
        this.loadDraft();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading time remaining:', error);
        // Fallback sur la durée totale
        this.timeRemaining = this.exam!.duration * 60;
        this.startTimer();
        this.loadDraft();
        this.isLoading = false;
      }
    });
  }

  loadDraft(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;

    this.examService.getDraft(this.examId, currentUser.userId).subscribe({
      next: (draft) => {
        if (draft && draft.answers) {
          // Restaurer les réponses du brouillon
          Object.keys(draft.answers).forEach(questionId => {
            this.userAnswers.set(Number(questionId), draft.answers[questionId]);
          });
          console.log('Brouillon chargé:', this.userAnswers.size, 'réponses');
        }
      },
      error: (error) => {
        console.log('Aucun brouillon trouvé');
      }
    });
  }

  startTimer(): void {
    this.timerInterval = setInterval(() => {
      if (this.timeRemaining > 0) {
        this.timeRemaining--;
      } else {
        this.autoSubmit();
      }
    }, 1000);
  }

  get formattedTime(): string {
    const hours = Math.floor(this.timeRemaining / 3600);
    const minutes = Math.floor((this.timeRemaining % 3600) / 60);
    const seconds = this.timeRemaining % 60;
    
    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  get currentQuestion(): Question | undefined {
    return this.exam?.questions?.[this.currentQuestionIndex];
  }

  selectAnswer(questionId: number, answer: string): void {
    this.userAnswers.set(questionId, answer);
    
    // Déclencher la sauvegarde avec debounce
    this.saveSubject.next();
  }

  saveDraftNow(): void {
    if (this.isSaving) return;
    
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || this.userAnswers.size === 0) return;

    this.isSaving = true;
    this.examService.saveDraft(this.examId, currentUser.userId, this.userAnswers).subscribe({
      next: () => {
        console.log('✓ Brouillon sauvegardé:', this.userAnswers.size, 'réponses');
        this.isSaving = false;
      },
      error: (error) => {
        console.error('Erreur lors de la sauvegarde du brouillon:', error);
        this.isSaving = false;
      }
    });
  }

  getAnswer(questionId: number): string | undefined {
    return this.userAnswers.get(questionId);
  }

  nextQuestion(): void {
    if (this.exam?.questions && this.currentQuestionIndex < this.exam.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  goToQuestion(index: number): void {
    this.currentQuestionIndex = index;
  }

  isQuestionAnswered(index: number): boolean {
    const question = this.exam?.questions?.[index];
    return question ? this.userAnswers.has(question.id!) : false;
  }

  get answeredCount(): number {
    return this.userAnswers.size;
  }

  get totalQuestions(): number {
    return this.exam?.questions?.length || 0;
  }

  submitExam(): void {
    if (!confirm('Êtes-vous sûr de vouloir soumettre votre examen ?')) {
      return;
    }

    this.performSubmit();
  }

  autoSubmit(): void {
    clearInterval(this.timerInterval);
    alert('Le temps est écoulé. Votre examen sera soumis automatiquement.');
    this.performSubmit();
  }

  performSubmit(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId || !this.exam) return;

    this.isSubmitting = true;
    clearInterval(this.timerInterval);

    const answers = Array.from(this.userAnswers.entries()).map(([questionId, answer]) => ({
      questionId,
      selectedAnswer: answer
    }));

    const submission = {
      examId: this.examId,
      userId: currentUser.userId,
      answers
    };

    this.examService.submitExam(submission).subscribe({
      next: (result) => {
        this.router.navigate(['/learner-exams/result', result.id]);
      },
      error: (error) => {
        console.error('Error submitting exam:', error);
        alert('Erreur lors de la soumission de l\'examen');
        this.isSubmitting = false;
      }
    });
  }

  ngOnDestroy(): void {
    const currentUser = this.authService.getUserInfo();
    
    // Sauvegarder une dernière fois avant de quitter
    if (this.userAnswers.size > 0 && !this.isSubmitting) {
      this.saveDraftNow();
    }
    
    // Mettre en pause si pas en train de soumettre
    if (!this.isSubmitting && currentUser?.userId) {
      this.examService.pauseExam(this.examId, currentUser.userId).subscribe({
        next: () => console.log('Examen mis en pause automatiquement'),
        error: (error) => console.error('Error pausing exam:', error)
      });
    }
    
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    
    this.saveSubject.complete();
  }

  exitExam(): void {
    const currentUser = this.authService.getUserInfo();
    
    if (this.userAnswers.size > 0) {
      if (!confirm('Voulez-vous quitter le quiz ? Vos réponses seront sauvegardées et vous pourrez reprendre plus tard.')) {
        return;
      }
      
      // Sauvegarder le brouillon avant de quitter
      this.saveDraftNow();
    }
    
    // Arrêter le timer
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    
    // Mettre en pause l'examen
    if (currentUser?.userId) {
      this.examService.pauseExam(this.examId, currentUser.userId).subscribe({
        next: () => {
          console.log('Examen mis en pause');
          // Retourner à la liste des examens
          this.router.navigate(['/learner-exams']);
        },
        error: (error) => {
          console.error('Error pausing exam:', error);
          // Retourner quand même
          this.router.navigate(['/learner-exams']);
        }
      });
    } else {
      // Retourner à la liste des examens
      this.router.navigate(['/learner-exams']);
    }
  }
}
