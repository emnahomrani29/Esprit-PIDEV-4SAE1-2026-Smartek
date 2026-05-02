import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, interval } from 'rxjs';

export interface NotificationMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info' | 'reminder';
  title: string;
  message: string;
  duration?: number; // in milliseconds, 0 for persistent
  actions?: NotificationAction[];
  timestamp: Date;
  isPopup?: boolean; // For reminder popups
}

export interface NotificationAction {
  label: string;
  action: () => void;
  style?: 'primary' | 'secondary' | 'danger';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications$ = new BehaviorSubject<NotificationMessage[]>([]);
  private reminderQueue: NotificationMessage[] = [];
  private lastReminderTime: { [key: string]: number } = {};

  constructor() {
    // Start reminder system - check every 5 minutes
    this.startReminderSystem();
  }

  getNotifications(): Observable<NotificationMessage[]> {
    return this.notifications$.asObservable();
  }

  showNotification(notification: Omit<NotificationMessage, 'id' | 'timestamp'>): void {
    const newNotification: NotificationMessage = {
      ...notification,
      id: this.generateId(),
      timestamp: new Date()
    };

    const currentNotifications = this.notifications$.value;
    this.notifications$.next([...currentNotifications, newNotification]);

    // Auto-remove after duration (default 5 seconds)
    if (notification.duration !== 0) {
      const duration = notification.duration || 5000;
      setTimeout(() => {
        this.removeNotification(newNotification.id);
      }, duration);
    }
  }

  showSuccess(title: string, message: string, duration?: number): void {
    this.showNotification({
      type: 'success',
      title,
      message,
      duration
    });
  }

  showError(title: string, message: string, duration?: number): void {
    this.showNotification({
      type: 'error',
      title,
      message,
      duration: duration || 8000 // Errors stay longer
    });
  }

  showWarning(title: string, message: string, duration?: number): void {
    this.showNotification({
      type: 'warning',
      title,
      message,
      duration
    });
  }

  showInfo(title: string, message: string, duration?: number): void {
    this.showNotification({
      type: 'info',
      title,
      message,
      duration
    });
  }

  showReminder(title: string, message: string, actions?: NotificationAction[]): void {
    this.showNotification({
      type: 'reminder',
      title,
      message,
      duration: 0, // Reminders are persistent
      actions,
      isPopup: true
    });
  }

  // Real data-driven admin reminders
  showAdminReminder(pendingCount: number, urgentCount: number = 0): void {
    const reminderKey = 'admin_pending';
    const now = Date.now();
    const lastReminder = this.lastReminderTime[reminderKey] || 0;
    const reminderInterval = 30 * 60 * 1000; // 30 minutes

    // Only show reminder if enough time has passed and there are pending items
    if (pendingCount > 0 && (now - lastReminder) > reminderInterval) {
      const isUrgent = urgentCount > 0;
      const title = isUrgent ? '🚨 URGENT: Sponsorship Approvals Required' : '⏰ Pending Sponsorship Approvals';
      
      let message = '';
      if (isUrgent) {
        message = `You have ${urgentCount} urgent sponsorship${urgentCount > 1 ? 's' : ''} pending for more than 24 hours`;
        if (pendingCount > urgentCount) {
          message += `, and ${pendingCount - urgentCount} other${pendingCount - urgentCount > 1 ? 's' : ''} waiting for review.`;
        } else {
          message += '.';
        }
      } else {
        message = `You have ${pendingCount} sponsorship${pendingCount > 1 ? 's' : ''} waiting for your review.`;
      }
      
      this.showReminder(
        title,
        message,
        [
          {
            label: 'Review Now',
            action: () => {
              window.location.href = '/dashboard/admin-approvals';
            },
            style: 'primary'
          },
          {
            label: 'Remind in 30min',
            action: () => {
              this.scheduleReminder(reminderKey, 30);
            },
            style: 'secondary'
          },
          {
            label: 'Dismiss',
            action: () => {
              this.lastReminderTime[reminderKey] = now;
            },
            style: 'secondary'
          }
        ]
      );

      this.lastReminderTime[reminderKey] = now;
    }
  }

