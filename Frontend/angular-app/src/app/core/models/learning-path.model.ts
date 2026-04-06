export type LearningPathStatus = 'PLANIFIE' | 'EN_COURS' | 'TERMINE' | 'ABANDONNE';

export interface LearningPathRequest {
  title: string;
  description?: string;
  learnerId: number;
  learnerName: string;
  status: LearningPathStatus;
  startDate: string;
  endDate?: string;
  progress: number;
}

export interface LearningPathResponse {
  pathId: number;
  title: string;
  description?: string;
  learnerId: number;
  learnerName: string;
  status: LearningPathStatus;
  startDate: string;
  endDate?: string;
  progress: number;
}
