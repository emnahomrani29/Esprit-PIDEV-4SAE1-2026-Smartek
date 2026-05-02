import { DeliveryMode, SessionStatus, LiveSessionResponse } from './live-session.model';

export interface Course {
  courseId?: number;
  title: string;
  content?: string;
  duration: string; // LocalDate from backend, represented as string in frontend
  trainerId: number;
  deliveryMode: DeliveryMode;
  chapters?: Chapter[];
  liveSessions?: LiveSessionResponse[];
  createdAt?: string;
  updatedAt?: string;
  message?: string;
}

export interface CourseCreateRequest {
  title: string;
  content?: string;
  duration: string;
  trainerId: number;
  deliveryMode: DeliveryMode;
}

export interface CourseUpdateRequest {
  title?: string;
  content?: string;
  duration?: string;
  trainerId?: number;
  deliveryMode?: DeliveryMode;
}

export interface Chapter {
  chapterId?: number;
  title: string;
  description?: string;
  orderIndex: number;
  pdfFileName?: string;
  pdfFilePath?: string;
  courseId?: number;
  createdAt?: string;
  updatedAt?: string;
  message?: string;
}

export interface CourseDetail {
  course: string;
  imageSrc: string;
  profession: string;
  price: string;
  category: string;
}
