export type EvidenceStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type EvidenceCategory = 'PROGRAMMING' | 'DESIGN' | 'MANAGEMENT' | 'COMMUNICATION' | 'OTHER';

export interface SkillEvidenceRequest {
  title: string;
  fileUrl?: string;
  description?: string;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  category?: EvidenceCategory;
}

export interface SkillEvidenceResponse {
  evidenceId: number;
  title: string;
  fileUrl?: string;
  description?: string;
  uploadDate: string;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  status: EvidenceStatus;
  score?: number;
  adminComment?: string;
  reviewedBy?: number;
  reviewedAt?: string;
  category?: EvidenceCategory;
}

export interface ApprovalRequest {
  score: number;
  adminComment?: string;
  reviewerId?: number;
}

export interface RejectionRequest {
  adminComment: string;
  reviewerId?: number;
}

export interface LearnerAnalytics {
  totalCount: number;
  approvedCount: number;
  pendingCount: number;
  rejectedCount: number;
  averageScore?: number;
  categoryDistribution: Record<string, number>;
  scoreTrend: { date: string; score: number; title: string }[];
}

export interface GlobalAnalytics {
  totalCount: number;
  approvedCount: number;
  pendingCount: number;
  rejectedCount: number;
  approvalRate: number;
  averageScore?: number;
  categoryDistribution: Record<string, number>;
  submissionTrend: Record<string, number>;
  statusDistribution: Record<string, number>;
}
