import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LearningPathService } from '../../../core/services/learning-path.service';
import { LearningPathResponse, LearningPathStatus } from '../../../core/models/learning-path.model';

@Component({
  selector: 'app-learning-path-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learning-path-management.component.html',
  styleUrl: './learning-path-management.component.scss'
})
export class LearningPathManagementComponent implements OnInit {
  paths: LearningPathResponse[] = [];
  filteredPaths: LearningPathResponse[] = [];
  isLoading = false;
  filterStatus = '';
  searchTerm = '';
  errorMessage = '';
  successMessage = '';
  activeTab: 'list' | 'analytics' = 'list';

  constructor(private learningPathService: LearningPathService) {}

  ngOnInit(): void {
    this.loadPaths();
  }

  loadPaths(): void {
    this.isLoading = true;
    this.learningPathService.getAll().subscribe({
      next: (data) => { this.paths = data; this.applyFilters(); this.isLoading = false; },
      error: () => { this.errorMessage = 'Erreur lors du chargement'; this.isLoading = false; }
    });
  }

  applyFilters(): void {
    this.filteredPaths = this.paths.filter(p => {
      const matchStatus = !this.filterStatus || p.status === this.filterStatus;
      const matchSearch = !this.searchTerm ||
        p.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.learnerName.toLowerCase().includes(this.searchTerm.toLowerCase());
      return matchStatus && matchSearch;
    });
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.searchTerm = '';
    this.filteredPaths = [...this.paths];
  }

  delete(id: number): void {
    if (!confirm('Supprimer ce parcours ?')) return;
    this.learningPathService.delete(id).subscribe({
      next: () => { this.successMessage = 'Parcours supprimé'; this.loadPaths(); },
      error: () => { this.errorMessage = 'Erreur lors de la suppression'; }
    });
  }

  // ---- Analytics ----
  getStats(): { total: number; enCours: number; termines: number; planifies: number; abandonnes: number; avgProgress: number } {
    const total = this.paths.length;
    return {
      total,
      enCours: this.paths.filter(p => p.status === 'EN_COURS').length,
      termines: this.paths.filter(p => p.status === 'TERMINE').length,
      planifies: this.paths.filter(p => p.status === 'PLANIFIE').length,
      abandonnes: this.paths.filter(p => p.status === 'ABANDONNE').length,
      avgProgress: total > 0 ? Math.round(this.paths.reduce((s, p) => s + p.progress, 0) / total) : 0
    };
  }

  getUniqueLearners(): number {
    return new Set(this.paths.map(p => p.learnerId)).size;
  }

  getCompletionRate(): string {
    const stats = this.getStats();
    if (stats.total === 0) return '0%';
    return ((stats.termines / stats.total) * 100).toFixed(1) + '%';
  }

  getStatusDistribution(): { status: string; count: number; color: string; label: string }[] {
    const stats = this.getStats();
    return [
      { status: 'EN_COURS', count: stats.enCours, color: '#3b82f6', label: 'En cours' },
      { status: 'TERMINE', count: stats.termines, color: '#22c55e', label: 'Terminés' },
      { status: 'PLANIFIE', count: stats.planifies, color: '#6b7280', label: 'Planifiés' },
      { status: 'ABANDONNE', count: stats.abandonnes, color: '#ef4444', label: 'Abandonnés' }
    ];
  }

  getProgressDistribution(): { label: string; count: number }[] {
    return [
      { label: '0%', count: this.paths.filter(p => p.progress === 0).length },
      { label: '1-25%', count: this.paths.filter(p => p.progress > 0 && p.progress <= 25).length },
      { label: '26-50%', count: this.paths.filter(p => p.progress > 25 && p.progress <= 50).length },
      { label: '51-75%', count: this.paths.filter(p => p.progress > 50 && p.progress <= 75).length },
      { label: '76-99%', count: this.paths.filter(p => p.progress > 75 && p.progress < 100).length },
      { label: '100%', count: this.paths.filter(p => p.progress === 100).length }
    ];
  }

  getMaxProgressCount(): number {
    return Math.max(...this.getProgressDistribution().map(d => d.count), 1);
  }

  downloadPdf(): void {
    const stats = this.getStats();
    const rows = this.filteredPaths.map(p =>
      `<tr><td>${p.title}</td><td>${p.learnerName}</td><td>${this.getStatusLabel(p.status)}</td><td>${p.progress}%</td><td>${p.startDate}</td></tr>`
    ).join('');
    const html = `<!DOCTYPE html><html><head><title>Learning Paths Analytics</title>
    <style>body{font-family:Arial,sans-serif;padding:20px}h1{color:#1e40af}
    .grid{display:grid;grid-template-columns:repeat(3,1fr);gap:15px;margin-bottom:25px}
    .card{background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:15px;text-align:center}
    .card .label{font-size:12px;color:#64748b;font-weight:600}
    .card .value{font-size:28px;font-weight:bold;margin-top:5px}
    table{width:100%;border-collapse:collapse;margin-top:20px}
    th{background:#eff6ff;padding:10px;text-align:left;font-size:12px;text-transform:uppercase}
    td{padding:10px;border-bottom:1px solid #e2e8f0;font-size:14px}</style></head><body>
    <h1>Analytics — Learning Paths</h1>
    <p>Généré le ${new Date().toLocaleDateString('fr-FR')}</p>
    <div class="grid">
      <div class="card"><div class="label">Total</div><div class="value" style="color:#1e40af">${stats.total}</div></div>
      <div class="card"><div class="label">Learners uniques</div><div class="value" style="color:#7c3aed">${this.getUniqueLearners()}</div></div>
      <div class="card"><div class="label">Taux de complétion</div><div class="value" style="color:#16a34a">${this.getCompletionRate()}</div></div>
      <div class="card"><div class="label">En cours</div><div class="value" style="color:#2563eb">${stats.enCours}</div></div>
      <div class="card"><div class="label">Terminés</div><div class="value" style="color:#16a34a">${stats.termines}</div></div>
      <div class="card"><div class="label">Progression moy.</div><div class="value" style="color:#d97706">${stats.avgProgress}%</div></div>
    </div>
    <h2>Liste des parcours</h2>
    <table><thead><tr><th>Titre</th><th>Apprenant</th><th>Statut</th><th>Progression</th><th>Début</th></tr></thead>
    <tbody>${rows}</tbody></table></body></html>`;
    const blob = new Blob([html], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const win = window.open(url, '_blank');
    if (win) win.onload = () => win.print();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'EN_COURS':  return 'bg-blue-100 text-blue-800';
      case 'TERMINE':   return 'bg-green-100 text-green-800';
      case 'ABANDONNE': return 'bg-red-100 text-red-800';
      default:          return 'bg-gray-100 text-gray-700';
    }
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PLANIFIE: 'Planifié', EN_COURS: 'En cours', TERMINE: 'Terminé', ABANDONNE: 'Abandonné'
    };
    return map[status] || status;
  }
}
