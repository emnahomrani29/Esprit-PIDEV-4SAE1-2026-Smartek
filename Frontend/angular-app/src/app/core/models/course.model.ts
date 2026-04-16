import { DeliveryMode } from './live-session.model';

export interface Course {
  courseId?: number;
  title: string;
  content: string;
  duration: string; // Format: YYYY-MM-DD
  trainerId: number;
  deliveryMode?: DeliveryMode;
  chapters?: any[]; // Pour la compatibilité avec my-courses
  liveSessions?: any[];
}

export interface CourseDetail {
  id?: number;
  title: string;
  description: string;
  image?: string;
  duration?: string;
  lessons?: number;
  students?: number;
  rating?: number;
  price?: number;
  instructor?: string;
  category?: string;
  // Anciens champs pour compatibilité
  course?: string;
  imageSrc?: string;
  profession?: string;
}

export interface CourseCreateRequest {
  title: string;
  content: string;
  duration: string;
  trainerId: number;
  deliveryMode: DeliveryMode;
}

export interface CourseUpdateRequest {
  title: string;
  content: string;
  duration: string;
}
