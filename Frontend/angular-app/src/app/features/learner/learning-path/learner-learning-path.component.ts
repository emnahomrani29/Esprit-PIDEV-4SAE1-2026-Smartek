import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LearningPathService } from '../../../core/services/learning-path.service';
import { LearningStyleService } from '../../../core/services/learning-style.service';
import { AuthService } from '../../../core/services/auth.service';
import { LearningPathResponse, LearningPathStatus } from '../../../core/models/learning-path.model';
import { LearningStylePreferenceResponse, LearningStyleType } from '../../../core/models/learning-style.model';

@Component({
  selector: 'app-learner-learning-path',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-learning-path.component.html',
  styleUrl: './learner-learning-path.component.scss'
})
export class LearnerLearningPathComponent implements OnInit {
  paths: LearningPathResponse[] = [];
  filteredPaths: LearningPathResponse[] = [];
  isLoading = false;
  showModal = false;
  showDetailModal = false;
  isEditMode = false;
  selectedPath: LearningPathResponse | null = null;
  successMessage = '';
  errorMessage = '';

  // Filters
  filterStatus = '';
  searchTerm = '';

  // Quick progress update
  quickProgressMap: Record<number, number> = {};

  // Learning style recommendation
  learningStyle: LearningStylePreferenceResponse | null = null;

  form: {
    pathId?: number;
    title: string;
    description: string;
    status: LearningPathStatus;
    startDate: string;
    endDate: string;
    progress: number;
  } = { title: '', description: '', status: 'PLANIFIE', startDate: '', endDate: '', progress: 0 };

  private currentUser: any = null;

  // Recommendations based on learning style
  readonly styleRecommendations: Record<LearningStyleType, { icon: string; tips: string[] }> = {
    VISUAL: {
      icon: 'visibility',
      tips: [
        'Créez des mind maps pour visualiser vos parcours',
        'Utilisez des diagrammes et schémas pour structurer vos objectifs',
        'Regardez des vidéos tutoriels pour chaque étape',
        'Utilisez des codes couleur pour organiser vos notes'
      ]
    },
    AUDITORY: {
      icon: 'hearing',
      tips: [
        'Écoutez des podcasts liés à vos objectifs d\'apprentissage',
        'Expliquez vos parcours à voix haute pour mieux les mémoriser',
        'Participez à des discussions et groupes d\'étude',
        'Enregistrez vos notes et réécoutez-les'
      ]
    },
    READ_WRITE: {
      icon: 'menu_book',
      tips: [
        'Rédigez des résumés détaillés de chaque étape',
        'Tenez un journal de progression écrit',
        'Lisez des articles et documentations approfondies',
        'Créez des listes de tâches et objectifs écrits'
      ]
    },
    KINESTHETIC: {
      icon: 'sports_handball',
      tips: [
        'Pratiquez immédiatement ce que vous apprenez',
        'Divisez vos parcours en petites tâches pratiques',
        'Faites des projets concrets pour chaque compétence',
        'Alternez apprentissage et mise en pratique régulièrement'
      ]
    },
    MULTIMODAL: {
      icon: 'auto_awesome',
      tips: [
        'Combinez plusieurs méthodes selon le sujet',
        'Adaptez votre approche selon la complexité du contenu',
        'Utilisez à la fois vidéos, textes et pratique',
        'Soyez flexible dans votre organisation de parcours'
      ]
    }
  };

  constructor(
    private learningPathService: LearningPathService,
    private learningStyleService: LearningStyleService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadPaths();
    this.loadLearningStyle();
  }

  loadPaths(): void {
    if (!this.currentUser?.userId) return;
    this.isLoading = true;
    this.learningPathService.getByLearner(this.currentUser.userId).subscribe({
      next: (data) => {
        this.paths = data;
        // Init quick progress map
        data.forEach(p => { this.quickProgressMap[p.pathId] = p.progress; });
        this.applyFilters();
        this.isLoading = false;
      },
      error: () => { this.errorMessage = 'Erreur lors du chargement'; this.isLoading = false; }
    });
  }

  loadLearningStyle(): void {
    if (!this.currentUser?.userId) return;
    this.learningStyleService.exists(Number(this.currentUser.userId)).subscribe({
      next: (exists) => {
        if (exists) {
          this.learningStyleService.getByLearner(Number(this.currentUser.userId)).subscribe({
            next: (data) => { this.learningStyle = data; },
            error: () => {}
          });
        }
      },
      error: () => {}
    });
  }

  // ---- Filters ----
  applyFilters(): void {
    this.filteredPaths = this.paths.filter(p => {
      const matchStatus = !this.filterStatus || p.status === this.filterStatus;
      const matchSearch = !this.searchTerm ||
        p.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (p.description || '').toLowerCase().includes(this.searchTerm.toLowerCase());
      return matchStatus && matchSearch;
    });
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.searchTerm = '';
    this.filteredPaths = [...this.paths];
  }

