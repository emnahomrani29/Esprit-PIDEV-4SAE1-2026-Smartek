export interface BadgeTemplate {
  id?: number;
  name: string;
  description: string;
  examId?: number;
  minimumScore?: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface EarnedBadge {
  id?: number;
  badgeTemplate: BadgeTemplate;
  learnerId: number;
  awardDate: Date;
  awardedBy: number;
  createdAt?: Date;
}

export interface AwardBadgeRequest {
  badgeTemplateId: number;
  learnerId: number;
}

export interface BulkAwardBadgeRequest {
  badgeTemplateId: number;
  learnerIds: number[];
}