  // Real data-driven sponsor reminders
  showSponsorReminder(rejectedCount: number, pendingCount: number, contractsExpiring: number = 0): void {
    const reminderKey = 'sponsor_status';
    const now = Date.now();
    const lastReminder = this.lastReminderTime[reminderKey] || 0;
    const reminderInterval = 60 * 60 * 1000; // 1 hour

    if ((now - lastReminder) > reminderInterval) {
      if (rejectedCount > 0) {
        this.showReminder(
          '❌ Action Required: Rejected Sponsorships',
          `You have ${rejectedCount} rejected sponsorship${rejectedCount > 1 ? 's' : ''} that need to be reviewed and resubmitted with the requested changes.`,
          [
            {
              label: 'View Details',
              action: () => {
                window.location.href = '/sponsor';
              },
              style: 'primary'
            },
            {
              label: 'Remind Later',
              action: () => {
                this.scheduleReminder(reminderKey, 120); // 2 hours
              },
              style: 'secondary'
            }
          ]
        );
        this.lastReminderTime[reminderKey] = now;
      } else if (contractsExpiring > 0) {
        this.showWarning(
          '⚠️ Contracts Expiring Soon',
          `You have ${contractsExpiring} contract${contractsExpiring > 1 ? 's' : ''} expiring within 30 days. Consider renewing to continue sponsorship activities.`,
          10000
        );
        this.lastReminderTime[reminderKey] = now;
      } else if (pendingCount > 0) {
        this.showInfo(
          '⏳ Sponsorships Under Review',
          `You have ${pendingCount} sponsorship${pendingCount > 1 ? 's' : ''} currently being reviewed by the admin. You'll be notified once they're processed.`,
          8000
        );
        this.lastReminderTime[reminderKey] = now;
      }
    }
  }

  // Contract expiration reminders with real data
  showContractExpirationReminder(expiringContracts: any[]): void {
    if (expiringContracts.length > 0) {
      const urgentContracts = expiringContracts.filter(c => c.daysUntilExpiry <= 7);
      const warningContracts = expiringContracts.filter(c => c.daysUntilExpiry > 7 && c.daysUntilExpiry <= 30);

      if (urgentContracts.length > 0) {
        const contractList = urgentContracts.map(c => `${c.contractNumber} (${c.daysUntilExpiry} days)`).join(', ');
        this.showError(
          '🚨 URGENT: Contracts Expiring This Week',
          `Critical: The following contracts expire within 7 days: ${contractList}. Immediate action required!`,
          0 // Persistent
        );
      } else if (warningContracts.length > 0) {
        const contractList = warningContracts.map(c => `${c.contractNumber} (${c.daysUntilExpiry} days)`).join(', ');
        this.showWarning(
          '⚠️ Contracts Expiring Soon',
          `The following contracts are expiring within 30 days: ${contractList}`,
          10000
        );
      }
    }
  }

  // Budget threshold reminders with real calculations
  showBudgetThresholdReminder(sponsorName: string, remainingBudget: number, totalBudget: number): void {
    const percentage = (remainingBudget / totalBudget) * 100;
    
    if (percentage <= 5 && percentage > 0) {
      this.showError(
        '🚨 CRITICAL: Budget Almost Depleted',
        `${sponsorName} has only ${percentage.toFixed(1)}% (${remainingBudget.toFixed(0)} TND) of their budget remaining. Immediate attention required!`,
        0 // Persistent
      );
    } else if (percentage <= 10 && percentage > 5) {
      this.showWarning(
        '⚠️ Low Budget Alert',
        `${sponsorName} has only ${percentage.toFixed(1)}% (${remainingBudget.toFixed(0)} TND) of their budget remaining.`,
        8000
      );
    } else if (percentage <= 0) {
      this.showError(
        '❌ Budget Exceeded',
        `${sponsorName} has exceeded their budget by ${Math.abs(remainingBudget).toFixed(0)} TND. No new sponsorships can be approved.`,
        0 // Persistent
      );
    }
  }

  // System health reminders
  showSystemHealthReminder(responseTime: number, errorRate: number): void {
    if (responseTime > 5) {
      this.showWarning(
        '⚠️ System Performance Alert',
        `Average response time is ${responseTime.toFixed(1)} hours. Consider reviewing pending approvals to improve system efficiency.`,
        8000
      );
    }

    if (errorRate > 15) {
      this.showError(
        '🚨 High Error Rate Detected',
        `System error rate is ${errorRate.toFixed(1)}%. Please review recent operations and contact technical support if needed.`,
        0
      );
    }
  }

  removeNotification(id: string): void {
    const currentNotifications = this.notifications$.value;
    const filteredNotifications = currentNotifications.filter(n => n.id !== id);
    this.notifications$.next(filteredNotifications);
  }

  clearAll(): void {
    this.notifications$.next([]);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
  }

  private scheduleReminder(key: string, minutes: number): void {
    setTimeout(() => {
      // Reset the reminder time to allow showing again
      delete this.lastReminderTime[key];
    }, minutes * 60 * 1000);
  }

  private startReminderSystem(): void {
    // Check for reminders every 5 minutes
    interval(5 * 60 * 1000).subscribe(() => {
      // This will be triggered by the components that have access to real data
    });
  }
}