  // ---- Quick progress update ----
  updateProgressQuick(path: LearningPathResponse): void {
    const newProgress = this.quickProgressMap[path.pathId];
    if (newProgress === path.progress) return;

    const payload = {
      title: path.title,
      description: path.description,
      learnerId: path.learnerId,
      learnerName: path.learnerName,
      status: path.status,
      startDate: path.startDate,
      endDate: path.endDate,
      progress: newProgress
    };

    this.learningPathService.update(path.pathId, payload).subscribe({
      next: (updated) => {
        path.progress = updated.progress;
        this.successMessage = `Progression mise à jour : ${newProgress}%`;
        setTimeout(() => { this.successMessage = ''; }, 3000);
      },
      error: () => { this.errorMessage = 'Erreur lors de la mise à jour'; }
    });
  }

  // ---- Stats ----
  getCountByStatus(status: LearningPathStatus): number {
    return this.paths.filter(p => p.status === status).length;
  }

  getAverageProgress(): number {
    if (this.paths.length === 0) return 0;
    return Math.round(this.paths.reduce((sum, p) => sum + p.progress, 0) / this.paths.length);
  }

  // ---- Recommendations ----
  getRecommendations(): string[] {
    if (!this.learningStyle) return [];
    return this.styleRecommendations[this.learningStyle.preferredStyle]?.tips || [];
  }

  getRecommendationIcon(): string {
    if (!this.learningStyle) return 'lightbulb';
    return this.styleRecommendations[this.learningStyle.preferredStyle]?.icon || 'lightbulb';
  }

  getStyleLabel(style: LearningStyleType): string {
    const map: Record<LearningStyleType, string> = {
      VISUAL: 'Visual', AUDITORY: 'Auditory',
      READ_WRITE: 'Read/Write', KINESTHETIC: 'Kinesthetic', MULTIMODAL: 'Multimodal'
    };
    return map[style] || style;
  }

  // ---- Add/Edit Modal ----
  openAddModal(): void {
    this.isEditMode = false;
    this.form = {
      title: '', description: '', status: 'PLANIFIE',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '', progress: 0
    };
    this.successMessage = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(p: LearningPathResponse): void {
    this.isEditMode = true;
    this.form = {
      pathId: p.pathId,
      title: p.title,
      description: p.description || '',
      status: p.status,
      startDate: p.startDate,
      endDate: p.endDate || '',
      progress: p.progress
    };
    this.successMessage = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.successMessage = '';
    this.errorMessage = '';
  }

  openDetailModal(p: LearningPathResponse): void {
    this.selectedPath = p;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedPath = null;
  }

  savePath(): void {
    if (!this.form.title.trim()) { this.errorMessage = 'Le titre est obligatoire'; return; }
    if (!this.form.startDate)    { this.errorMessage = 'La date de début est obligatoire'; return; }

    this.isLoading = true;
    this.errorMessage = '';

    const payload = {
      title: this.form.title,
      description: this.form.description || undefined,
      learnerId: this.currentUser.userId,
      learnerName: this.currentUser.firstName,
      status: this.form.status,
      startDate: this.form.startDate,
      endDate: this.form.endDate || undefined,
      progress: this.form.progress
    };

    const obs = this.isEditMode && this.form.pathId
      ? this.learningPathService.update(this.form.pathId, payload)
      : this.learningPathService.create(payload);

    obs.subscribe({
      next: () => {
        this.successMessage = this.isEditMode ? 'Parcours modifié' : 'Parcours créé';
        this.isLoading = false;
        this.showModal = false;
        this.loadPaths();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la sauvegarde';
        this.isLoading = false;
      }
    });
  }

  deletePath(id: number): void {
    if (!confirm('Supprimer ce parcours ?')) return;
    this.learningPathService.delete(id).subscribe({
      next: () => { this.successMessage = 'Parcours supprimé'; this.loadPaths(); },
      error: () => { this.errorMessage = 'Erreur lors de la suppression'; }
    });
  }

  // ---- Helpers ----
  getStatusColor(status: string): string {
    switch (status) {
      case 'EN_COURS':  return 'bg-blue-100 text-blue-800';
      case 'TERMINE':   return 'bg-green-100 text-green-800';
      case 'ABANDONNE': return 'bg-red-100 text-red-800';
      default:          return 'bg-gray-100 text-gray-700';
    }
  }

  getStatusTextClass(status: string): string {
    switch (status) {
      case 'EN_COURS':  return 'text-blue-600';
      case 'TERMINE':   return 'text-green-600';
      case 'ABANDONNE': return 'text-red-600';
      default:          return 'text-gray-600';
    }
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PLANIFIE: 'Planifié', EN_COURS: 'En cours', TERMINE: 'Terminé', ABANDONNE: 'Abandonné'
    };
    return map[status] || status;
  }
}
