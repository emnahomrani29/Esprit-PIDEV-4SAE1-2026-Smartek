import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ExamService } from '../../../core/services/exam.service';
import { Exam, Question } from '../../../core/models/exam.model';

@Component({
  selector: 'app-my-exams',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './my-exams.component.html',
  styleUrl: './my-exams.component.scss'
})
export class MyExamsComponent implements OnInit {
  quizzes: Exam[] = [];
  exams: Exam[] = [];
  isLoading = false;
  errorMessage = '';
  
  // Exam taking modal
  showExamModal = false;
  currentExam: Exam | null = null;
  examQuestions: Question[] = [];
  examForm: FormGroup;
  currentQuestionIndex = 0;
  timeRemaining = 0;
  timerInterval: any;
  isSubmitting = false;

  constructor(
    private authService: AuthService,
    private examService: ExamService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.examForm = this.fb.group({
      answers: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadMyExams();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
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
        // Séparer les quiz et les examens
        this.quizzes = data.filter(item => item.examType === 'QUIZ');
        this.exams = data.filter(item => item.examType === 'EXAM');
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading exams:', error);
        this.errorMessage = 'Erreur lors du chargement des examens';
        this.isLoading = false;
      }
    });
  }

  startExam(exam: Exam): void {
    if (exam.isLocked) {
      const message = exam.examType === 'QUIZ' 
        ? 'Vous devez terminer le cours associé avant de passer ce quiz.'
        : 'Vous devez terminer toute la formation avant de passer cet examen.';
      alert(message);
      return;
    }
    
    if (exam.hasAttempted) {
      // Ne pas permettre de repasser l'examen
      return;
    }

    // Charger les questions de l'examen
    this.isLoading = true;
    this.examService.getExamById(exam.id!).subscribe({
      next: (examData) => {
        if (!examData.questions || examData.questions.length === 0) {
          alert('Cet examen ne contient pas encore de questions.');
          this.isLoading = false;
          return;
        }

        this.currentExam = examData;
        this.examQuestions = examData.questions;
        this.currentQuestionIndex = 0;
        this.timeRemaining = exam.duration * 60; // Convert to seconds
        
        // Initialize form with answers
        const answersArray = this.examForm.get('answers') as FormArray;
        answersArray.clear();
        
        examData.questions.forEach(question => {
          if (question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'TRUE_FALSE') {
            // For multiple choice, store selected option IDs
            answersArray.push(this.fb.control([], Validators.required));
          } else {
            // For short answer
            answersArray.push(this.fb.control('', Validators.required));
          }
        });

        this.showExamModal = true;
        this.startTimer();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading exam questions:', error);
        alert('Erreur lors du chargement de l\'examen');
        this.isLoading = false;
      }
    });
  }

  startTimer(): void {
    this.timerInterval = setInterval(() => {
      this.timeRemaining--;
      
      if (this.timeRemaining <= 0) {
        clearInterval(this.timerInterval);
        alert('Le temps est écoulé ! Votre examen sera soumis automatiquement.');
        this.submitExam();
      }
    }, 1000);
  }

  getFormattedTime(): string {
    const minutes = Math.floor(this.timeRemaining / 60);
    const seconds = this.timeRemaining % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  get answers(): FormArray {
    return this.examForm.get('answers') as FormArray;
  }

  getCurrentAnswer(): any {
    return this.answers.at(this.currentQuestionIndex).value;
  }

  getCurrentQuestion(): Question {
    return this.examQuestions[this.currentQuestionIndex];
  }

  isMultipleChoiceQuestion(): boolean {
    const question = this.getCurrentQuestion();
    return question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'TRUE_FALSE';
  }

  toggleOption(optionIndex: number): void {
    const currentAnswer = this.getCurrentAnswer();
    const question = this.getCurrentQuestion();
    
    if (question.questionType === 'TRUE_FALSE') {
      // For true/false, only one answer
      this.answers.at(this.currentQuestionIndex).setValue([optionIndex]);
    } else {
      // For multiple choice, toggle selection
      const index = currentAnswer.indexOf(optionIndex);
      if (index > -1) {
        currentAnswer.splice(index, 1);
      } else {
        currentAnswer.push(optionIndex);
      }
      this.answers.at(this.currentQuestionIndex).setValue([...currentAnswer]);
    }
  }

  isOptionSelected(optionIndex: number): boolean {
    const currentAnswer = this.getCurrentAnswer();
    return Array.isArray(currentAnswer) && currentAnswer.includes(optionIndex);
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.examQuestions.length - 1) {
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
    const answer = this.answers.at(index).value;
    if (Array.isArray(answer)) {
      return answer.length > 0;
    }
    return answer !== '' && answer !== null;
  }

  getAnsweredQuestionsCount(): number {
    return this.examQuestions.filter((_, index) => this.isQuestionAnswered(index)).length;
  }

  canSubmit(): boolean {
    return this.getAnsweredQuestionsCount() === this.examQuestions.length;
  }

  submitExam(): void {
    if (!this.canSubmit() && this.timeRemaining > 0) {
      const unanswered = this.examQuestions.length - this.getAnsweredQuestionsCount();
      if (!confirm(`Il vous reste ${unanswered} question(s) sans réponse. Voulez-vous vraiment soumettre ?`)) {
        return;
      }
    }

    this.isSubmitting = true;
    clearInterval(this.timerInterval);

    // Calculate time taken
    const timeTaken = this.currentExam!.duration - Math.floor(this.timeRemaining / 60);

    // Prepare submission data
    const currentUser = this.authService.getUserInfo();
    const submissionData = {
      examId: this.currentExam!.id,
      userId: currentUser?.userId,
      timeTaken: timeTaken,
      answers: this.examQuestions.map((question, index) => {
        const answer = this.answers.at(index).value;
        
        if (question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'TRUE_FALSE') {
          // For multiple choice, get selected option texts
          const selectedOptions = Array.isArray(answer) 
            ? answer.map((optIndex: number) => question.options![optIndex].optionText)
            : [];
          
          return {
            questionId: question.id,
            selectedAnswer: selectedOptions.join(', '),
            selectedOptions: answer
          };
        } else {
          // For short answer
          return {
            questionId: question.id,
            selectedAnswer: answer || ''
          };
        }
      })
    };

    console.log('Submitting exam:', submissionData);

    // Submit to backend
    this.examService.submitExam(submissionData).subscribe({
      next: (result) => {
        console.log('Exam result:', result);
        this.isSubmitting = false;
        
        // Save exam info before closing modal
        const examTitle = this.currentExam!.title;
        const passingScore = this.currentExam!.passingScore;
        
        // Close modal
        this.closeExamModal();
        
        // Show results
        const percentage = (result.obtainedMarks / result.totalMarks) * 100;
        const passed = percentage >= passingScore;
        
        alert(
          `${examTitle} - Terminé !\n\n` +
          `Score: ${result.obtainedMarks}/${result.totalMarks} (${percentage.toFixed(1)}%)\n` +
          `Statut: ${passed ? '✓ Réussi' : '✗ Échoué'}\n` +
          `Note de passage: ${passingScore}%`
        );
        
        this.loadMyExams(); // Reload to update status
      },
      error: (error) => {
        console.error('Error submitting exam:', error);
        alert('Erreur lors de la soumission de l\'examen: ' + (error.error?.message || error.message));
        this.isSubmitting = false;
      }
    });
  }

  closeExamModal(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    
    if (!this.isSubmitting && this.timeRemaining > 0) {
      if (!confirm('Êtes-vous sûr de vouloir quitter ? Votre progression ne sera pas sauvegardée.')) {
        return;
      }
    }
    
    this.showExamModal = false;
    this.currentExam = null;
    this.examQuestions = [];
    this.currentQuestionIndex = 0;
    this.examForm.reset();
  }

  getExamStatusColor(exam: Exam): string {
    if (exam.isLocked) return 'bg-gray-100 text-gray-600';
    if (exam.hasAttempted) return 'bg-green-100 text-green-800';
    return 'bg-blue-100 text-blue-800';
  }

  getExamStatusLabel(exam: Exam): string {
    if (exam.isLocked) return 'Verrouillé';
    if (exam.hasAttempted) return 'Déjà passé';
    return 'Disponible';
  }

  getExamStatusIcon(exam: Exam): string {
    if (exam.isLocked) return 'lock';
    if (exam.hasAttempted) return 'check_circle';
    return 'play_circle_outline';
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

  getRemainingTimeFormatted(exam: Exam): string {
    if (!exam.remainingTimeMinutes) return 'Pas de limite';
    
    const minutes = exam.remainingTimeMinutes;
    
    if (minutes <= 0) return 'Expiré';
    if (minutes < 60) return `${minutes} min`;
    
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    
    if (hours < 24) {
      return remainingMinutes > 0 
        ? `${hours}h ${remainingMinutes}min` 
        : `${hours}h`;
    }
    
    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;
    
    return remainingHours > 0 
      ? `${days}j ${remainingHours}h` 
      : `${days}j`;
  }

  getRemainingTimeColor(exam: Exam): string {
    if (!exam.remainingTimeMinutes) return 'text-gray-600';
    
    const minutes = exam.remainingTimeMinutes;
    
    if (minutes <= 0) return 'text-red-600';
    if (minutes < 60) return 'text-red-600'; // Moins d'1 heure
    if (minutes < 1440) return 'text-orange-600'; // Moins d'1 jour
    
    return 'text-green-600';
  }
}
