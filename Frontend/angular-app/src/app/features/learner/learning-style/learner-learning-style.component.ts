import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LearningStyleService } from '../../../core/services/learning-style.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  LearningStylePreferenceResponse,
  LearningStyleType
} from '../../../core/models/learning-style.model';

@Component({
  selector: 'app-learner-learning-style',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-learning-style.component.html',
  styleUrl: './learner-learning-style.component.scss'
})
export class LearnerLearningStyleComponent implements OnInit {
  preference: LearningStylePreferenceResponse | null = null;
  loading = false;
  showModal = false;
  isEditMode = false;
  successMessage = '';
  errorMessage = '';

  learningStyleTypes: LearningStyleType[] = ['VISUAL', 'AUDITORY', 'READ_WRITE', 'KINESTHETIC', 'MULTIMODAL'];

  currentPreference: {
    preferredStyle: LearningStyleType;
    videoPreferred: boolean;
    textPreferred: boolean;
    practicalWorkPreferred: boolean;
  } = { preferredStyle: 'VISUAL', videoPreferred: false, textPreferred: false, practicalWorkPreferred: false };

  styleDescriptions: Record<LearningStyleType, string> = {
    VISUAL: 'You learn best through images, diagrams, charts and visual representations.',
    AUDITORY: 'You learn best through listening, discussions and verbal explanations.',
    READ_WRITE: 'You learn best through reading and writing — notes, lists and text.',
    KINESTHETIC: 'You learn best through hands-on experience, practice and movement.',
    MULTIMODAL: 'You adapt to multiple learning styles depending on the context.'
  };

  private currentUser: any = null;

  constructor(
    private learningStyleService: LearningStyleService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadPreference();
  }

  loadPreference(): void {
    if (!this.currentUser?.userId) return;
    this.loading = true;
    const userId = Number(this.currentUser.userId);
    this.learningStyleService.exists(userId).subscribe({
      next: (exists) => {
        if (!exists) {
          this.preference = null;
          this.loading = false;
          return;
        }
        this.learningStyleService.getByLearner(userId).subscribe({
          next: (data) => { this.preference = data; this.loading = false; },
          error: () => { this.preference = null; this.loading = false; }
        });
      },
      error: () => { this.preference = null; this.loading = false; }
    });
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentPreference = { preferredStyle: 'VISUAL', videoPreferred: false, textPreferred: false, practicalWorkPreferred: false };
    this.successMessage = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(): void {
    if (!this.preference) return;
    this.isEditMode = true;
    this.currentPreference = {
      preferredStyle: this.preference.preferredStyle,
      videoPreferred: this.preference.videoPreferred,
      textPreferred: this.preference.textPreferred,
      practicalWorkPreferred: this.preference.practicalWorkPreferred
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

  savePreference(): void {
    // Vérification défensive
    if (!this.currentUser?.userId) {
      this.errorMessage = 'Utilisateur non connecté. Veuillez vous reconnecter.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const payload = {
      preferredStyle: this.currentPreference.preferredStyle,
      videoPreferred: this.currentPreference.videoPreferred,
      textPreferred: this.currentPreference.textPreferred,
      practicalWorkPreferred: this.currentPreference.practicalWorkPreferred,
      learnerId: Number(this.currentUser.userId),
      learnerName: this.currentUser.firstName || ''
    };

    this.learningStyleService.save(payload).subscribe({
      next: (data) => {
        this.preference = data;
        this.successMessage = this.isEditMode ? 'Préférences mises à jour !' : 'Préférences enregistrées !';
        this.loading = false;
        this.showModal = false;
        // Recharger pour s'assurer que les données sont à jour
        this.loadPreference();
      },
      error: (err) => {
        console.error('Save error:', err);
        this.errorMessage = err.error?.message || err.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  deletePreference(): void {
    if (!this.currentUser?.userId) return;
    if (!confirm('Supprimer vos préférences de style d\'apprentissage ?')) return;
    this.learningStyleService.delete(Number(this.currentUser.userId)).subscribe({
      next: () => { this.preference = null; this.successMessage = 'Préférences supprimées'; },
      error: () => { this.errorMessage = 'Erreur lors de la suppression'; }
    });
  }

  getStyleLabel(style: LearningStyleType): string {
    const map: Record<LearningStyleType, string> = {
      VISUAL: 'Visual', AUDITORY: 'Auditory',
      READ_WRITE: 'Read/Write', KINESTHETIC: 'Kinesthetic', MULTIMODAL: 'Multimodal'
    };
    return map[style] || style;
  }

  getStyleIcon(style: LearningStyleType): string {
    const map: Record<LearningStyleType, string> = {
      VISUAL: 'visibility', AUDITORY: 'hearing',
      READ_WRITE: 'menu_book', KINESTHETIC: 'sports_handball', MULTIMODAL: 'auto_awesome'
    };
    return map[style] || 'psychology';
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }
}
