export interface CertificationTemplate {
  id?: number;
  title: string;
  description: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface EarnedCertification {
  id?: number;
  certificationTemplate: CertificationTemplate;
  learnerId: number;
  issueDate: string | Date;
  expiryDate?: string | Date;
  certificateUrl?: string;
  awardedBy: number;
  isExpired?: boolean;
  examId?: string;
}

export interface AwardCertificationRequest {
  certificationTemplateId: number;
  learnerId: number;
  issueDate: string;
  expiryDate?: string;
  certificateUrl?: string;
}

export interface BulkAwardCertificationRequest {
  certificationTemplateId: number;
  learnerIds: number[];
}
