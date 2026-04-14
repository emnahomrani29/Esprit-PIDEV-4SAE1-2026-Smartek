export interface Interview {
  id?: number;
  applicationId: number;
  offerId: number;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  interviewDate: string;
  location: string;
  meetingLink?: string;
  notes?: string;
  status: string;
  createdBy: number;
  createdAt?: string;
  updatedAt?: string;
  offerTitle?: string; // Ajouté pour afficher le titre de l'offre
}

export interface InterviewRequest {
  applicationId: number;
  interviewDate: string;
  location: string;
  meetingLink?: string;
  notes?: string;
  createdBy: number;
}
