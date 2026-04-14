import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChapterService } from '../../../core/services/chapter.service';
import { CourseService } from '../../../core/services/course.service';
import { Chapter, ChapterCreateRequest } from '../../../core/models/chapter.model';
import { Course } from '../../../core/models/course.model';

@Component({
  selector: 'app-chapter-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './chapter-management.component.html',
  styleUrl: './chapter-management.component.scss'
})
export class ChapterManagementComponent implements OnInit {
  chapters: Chapter[] = [];
  course: Course | null = null;
  courseId: number | null = null;
  chapterForm: FormGroup;
  isEditMode = false;
  selectedChapterId: number | null = null;
  showModal = false;
  loading = false;
  selectedFile: File | null = null;
  uploadingChapterId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private chapterService: ChapterService,
    private courseService: CourseService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.chapterForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      orderIndex: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.courseId = +params['courseId'];
      if (this.courseId) {
        this.loadCourse();
        this.loadChapters();
      }
    });
  }

  loadCourse(): void {
    if (this.courseId) {
      this.courseService.getCourseById(this.courseId).subscribe({
        next: (course) => {
          this.course = course;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du cours:', error);
        }
      });
    }
  }

  loadChapters(): void {
    if (this.courseId) {
      this.loading = true;
      this.chapterService.getChaptersByCourse(this.courseId).subscribe({
        next: (chapters) => {
          this.chapters = chapters;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des chapitres:', error);
          this.loading = false;
        }
      });
    }
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedChapterId = null;
    this.chapterForm.reset({ orderIndex: this.chapters.length + 1 });
    this.showModal = true;
  }

  openEditModal(chapter: Chapter): void {
    this.isEditMode = true;
    this.selectedChapterId = chapter.chapterId || null;
    this.chapterForm.patchValue({
      title: chapter.title,
      description: chapter.description,
      orderIndex: chapter.orderIndex
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.chapterForm.reset();
    this.selectedFile = null;
  }

  onSubmit(): void {
    if (this.chapterForm.invalid || !this.courseId) return;

    this.loading = true;
    
    if (this.isEditMode && this.selectedChapterId) {
      this.chapterService.updateChapter(this.courseId, this.selectedChapterId, this.chapterForm.value).subscribe({
        next: () => {
          this.loadChapters();
          this.closeModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors de la mise à jour du chapitre:', error);
          this.loading = false;
        }
      });
    } else {
      const createData: ChapterCreateRequest = this.chapterForm.value;
      this.chapterService.createChapter(this.courseId, createData).subscribe({
        next: () => {
          this.loadChapters();
          this.closeModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors de la création du chapitre:', error);
          this.loading = false;
        }
      });
    }
  }

  onFileSelected(event: Event, chapterId: number): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      if (file.type !== 'application/pdf') {
        alert('Seuls les fichiers PDF sont acceptés');
        return;
      }

      if (file.size > 10 * 1024 * 1024) { // 10MB max
        alert('Le fichier ne doit pas dépasser 10MB');
        return;
      }

      this.uploadPdf(chapterId, file);
    }
  }

  uploadPdf(chapterId: number, file: File): void {
    if (!this.courseId) return;

    this.uploadingChapterId = chapterId;
    this.chapterService.uploadPdf(this.courseId, chapterId, file).subscribe({
      next: () => {
        this.loadChapters();
        this.uploadingChapterId = null;
        alert('PDF uploadé avec succès');
      },
      error: (error) => {
        console.error('Erreur lors de l\'upload du PDF:', error);
        this.uploadingChapterId = null;
        alert('Erreur lors de l\'upload du PDF');
      }
    });
  }

  deleteChapter(chapterId: number): void {
    if (!this.courseId) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer ce chapitre ?')) {
      this.loading = true;
      this.chapterService.deleteChapter(this.courseId, chapterId).subscribe({
        next: () => {
          this.loadChapters();
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors de la suppression du chapitre:', error);
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard/courses']);
  }
}
