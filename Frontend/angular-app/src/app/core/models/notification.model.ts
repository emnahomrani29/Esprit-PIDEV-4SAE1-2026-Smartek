export type NotificationType = 'APPROVAL' | 'REJECTION';

export interface Notification {
  notificationId: number;
  learnerId: number;
  evidenceId: number;
  message: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}
