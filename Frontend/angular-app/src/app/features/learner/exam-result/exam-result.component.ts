import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ExamService } from '../../../core/services/exam.service';
import { Exam, ExamResult, UserAnswer } from '../../../core/models/exam.model';

@Component({
  selector: 'app-exam-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './exam-result.component.html',
  styleUrl: './exam-result.component.scss'
})
export class ExamResultComponent implements OnInit {
  result: ExamResult | null = null;
  exam: Exam | null = null;
  userAnswers: UserAnswer[] = [];
  resultId: number = 0;
  isLoading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private examService: ExamService
  ) {}

  ngOnInit(): void {
    this.resultId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.resultId) {
      this.loadResult();
    }
  }

  loadResult(): void {
    this.isLoading = true;
    this.examService.getExamResult(this.resultId).subscribe({
      next: (result) => {
        this.result = result;
        this.loadExam(result.examId);
        this.loadUserAnswers(this.resultId);
      },
      error: (error) => {
        console.error('Error loading result:', error);
        this.errorMessage = 'Erreur lors du chargement des résultats';
        this.isLoading = false;
      }
    });
  }

  loadExam(examId: number): void {
    this.examService.getExamById(examId).subscribe({
      next: (exam) => {
        this.exam = exam;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading exam:', error);
        this.isLoading = false;
      }
    });
  }

  loadUserAnswers(resultId: number): void {
    this.examService.getUserAnswers(resultId).subscribe({
      next: (answers) => {
        this.userAnswers = answers;
      },
      error: (error) => {
        console.error('Error loading answers:', error);
      }
    });
  }

  isAnswerCorrect(questionId: number): boolean {
    const answer = this.userAnswers.find(a => a.questionId === questionId);
    return answer?.isCorrect || false;
  }

  getUserAnswer(questionId: number): string {
    const answer = this.userAnswers.find(a => a.questionId === questionId);
    return answer?.selectedAnswer || 'Non répondu';
  }

  goBack(): void {
    this.router.navigate(['/learner-exams']);
  }

  retakeExam(): void {
    if (this.result) {
      this.router.navigate(['/learner-exams/take', this.result.examId]);
    }
  }
}
