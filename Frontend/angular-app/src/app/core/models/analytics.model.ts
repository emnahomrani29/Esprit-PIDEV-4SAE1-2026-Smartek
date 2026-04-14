export interface LearnerAnalytics {
  learnerId: number;
  learnerName: string;
  totalCourses: number;
  completedCourses: number;
  totalExams: number;
  passedExams: number;
  averageScore: number;
}

export interface TrainingAnalytics {
  trainingId: number;
  trainingName: string;
  totalLearners: number;
  completedLearners: number;
  averageProgress: number;
  averageScore: number;
}

export interface ExamAnalytics {
  examId: number;
  examTitle: string;
  learnerId: number;
  learnerName: string;
  score: number;
  status: string;
  submittedAt: string;
}
