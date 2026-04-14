import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ExamService } from '../../../core/services/exam.service';
import { Exam } from '../../../core/models/exam.model';

@Component({
  selector: 'app-learner-exams',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-exams.component.html',
  styleUrl: './learner-exams.component.scss'
})
export class LearnerExamsComponent implements OnInit {
  quizzes: Exam[] = [];
  exams: Exam[] = [];
  filteredQuizzes: Exam[] = [];
  filteredExams: Exam[] = [];
  isLoading = false;
  errorMessage = '';

  // Recherche et tri
  searchTerm: string = '';
  sortBy: 'name' | 'status' | 'score' = 'name';
  sortOrder: 'asc' | 'desc' = 'asc';

  constructor(
    private authService: AuthService,
    private examService: ExamService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMyExams();
  }

  loadMyExams(): void {
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) {
      this.errorMessage = 'Utilisateur non connecté';
      return;
    }

    this.isLoading = true;
    
    this.examService.getMyExams(currentUser.userId).subscribe({
      next: (data) => {
        this.quizzes = data.filter(item => item.examType === 'QUIZ');
        this.exams = data.filter(item => item.examType === 'EXAM');
        this.isLoading = false;
        this.applyFiltersAndSort();
      },
      error: (error) => {
        console.error('Error loading exams:', error);
        this.errorMessage = 'Erreur lors du chargement des examens';
        this.isLoading = false;
      }
    });
  }

  getScorePercentage(exam: Exam): number {
    if (!exam.bestScore || !exam.totalMarks) return 0;
    return Math.round((exam.bestScore / exam.totalMarks) * 100);
  }

  getScoreColor(exam: Exam): string {
    const percentage = this.getScorePercentage(exam);
    if (percentage >= exam.passingScore) return 'text-green-600';
    return 'text-red-600';
  }

  getScoreBgColor(exam: Exam): string {
    const percentage = this.getScorePercentage(exam);
    if (percentage >= exam.passingScore) return 'bg-green-50 border-green-200';
    return 'bg-red-50 border-red-200';
  }

  startExam(exam: Exam): void {
    if (exam.isLocked) {
      const message = exam.examType === 'QUIZ' 
        ? 'Vous devez terminer le cours associé avant de passer ce quiz.'
        : 'Vous devez terminer la formation associée avant de passer cet examen.';
      alert(message);
      return;
    }
    
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;
    
    // Démarrer l'examen (enregistrer l'heure de début)
    this.examService.startExam(exam.id!, currentUser.userId).subscribe({
      next: () => {
        // Rediriger vers la page de passage d'examen
        this.router.navigate(['/learner-exams/take', exam.id]);
      },
      error: (error) => {
        console.error('Error starting exam:', error);
        // Rediriger quand même
        this.router.navigate(['/learner-exams/take', exam.id]);
      }
    });
  }
  
  retakeExam(exam: Exam): void {
    // Vérifier si l'utilisateur a déjà 2 tentatives
    if (exam.attemptsCount && exam.attemptsCount >= 2) {
      alert('Vous avez déjà utilisé votre tentative de reprise. Nombre maximum de tentatives atteint (2).');
      return;
    }
    
    if (!confirm('Êtes-vous sûr de vouloir repasser cet examen ? Votre ancien résultat sera supprimé. Vous aurez droit à une seule reprise.')) {
      return;
    }
    
    const currentUser = this.authService.getUserInfo();
    if (!currentUser?.userId) return;
    
    this.examService.retakeExam(exam.id!, currentUser.userId).subscribe({
      next: () => {
        alert('Vous pouvez maintenant repasser l\'examen');
        // Rediriger vers la page de passage d'examen
        this.router.navigate(['/learner-exams/take', exam.id]);
      },
      error: (error) => {
        console.error('Error retaking exam:', error);
        const errorMessage = error.error?.message || 'Erreur lors de la reprise de l\'examen';
        alert(errorMessage);
      }
    });
  }

  // Recherche et tri
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.applyFiltersAndSort();
  }

  onSortChange(sortBy: 'name' | 'status' | 'score'): void {
    if (this.sortBy === sortBy) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortOrder = 'asc';
    }
    this.applyFiltersAndSort();
  }

  applyFiltersAndSort(): void {
    // Filtrer les quizzes
    let filteredQ = this.quizzes.filter(q => 
      q.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (q.description && q.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );

    // Filtrer les exams
    let filteredE = this.exams.filter(e => 
      e.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (e.description && e.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );

    // Trier les quizzes
    filteredQ.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'status':
          const statusA = a.hasAttempted ? 1 : 0;
          const statusB = b.hasAttempted ? 1 : 0;
          comparison = statusA - statusB;
          break;
        case 'score':
          const scoreA = this.getScorePercentage(a);
          const scoreB = this.getScorePercentage(b);
          comparison = scoreA - scoreB;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    // Trier les exams
    filteredE.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'status':
          const statusA = a.hasAttempted ? 1 : 0;
          const statusB = b.hasAttempted ? 1 : 0;
          comparison = statusA - statusB;
          break;
        case 'score':
          const scoreA = this.getScorePercentage(a);
          const scoreB = this.getScorePercentage(b);
          comparison = scoreA - scoreB;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredQuizzes = filteredQ;
    this.filteredExams = filteredE;
  }

  get displayedQuizzes(): Exam[] {
    return this.filteredQuizzes.length > 0 || this.searchTerm ? this.filteredQuizzes : this.quizzes;
  }

  get displayedExams(): Exam[] {
    return this.filteredExams.length > 0 || this.searchTerm ? this.filteredExams : this.exams;
  }
}
