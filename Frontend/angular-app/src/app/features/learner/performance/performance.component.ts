import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PerformanceService, PerformanceStats } from '../../../core/services/performance.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-performance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './performance.component.html',
  styleUrls: ['./performance.component.scss']
})
export class PerformanceComponent implements OnInit {
  stats: any = {
    courses: {
      enrolled: 0,
      inProgress: 0,
      completed: 0,
      completionRate: 0,
      totalChapters: 0,
      completedChapters: 0
    },
    trainings: {
      enrolled: 0,
      inProgress: 0,
      completed: 0,
      averageProgress: 0,
      statusBreakdown: {}
    },
    exams: {
      available: 0,
      attempted: 0,
      passed: 0,
      failed: 0,
      averageScore: 0,
      successRate: 0,
      totalAttempts: 0
    },
    overall: {
      totalHours: 0,
      streak: 0,
      rank: 'Beginner'
    }
  };

  activeTab: 'overview' | 'courses' | 'trainings' | 'exams' = 'overview';
  loading = true;
  error: string | null = null;
  userId: number | null = null;

  constructor(
    private performanceService: PerformanceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    if (userInfo && userInfo.userId) {
      this.userId = userInfo.userId;
      this.loadPerformanceData();
    } else {
      this.error = 'User not authenticated';
      this.loading = false;
    }
  }

  loadPerformanceData(): void {
    if (!this.userId) return;
    
    this.loading = true;
    this.error = null;
    
    this.performanceService.getAllStats(this.userId).subscribe({
      next: (data: PerformanceStats) => {
        this.stats.courses = {
          enrolled: data.courses.totalEnrolled,
          inProgress: data.courses.inProgress,
          completed: data.courses.completed,
          completionRate: data.courses.completionRate,
          totalChapters: data.courses.totalChapters,
          completedChapters: data.courses.completedChapters
        };
        
        this.stats.trainings = {
          enrolled: data.trainings.totalEnrolled,
          inProgress: data.trainings.inProgress,
          completed: data.trainings.completed,
          averageProgress: data.trainings.averageProgress,
          statusBreakdown: data.trainings.statusBreakdown
        };
        
        this.stats.exams = {
          available: data.exams.totalAvailable,
          attempted: data.exams.attempted,
          passed: data.exams.passed,
          failed: data.exams.failed,
          averageScore: data.exams.averageScore,
          successRate: data.exams.successRate,
          totalAttempts: data.exams.totalAttempts
        };
        
        // Calculate overall stats
        this.calculateOverallStats();
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading performance data:', err);
        this.error = 'Failed to load performance data';
        this.loading = false;
      }
    });
  }

  calculateOverallStats(): void {
    // Calculate total hours (estimation based on courses and trainings)
    const avgHoursPerCourse = 10;
    this.stats.overall.totalHours = this.stats.courses.completed * avgHoursPerCourse;
    
    // Calculate streak (mock for now - would need backend support)
    this.stats.overall.streak = 0;
    
    // Calculate rank based on completion rate
    const completionRate = this.stats.courses.completionRate;
    if (completionRate >= 80) {
      this.stats.overall.rank = 'Expert';
    } else if (completionRate >= 60) {
      this.stats.overall.rank = 'Advanced';
    } else if (completionRate >= 40) {
      this.stats.overall.rank = 'Intermediate';
    } else {
      this.stats.overall.rank = 'Beginner';
    }
  }

  setActiveTab(tab: 'overview' | 'courses' | 'trainings' | 'exams'): void {
    this.activeTab = tab;
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 80) return 'bg-green-500';
    if (percentage >= 60) return 'bg-blue-500';
    if (percentage >= 40) return 'bg-yellow-500';
    return 'bg-red-500';
  }

  getRankColor(rank: string): string {
    switch (rank) {
      case 'Expert': return 'text-purple-600';
      case 'Advanced': return 'text-blue-600';
      case 'Intermediate': return 'text-green-600';
      default: return 'text-gray-600';
    }
  }

  getStatusBreakdownEntries(): Array<{key: string, value: number}> {
    return Object.entries(this.stats.trainings.statusBreakdown || {})
      .map(([key, value]) => ({ key, value: value as number }));
  }

  formatStatusLabel(status: string): string {
    return status.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  }
}
