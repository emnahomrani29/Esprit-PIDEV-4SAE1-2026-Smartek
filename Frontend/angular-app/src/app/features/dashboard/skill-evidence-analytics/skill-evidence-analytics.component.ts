import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { GlobalAnalytics } from '../../../core/models/skill-evidence.model';

@Component({
  selector: 'app-skill-evidence-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skill-evidence-analytics.component.html',
  styleUrl: './skill-evidence-analytics.component.scss'
})
export class SkillEvidenceAnalyticsComponent implements OnInit {
  analytics: GlobalAnalytics | null = null;
  loading = false;
  errorMessage = '';
  exportingPdf = false;

  constructor(private skillEvidenceService: SkillEvidenceService) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading = true;
    this.skillEvidenceService.getGlobalAnalytics().subscribe({
      next: (data) => { this.analytics = data; this.loading = false; },
      error: () => { this.errorMessage = 'Erreur lors du chargement'; this.loading = false; }
    });
  }

  getApprovalRateDisplay(): string {
    if (!this.analytics) return '0%';
    return (this.analytics.approvalRate * 100).toFixed(1) + '%';
  }

  getAverageScoreDisplay(): string {
    if (!this.analytics || this.analytics.averageScore === null || this.analytics.averageScore === undefined) return 'N/A';
    return this.analytics.averageScore.toFixed(1);
  }

  getCategoryEntries(): { key: string; value: number }[] {
    if (!this.analytics?.categoryDistribution) return [];
    return Object.entries(this.analytics.categoryDistribution)
      .map(([key, value]) => ({ key, value: Number(value) }))
      .sort((a, b) => b.value - a.value);
  }

  getStatusEntries(): { key: string; value: number; color: string }[] {
    if (!this.analytics?.statusDistribution) return [];
    const colorMap: Record<string, string> = {
      APPROVED: '#22c55e', PENDING: '#eab308', REJECTED: '#ef4444'
    };
    return Object.entries(this.analytics.statusDistribution)
      .map(([key, value]) => ({ key, value: Number(value), color: colorMap[key] || '#6b7280' }));
  }

  getSubmissionEntries(): { key: string; value: number }[] {
    if (!this.analytics?.submissionTrend) return [];
    return Object.entries(this.analytics.submissionTrend)
      .map(([key, value]) => ({ key, value: Number(value) }))
      .sort((a, b) => a.key.localeCompare(b.key))
      .slice(-10);
  }

  getMaxCategoryValue(): number {
    const entries = this.getCategoryEntries();
    return entries.length > 0 ? Math.max(...entries.map(e => e.value)) : 1;
  }

  getMaxSubmissionValue(): number {
    const entries = this.getSubmissionEntries();
    return entries.length > 0 ? Math.max(...entries.map(e => e.value)) : 1;
  }

  getCategoryLabel(cat: string): string {
    const map: Record<string, string> = {
      PROGRAMMING: 'Programming', DESIGN: 'Design',
      MANAGEMENT: 'Management', COMMUNICATION: 'Communication', OTHER: 'Other'
    };
    return map[cat] || cat;
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = { APPROVED: 'Approved', PENDING: 'Pending', REJECTED: 'Rejected' };
    return map[status] || status;
  }

  getTotalForStatus(): number {
    return this.getStatusEntries().reduce((sum, e) => sum + e.value, 0);
  }

  exportToPDF(): void {
    if (!this.analytics) return;
    this.exportingPdf = true;

    const a = this.analytics;
    const html = `<!DOCTYPE html><html><head><title>Global Analytics - Skill Evidence</title>
    <style>
      body { font-family: Arial, sans-serif; padding: 30px; color: #333; }
      h1 { color: #1e40af; margin-bottom: 5px; }
      p { color: #666; margin-bottom: 30px; }
      .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 30px; }
      .card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 15px; text-align: center; }
      .card .label { font-size: 12px; color: #64748b; font-weight: 600; }
      .card .value { font-size: 28px; font-weight: bold; margin-top: 5px; }
      table { width: 100%; border-collapse: collapse; margin-top: 20px; }
      th { background: #eff6ff; padding: 10px; text-align: left; font-size: 12px; text-transform: uppercase; }
      td { padding: 10px; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
    </style></head><body>
    <h1>Global Analytics — Skill Evidence</h1>
    <p>Generated on ${new Date().toLocaleDateString('fr-FR')}</p>
    <div class="grid">
      <div class="card"><div class="label">Total</div><div class="value" style="color:#1e40af">${a.totalCount}</div></div>
      <div class="card"><div class="label">Approved</div><div class="value" style="color:#16a34a">${a.approvedCount}</div></div>
      <div class="card"><div class="label">Pending</div><div class="value" style="color:#ca8a04">${a.pendingCount}</div></div>
      <div class="card"><div class="label">Rejected</div><div class="value" style="color:#dc2626">${a.rejectedCount}</div></div>
      <div class="card"><div class="label">Approval Rate</div><div class="value" style="color:#2563eb">${this.getApprovalRateDisplay()}</div></div>
      <div class="card"><div class="label">Avg Score</div><div class="value" style="color:#7c3aed">${this.getAverageScoreDisplay()}</div></div>
    </div>
    <h2>By Category</h2>
    <table><thead><tr><th>Category</th><th>Count</th></tr></thead><tbody>
    ${this.getCategoryEntries().map(e => `<tr><td>${this.getCategoryLabel(e.key)}</td><td>${e.value}</td></tr>`).join('')}
    </tbody></table>
    </body></html>`;

    const blob = new Blob([html], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const win = window.open(url, '_blank');
    if (win) win.onload = () => { win.print(); };
    setTimeout(() => { this.exportingPdf = false; }, 2000);
  }
}
