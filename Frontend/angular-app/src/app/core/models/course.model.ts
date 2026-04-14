export interface Course {
  courseId?: number;
  title: string;
  content: string;
  duration: string; // Format: YYYY-MM-DD
  trainerId: number;
  chapters?: any[]; // Pour la compatibilité avec my-courses
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
}

export interface CourseUpdateRequest {
  title: string;
  content: string;
  duration: string;
}
