import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { AuthService } from '../../../core/services/auth.service';
import { LearnerAnalytics } from '../../../core/models/skill-evidence.model';

@Component({
  selector: 'app-learner-skill-evidence-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './learner-skill-evidence-analytics.component.html',
  styleUrl: './learner-skill-evidence-analytics.component.scss'
})
export class LearnerSkillEvidenceAnalyticsComponent implements OnInit {
  analytics: LearnerAnalytics | null = null;
  isLoading = false;
  errorMessage = '';

  private readonly categoryColors = [
    'bg-blue-400', 'bg-purple-400', 'bg-pink-400',
    'bg-orange-400', 'bg-teal-400', 'bg-cyan-400'
  ];

  constructor(
    private skillEvidenceService: SkillEvidenceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUserInfo();
    if (!user?.userId) return;
    this.isLoading = true;
    this.skillEvidenceService.getLearnerAnalytics(user.userId).subscribe({
      next: (data) => { this.analytics = data; this.isLoading = false; },
      error: () => { this.errorMessage = 'Erreur lors du chargement des analytics'; this.isLoading = false; }
    });
  }

  getApprovalRate(): number {
    if (!this.analytics || this.analytics.totalCount === 0) return 0;
    return Math.round((this.analytics.approvedCount / this.analytics.totalCount) * 100);
  }

  getCategoryEntries(): { key: string; value: number }[] {
    if (!this.analytics?.categoryDistribution) return [];
    return Object.entries(this.analytics.categoryDistribution)
      .map(([key, value]) => ({ key, value }))
      .sort((a, b) => b.value - a.value);
  }

  getCategoryLabel(cat: string): string {
    const map: Record<string, string> = {
      PROGRAMMING: 'Programming', DESIGN: 'Design',
      MANAGEMENT: 'Management', COMMUNICATION: 'Communication', OTHER: 'Other'
    };
    return map[cat] || cat;
  }

  getCategoryBarColor(index: number): string {
    return this.categoryColors[index % this.categoryColors.length];
  }
}
