export interface Training {
  trainingId?: number;
  title: string;
  description?: string;
  category: string;
  level: string;
  duration: string;
  courseIds?: number[];
  courses?: CourseInfo[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CourseInfo {
  courseId: number;
  title: string;
  content: string;
  duration: string;
  instructor?: string;
  chapters?: ChapterInfo[];
}

export interface ChapterInfo {
  chapterId: number;
  title: string;
  description: string;
  orderIndex: number;
  pdfFileName?: string;
  pdfFilePath?: string;
}

export interface TrainingCreateRequest {
  title: string;
  description?: string;
  category: string;
  level: string;
  duration: string;
  courseIds?: number[];
  createdBy?: number;
}

export interface TrainingUpdateRequest {
  title: string;
  description?: string;
  category: string;
  level: string;
  duration: string;
  courseIds?: number[];
}
