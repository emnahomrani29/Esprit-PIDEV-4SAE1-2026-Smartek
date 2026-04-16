import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CourseService } from '../../../core/services/course.service';
import { ChapterService } from '../../../core/services/chapter.service';
import { LiveSessionService } from '../../../core/services/live-session.service';
import { AuthService } from '../../../core/services/auth.service';
import { Course, CourseCreateRequest } from '../../../core/models/course.model';
import { DeliveryMode, LiveSessionRequest, LiveSessionResponse, SessionStatus } from '../../../core/models/live-session.model';
import { Chapter, ChapterCreateRequest } from '../../../core/models/chapter.model';
import { forkJoin } from 'rxjs';
import { retry, delay } from 'rxjs/operators';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';

interface ChapterFormData {
  title: string;
  description: string;
  orderIndex: number;
  pdfFile: File | null;
}

@Component({
  selector: 'app-trainer-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, PageHeaderComponent],
  templateUrl: './trainer-courses.component.html',
  styleUrl: './trainer-courses.component.scss'
})
export class TrainerCoursesComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  courseForm: FormGroup;
  chapterForm: FormGroup;
  isEditMode = false;
  selectedCourseId: number | null = null;
  showModal = false;
  showChapterModal = false;
  loading = false;
  currentUserId: number | null = null;
  chapters: ChapterFormData[] = [];
  selectedChapterFile: File | null = null;
  editingChapterIndex: number | null = null;
  
  // Recherche et tri
  searchTerm: string = '';
  sortBy: 'name' | 'duration' | 'chapters' = 'name';
  sortOrder: 'asc' | 'desc' = 'asc';
  
  // Chapter management modal
  showChaptersListModal = false;
  currentCourseForChapters: Course | null = null;
  courseChapters: Chapter[] = [];
  loadingChapters = false;

  // Delivery mode
  DeliveryMode = DeliveryMode;

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private chapterService: ChapterService,
    private liveSessionService: LiveSessionService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      content: ['', Validators.required],
      duration: ['', Validators.required],
      trainerId: [null],
      deliveryMode: [DeliveryMode.PRESENTIEL, Validators.required]
    });

    this.chapterForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      orderIndex: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    this.currentUserId = userInfo?.userId || null;
    this.loadCourses();
  }

  getCourseStats() {
    return [
      { label: 'Total Courses', value: this.courses.length },
      { label: 'Chapters', value: this.courses.reduce((sum, c) => sum + (c.chapters?.length || 0), 0) }
    ];
  }

  loadCourses(): void {
    this.loading = true;
    if (this.currentUserId) {
      this.courseService.getCoursesByTrainer(this.currentUserId).subscribe({
        next: (courses) => {
          this.courses = courses;
          this.loading = false;
          this.applyFiltersAndSort();
        },
        error: (error) => {
          console.error('Error loading courses:', error);
          this.loading = false;
        }
      });
    }
  }

  // Recherche et tri
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.applyFiltersAndSort();
  }

  onSortChange(sortBy: 'name' | 'duration' | 'chapters'): void {
    if (this.sortBy === sortBy) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortOrder = 'asc';
    }
    this.applyFiltersAndSort();
  }

  applyFiltersAndSort(): void {
    // Filtrer par recherche
    let filtered = this.courses.filter(c => 
      c.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (c.content && c.content.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );

    // Trier
    filtered.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'duration':
          const durationA = parseInt(a.duration) || 0;
          const durationB = parseInt(b.duration) || 0;
          comparison = durationA - durationB;
          break;
        case 'chapters':
          const chaptersA = a.chapters?.length || 0;
          const chaptersB = b.chapters?.length || 0;
          comparison = chaptersA - chaptersB;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredCourses = filtered;
  }

  get displayedCourses(): Course[] {
    return this.filteredCourses.length > 0 || this.searchTerm ? this.filteredCourses : this.courses;
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedCourseId = null;
    this.courseForm.reset();
    this.chapters = [];
    this.showModal = true;
  }

  openEditModal(course: Course): void {
    this.isEditMode = true;
    this.selectedCourseId = course.courseId || null;
    this.courseForm.patchValue({
      title: course.title,
      content: course.content,
      duration: course.duration,
      trainerId: course.trainerId,
      deliveryMode: course.deliveryMode || DeliveryMode.PRESENTIEL
    });
    this.chapters = [];
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.courseForm.reset();
    this.chapters = [];
  }

  openChapterModal(): void {
    this.editingChapterIndex = null;
    this.selectedChapterFile = null;
    this.chapterForm.reset({
      orderIndex: this.chapters.length + 1
    });
    this.showChapterModal = true;
  }

  openEditChapterModal(index: number): void {
    this.editingChapterIndex = index;
    const chapter = this.chapters[index];
    this.selectedChapterFile = chapter.pdfFile;
    this.chapterForm.patchValue({
      title: chapter.title,
      description: chapter.description,
      orderIndex: chapter.orderIndex
    });
    this.showChapterModal = true;
  }

  closeChapterModal(): void {
    this.showChapterModal = false;
    this.chapterForm.reset();
    this.selectedChapterFile = null;
    this.editingChapterIndex = null;
  }

  onChapterModalFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      if (file.type !== 'application/pdf') {
        alert('Seuls les fichiers PDF sont acceptés');
        input.value = '';
        return;
      }

      if (file.size > 10 * 1024 * 1024) {
        alert('Le fichier ne doit pas dépasser 10MB');
        input.value = '';
        return;
      }

      this.selectedChapterFile = file;
    }
  }

  saveChapter(): void {
    if (this.chapterForm.invalid) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const chapterData: ChapterFormData = {
      title: this.chapterForm.value.title,
      description: this.chapterForm.value.description,
      orderIndex: this.chapterForm.value.orderIndex,
      pdfFile: this.selectedChapterFile
    };

    if (this.editingChapterIndex !== null) {
      // Mode édition
      this.chapters[this.editingChapterIndex] = chapterData;
    } else {
      // Mode création
      this.chapters.push(chapterData);
    }

    this.closeChapterModal();
  }

  addChapter(): void {
    this.openChapterModal();
  }

  removeChapter(index: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce chapitre?')) {
      this.chapters.splice(index, 1);
      // Réorganiser les orderIndex
      this.chapters.forEach((chapter, idx) => {
        chapter.orderIndex = idx + 1;
      });
    }
  }

  onChapterFileSelected(event: Event, index: number): void {
    // Cette méthode n'est plus utilisée car on utilise le modal
  }

  onSubmit(): void {
    if (this.courseForm.invalid || !this.currentUserId) return;

    // Valider les chapitres
    for (const chapter of this.chapters) {
      if (!chapter.title.trim()) {
        alert('Tous les chapitres doivent avoir un titre');
        return;
      }
    }

    this.loading = true;
    
    if (this.isEditMode && this.selectedCourseId) {
      const updateData: CourseCreateRequest = this.courseForm.value;
      this.courseService.updateCourse(this.selectedCourseId, updateData).subscribe({
        next: () => {
          if (this.chapters.length > 0 && this.selectedCourseId) {
            this.createChaptersForCourse(this.selectedCourseId);
          } else {
            this.loadCourses();
            this.closeModal();
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Error updating course:', error);
          this.loading = false;
        }
      });
    } else {
      const createData: CourseCreateRequest = {
        ...this.courseForm.value,
        trainerId: this.currentUserId
      };
      this.courseService.createCourse(createData).subscribe({
        next: (course) => {
          if (this.chapters.length > 0 && course.courseId) {
            this.createChaptersForCourse(course.courseId);
          } else {
            this.loadCourses();
            this.closeModal();
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Error creating course:', error);
          this.loading = false;
        }
      });
    }
  }

  private createChaptersForCourse(courseId: number): void {
    const chapterCreations = this.chapters.map(chapter => {
      const chapterData = {
        title: chapter.title,
        description: chapter.description,
        orderIndex: chapter.orderIndex
      };
      return this.chapterService.createChapter(courseId, chapterData);
    });

    forkJoin(chapterCreations).subscribe({
      next: (createdChapters) => {
        // Upload PDFs si présents
        const pdfUploads = createdChapters
          .map((chapter, index) => {
            const pdfFile = this.chapters[index].pdfFile;
            if (pdfFile && chapter.chapterId) {
              return this.chapterService.uploadPdf(courseId, chapter.chapterId, pdfFile);
            }
            return null;
          })
          .filter(upload => upload !== null);

        if (pdfUploads.length > 0) {
          forkJoin(pdfUploads).subscribe({
            next: () => {
              this.loadCourses();
              this.closeModal();
              this.loading = false;
              alert('Cours et chapitres créés avec succès!');
            },
            error: (error) => {
              console.error('Error uploading PDFs:', error);
              this.loadCourses();
              this.closeModal();
              this.loading = false;
              alert('Cours créé mais erreur lors de l\'upload de certains PDFs');
            }
          });
        } else {
          this.loadCourses();
          this.closeModal();
          this.loading = false;
          alert('Cours et chapitres créés avec succès!');
        }
      },
      error: (error) => {
        console.error('Error creating chapters:', error);
        this.loadCourses();
        this.closeModal();
        this.loading = false;
        alert('Cours créé mais erreur lors de la création des chapitres');
      }
    });
  }

  deleteCourse(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce cours et tous ses chapitres?')) {
      this.loading = true;
      this.courseService.deleteCourse(id).subscribe({
        next: () => {
          this.loadCourses();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error deleting course:', error);
          this.loading = false;
        }
      });
    }
  }

  // Chapter management methods
  openChaptersModal(course: Course): void {
    this.currentCourseForChapters = course;
    this.showChaptersListModal = true;
    this.loadChaptersForCourse(course.courseId!);
  }

  closeChaptersModal(): void {
    this.showChaptersListModal = false;
    this.currentCourseForChapters = null;
    this.courseChapters = [];
  }

  loadChaptersForCourse(courseId: number): void {
    this.loadingChapters = true;
    this.chapterService.getChaptersByCourse(courseId).subscribe({
      next: (chapters) => {
        this.courseChapters = chapters;
        this.loadingChapters = false;
      },
      error: (error) => {
        console.error('Error loading chapters:', error);
        this.loadingChapters = false;
      }
    });
  }

  openAddChapterModal(): void {
    if (!this.currentCourseForChapters) return;
    this.editingChapterIndex = null;
    this.selectedChapterFile = null;
    this.chapterForm.reset({
      orderIndex: this.courseChapters.length + 1
    });
    this.showChapterModal = true;
  }

  openEditChapterFromList(chapter: Chapter): void {
    this.editingChapterIndex = null;
    this.selectedChapterFile = null;
    this.chapterForm.patchValue({
      title: chapter.title,
      description: chapter.description,
      orderIndex: chapter.orderIndex
    });
    this.showChapterModal = true;
  }

  saveChapterFromModal(): void {
    if (this.chapterForm.invalid || !this.currentCourseForChapters?.courseId) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const courseId = this.currentCourseForChapters.courseId;
    const chapterData: ChapterCreateRequest = {
      title: this.chapterForm.value.title,
      description: this.chapterForm.value.description,
      orderIndex: this.chapterForm.value.orderIndex
    };

    this.loading = true;
    this.chapterService.createChapter(courseId, chapterData).subscribe({
      next: (createdChapter) => {
        if (this.selectedChapterFile && createdChapter.chapterId) {
          this.chapterService.uploadPdf(courseId, createdChapter.chapterId, this.selectedChapterFile).subscribe({
            next: () => {
              this.loadChaptersForCourse(courseId);
              this.closeChapterModal();
              this.loading = false;
              alert('Chapitre créé avec succès!');
            },
            error: (error) => {
              console.error('Error uploading PDF:', error);
              this.loadChaptersForCourse(courseId);
              this.closeChapterModal();
              this.loading = false;
              alert('Chapitre créé mais erreur lors de l\'upload du PDF');
            }
          });
        } else {
          this.loadChaptersForCourse(courseId);
          this.closeChapterModal();
          this.loading = false;
          alert('Chapitre créé avec succès!');
        }
      },
      error: (error) => {
        console.error('Error creating chapter:', error);
        this.loading = false;
        alert('Erreur lors de la création du chapitre');
      }
    });
  }

  deleteChapterFromList(chapterId: number): void {
    if (!this.currentCourseForChapters?.courseId) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer ce chapitre?')) {
      this.loadingChapters = true;
      this.chapterService.deleteChapter(this.currentCourseForChapters.courseId, chapterId).subscribe({
        next: () => {
          this.loadChaptersForCourse(this.currentCourseForChapters!.courseId!);
          alert('Chapitre supprimé avec succès!');
        },
        error: (error) => {
          console.error('Error deleting chapter:', error);
          this.loadingChapters = false;
          alert('Erreur lors de la suppression du chapitre');
        }
      });
    }
  }

  // Navigate to live sessions management
  manageLiveSessions(courseId: number): void {
    this.currentCourseForSessions = this.courses.find(c => c.courseId === courseId) || null;
    if (this.currentCourseForSessions) {
      this.showLiveSessionsModal = true;
      // Utiliser directement les sessions du cours déjà chargé
      this.liveSessions = this.currentCourseForSessions.liveSessions || [];
      this.loadingLiveSessions = false;
      this.cdr.detectChanges();
      // Recharger en arrière-plan pour synchroniser
      this.loadLiveSessionsForCourse(courseId);
    }
  }

  // Live Sessions Modal Management
  showLiveSessionsModal = false;
  currentCourseForSessions: Course | null = null;
  liveSessions: LiveSessionResponse[] = [];
  loadingLiveSessions = false;
  showSessionForm = false;
  isEditingSession = false;
  selectedSession: LiveSessionResponse | null = null;
  sessionForm: any = {
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    maxParticipants: null
  };

  closeLiveSessionsModal(): void {
    this.showLiveSessionsModal = false;
    this.currentCourseForSessions = null;
    this.liveSessions = [];
    this.closeSessionForm();
  }

  loadLiveSessionsForCourse(courseId: number): void {
    this.loadingLiveSessions = true;
    // Essayer d'abord via l'endpoint dédié
    this.liveSessionService.getSessionsByCourseId(courseId).pipe(
      retry({ count: 2, delay: 2000 })
    ).subscribe({
      next: (sessions) => {
        if (sessions.length > 0 || this.liveSessions.length === 0) {
          this.liveSessions = sessions;
        }
        this.loadingLiveSessions = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.warn('Endpoint sessions indisponible, utilisation des données du cours:', error);
        // Fallback: recharger le cours complet pour avoir les sessions à jour
        this.courseService.getCoursesByTrainer(this.currentUserId!).subscribe({
          next: (courses) => {
            this.courses = courses;
            const updatedCourse = courses.find(c => c.courseId === courseId);
            if (updatedCourse?.liveSessions) {
              this.liveSessions = updatedCourse.liveSessions;
              this.currentCourseForSessions = updatedCourse;
            }
            this.loadingLiveSessions = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.loadingLiveSessions = false;
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  openSessionForm(): void {
    this.isEditingSession = false;
    this.selectedSession = null;
    this.sessionForm = {
      title: '',
      description: '',
      startTime: '',
      endTime: '',
      maxParticipants: null
    };
    this.showSessionForm = true;
    this.cdr.detectChanges();
    
    // Scroll to top of modal to ensure form is visible
    setTimeout(() => {
      const modalContent = document.querySelector('.max-h-\\[90vh\\]');
      if (modalContent) {
        modalContent.scrollTop = 0;
      }
    }, 50);
  }

  openEditSessionForm(session: LiveSessionResponse): void {
    this.isEditingSession = true;
    this.selectedSession = session;
    this.sessionForm = {
      title: session.title,
      description: session.description || '',
      startTime: this.formatDateTimeForInput(session.startTime),
      endTime: this.formatDateTimeForInput(session.endTime),
      maxParticipants: session.maxParticipants || null
    };
    this.showSessionForm = true;
    this.cdr.detectChanges();
    
    // Scroll to top of modal to ensure form is visible
    setTimeout(() => {
      const modalContent = document.querySelector('.max-h-\\[90vh\\]');
      if (modalContent) {
        modalContent.scrollTop = 0;
      }
    }, 50);
  }

  closeSessionForm(): void {
    this.showSessionForm = false;
    this.isEditingSession = false;
    this.selectedSession = null;
  }

  formatDateTimeForInput(dateTime: string): string {
    if (!dateTime) return '';
    return dateTime.substring(0, 16);
  }

  saveSession(): void {
    if (!this.sessionForm.title || !this.sessionForm.startTime || !this.sessionForm.endTime) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (this.sessionForm.endTime <= this.sessionForm.startTime) {
      alert('La date de fin doit être après la date de début');
      return;
    }

    if (!this.currentCourseForSessions?.courseId || !this.currentUserId) {
      alert('Erreur: informations manquantes');
      return;
    }

    // Convertir les dates au format ISO
    const startTime = new Date(this.sessionForm.startTime).toISOString();
    const endTime = new Date(this.sessionForm.endTime).toISOString();

    const request: LiveSessionRequest = {
      courseId: this.currentCourseForSessions.courseId,
      title: this.sessionForm.title,
      description: this.sessionForm.description || undefined,
      trainerId: this.currentUserId,
      startTime: startTime,
      endTime: endTime,
      maxParticipants: this.sessionForm.maxParticipants || undefined
    };

    if (this.isEditingSession && this.selectedSession?.sessionId) {
      // Update existing session
      this.liveSessionService.updateSession(this.selectedSession.sessionId, request).subscribe({
        next: (response) => {
          console.log('Session updated:', response);
          // Mettre à jour localement
          const idx = this.liveSessions.findIndex(s => s.sessionId === response.sessionId);
          if (idx !== -1) {
            this.liveSessions = [
              ...this.liveSessions.slice(0, idx),
              response,
              ...this.liveSessions.slice(idx + 1)
            ];
          }
          this.cdr.detectChanges();
          this.closeSessionForm();
          // Ne pas recharger — la liste locale est déjà à jour
        },
        error: (error) => {
          console.error('Error updating session:', error);
          alert('Erreur lors de la mise à jour de la session');
        }
      });
    } else {
      // Create new session
      this.liveSessionService.createSession(request).subscribe({
        next: (response) => {
          console.log('Session created:', response);
          // Ajouter immédiatement à la liste locale
          this.liveSessions = [...this.liveSessions, response];
          // Mettre à jour aussi dans le cours local
          if (this.currentCourseForSessions) {
            this.currentCourseForSessions.liveSessions = [...this.liveSessions];
            const idx = this.courses.findIndex(c => c.courseId === this.currentCourseForSessions!.courseId);
            if (idx !== -1) {
              this.courses[idx] = { ...this.currentCourseForSessions };
            }
          }
          this.cdr.detectChanges();
          this.closeSessionForm();
        },
        error: (error) => {
          console.error('Error creating session:', error);
          alert('Erreur lors de la création de la session');
        }
      });
    }
  }

  deleteSession(sessionId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette session ?')) {
      this.liveSessions = this.liveSessions.filter(s => s.sessionId !== sessionId);
      // Mettre à jour le cours local
      if (this.currentCourseForSessions) {
        this.currentCourseForSessions.liveSessions = [...this.liveSessions];
        const idx = this.courses.findIndex(c => c.courseId === this.currentCourseForSessions!.courseId);
        if (idx !== -1) this.courses[idx] = { ...this.currentCourseForSessions };
      }
      this.cdr.detectChanges();
      this.liveSessionService.deleteSession(sessionId).subscribe({
        next: () => console.log('Session deleted:', sessionId),
        error: (error) => {
          console.error('Error deleting session:', error);
          this.loadLiveSessionsForCourse(this.currentCourseForSessions!.courseId!);
          alert('Erreur lors de la suppression de la session');
        }
      });
    }
  }

  joinSession(roomId: string): void {
    // Ouvrir la salle de visioconférence
    this.router.navigate(['/video-conference', roomId]);
  }

  startSession(session: LiveSessionResponse): void {
    this.liveSessionService.updateSessionStatus(session.sessionId, SessionStatus.ONGOING).subscribe({
      next: (updated) => {
        // Mettre à jour localement
        const idx = this.liveSessions.findIndex(s => s.sessionId === session.sessionId);
        if (idx !== -1) {
          this.liveSessions = [
            ...this.liveSessions.slice(0, idx),
            { ...this.liveSessions[idx], status: SessionStatus.ONGOING },
            ...this.liveSessions.slice(idx + 1)
          ];
          this.cdr.detectChanges();
        }
        // Rejoindre automatiquement après démarrage
        this.router.navigate(['/video-conference', session.roomId]);
      },
      error: (err) => {
        console.error('Error starting session:', err);
        alert('Erreur lors du démarrage de la session');
      }
    });
  }

  endSession(session: LiveSessionResponse): void {
    if (!confirm(`Terminer la session "${session.title}" ?`)) return;

    this.liveSessionService.updateSessionStatus(session.sessionId, SessionStatus.COMPLETED).subscribe({
      next: () => {
        // Mettre à jour localement
        const idx = this.liveSessions.findIndex(s => s.sessionId === session.sessionId);
        if (idx !== -1) {
          this.liveSessions = [
            ...this.liveSessions.slice(0, idx),
            { ...this.liveSessions[idx], status: SessionStatus.COMPLETED },
            ...this.liveSessions.slice(idx + 1)
          ];
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error ending session:', err);
        alert('Erreur lors de la clôture de la session');
      }
    });
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'SCHEDULED': 'À venir',
      'ONGOING': 'En direct',
      'COMPLETED': 'Terminée',
      'CANCELLED': 'Annulée'
    };
    return labels[status] || status;
  }

  formatDateTime(dateTime: string): string {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSessionDuration(startTime: string, endTime: string): string {
    return this.liveSessionService.getSessionDuration(startTime, endTime);
  }

  getCurrentDateTime(): string {
    const now = new Date();
    // Format: YYYY-MM-DDTHH:mm
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
