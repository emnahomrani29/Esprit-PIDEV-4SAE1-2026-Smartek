export interface Course {
  courseId?: number;
  name: string;
  title: string;
  content: string;
  duration: string;
  trainerId: number;
  chapters?: any[];
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
