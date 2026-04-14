export interface Application {
  id?: number;
  offerId: number;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  coverLetter?: string;
  cvBase64?: string;
  cvFileName?: string;
  status: string;
  appliedAt?: string;
}

export interface ApplicationRequest {
  offerId: number;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  coverLetter?: string;
  cvBase64?: string;
  cvFileName?: string;
}
