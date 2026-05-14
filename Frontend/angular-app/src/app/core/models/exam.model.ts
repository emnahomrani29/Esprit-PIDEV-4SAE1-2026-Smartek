export interface Exam {
  id?: number;
  courseId?: number;
  trainingId?: number;
  examType: 'QUIZ' | 'EXAM';
  title: string;
  description?: string;
  duration: number;
  passingScore: number;
  totalMarks?: number; // Optionnel - calculé automatiquement
  startDate?: string;
  endDate?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
  // Enrollment fields
  isUnlocked?: boolean;
  isCompleted?: boolean;
  // Questions
  questions?: Question[];
  questionCount?: number;
  exerciseCount?: number;
  // Learner specific fields
  isLocked?: boolean;
  hasAttempted?: boolean;
  bestScore?: number;
  attemptsCount?: number;
  courseName?: string;
  trainingName?: string;
  remainingTimeMinutes?: number;
}

export interface Question {
  id?: number;
  examId?: number;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER';
  marks: number;
  correctAnswer?: string;
  options?: QuestionOption[];
}

export interface QuestionOption {
  id?: number;
  questionId?: number;
  optionText: string;
  isCorrect: boolean;
}

export interface Exercise {
  id?: number;
  examId: number;
  exerciseNumber: number;
  content: string;
  marks: number;
  instructions?: string;
}

export interface ExamResult {
  id?: number;
  examId: number;
  userId: number;
  obtainedMarks: number;
  totalMarks: number;
  percentage: number;
  passed: boolean;
  isCorrected: boolean;
  correctedBy?: number;
  correctedAt?: string;
  submittedAt?: string;
}

export interface ExerciseAnswer {
  id?: number;
  exerciseId: number;
  examResultId: number;
  answerText: string;
  marksObtained?: number;
  trainerFeedback?: string;
  isCorrected: boolean;
}

export interface UserAnswer {
  id?: number;
  questionId: number;
  examResultId: number;
  selectedAnswer: string;
  isCorrect: boolean;
}
