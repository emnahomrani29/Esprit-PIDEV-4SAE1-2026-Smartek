export interface Chapter {
  chapterId?: number;
  title: string;
  description?: string;
  orderIndex: number;
  pdfFileName?: string;
  pdfFilePath?: string;
  courseId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ChapterCreateRequest {
  title: string;
  description?: string;
  orderIndex: number;
}

export interface ChapterUpdateRequest {
  title: string;
  description?: string;
  orderIndex: number;
}
