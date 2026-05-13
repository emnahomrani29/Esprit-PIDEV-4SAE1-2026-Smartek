import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RecommendationService, RecommendationItem } from '../../../../core/services/recommendation.service';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recommendations.component.html',
  styleUrl: './recommendations.component.scss'
})
export class RecommendationsComponent implements OnInit, OnChanges {
  @Input() learnerId: number | null = null;

  recommendations: RecommendationItem[] = [];
  isLoading = false;
  error = '';

  constructor(private recommendationService: RecommendationService) {}

  ngOnInit(): void {
    console.log('[Recommendations] ngOnInit, learnerId=', this.learnerId);
    if (this.learnerId) this.load();
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('[Recommendations] ngOnChanges', changes);
    if (changes['learnerId']?.currentValue) {
      this.load();
    }
  }

  load(): void {
    if (!this.learnerId) return;
    console.log('[Recommendations] loading for learnerId=', this.learnerId);
    this.isLoading = true;
    this.error = '';
    this.recommendationService.getRecommendations(this.learnerId, 5).subscribe({
      next: (res) => {
        console.log('[Recommendations] received', res);
        this.recommendations = res.recommendations;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('[Recommendations] error', err);
        this.error = 'Impossible de charger les recommandations.';
        this.isLoading = false;
      }
    });
  }

  getScorePercent(score: number): number {
    return Math.round(score * 100);
  }

  getScoreColor(score: number): string {
    if (score >= 0.6) return 'bg-green-500';
    if (score >= 0.3) return 'bg-blue-500';
    return 'bg-gray-400';
  }

  getScoreBadge(score: number): string {
    if (score >= 0.6) return 'Très pertinent';
    if (score >= 0.3) return 'Pertinent';
    return 'Suggéré';
  }

  getScoreBadgeClass(score: number): string {
    if (score >= 0.6) return 'bg-green-100 text-green-700';
    if (score >= 0.3) return 'bg-blue-100 text-blue-700';
    return 'bg-gray-100 text-gray-600';
  }
}
