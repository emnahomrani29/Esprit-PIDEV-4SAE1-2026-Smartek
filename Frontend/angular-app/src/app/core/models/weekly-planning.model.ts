export interface WeeklyPlanningItem {
  planningId?: number;
  type: 'TRAINING' | 'EXAM' | 'EVENT';
  itemId?: number;
  title: string;
  description?: string;
  date: string;
  startTime: string;
  endTime: string;
  location?: string;
  color: string;
  maxParticipants?: number;
  currentParticipants?: number;
  status: 'DRAFT' | 'PUBLISHED';
  published?: boolean;
}

export interface WeeklyPlanningRequest {
  weekStartDate: string;
  trainerId: number;
  items: WeeklyPlanningItem[];
}

export interface WeeklyStats {
  totalSessions: number;
  trainingSessions: number;
  examSessions: number;
  eventSessions: number;
  publishedSessions: number;
  draftSessions: number;
  totalHours: number;
}

export interface WeeklyPlanningResponse {
  weekStartDate: string;
  weekEndDate: string;
  trainerId: number;
  items: WeeklyPlanningItem[];
  stats: WeeklyStats;
}

export interface TrainingItem {
  trainingId: number;
  title: string;
  description?: string;
  category: string;
  level: string;
  duration: string;
}

export interface ExamItem {
  id: number;
  title: string;
  description?: string;
  duration: number;
  examType: string;
  totalMarks: number;
}

export interface EventItem {
  eventId: number;
  title: string;
  description?: string;
  location: string;
  mode: string;
  maxParticipations: number;
}