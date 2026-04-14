import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CourseService } from '../../../core/services/course.service';
import { ChapterService } from '../../../core/services/chapter.service';
import { AuthService } from '../../../core/services/auth.service';
import { Course, CourseCreateRequest } from '../../../core/models/course.model';
import { Chapter, ChapterCreateRequest } from '../../../core/models/chapter.model';
import { forkJoin } from 'rxjs';

interface ChapterFormData {
  title: string;
  description: string;
  orderIndex: number;
  pdfFile: File | null;
}

@Component({
  selector: 'app-course-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './course-management.component.html',
  styleUrl: './course-management.component.scss'
})
export class CourseManagementComponent implements OnInit {
  courses: Course[] = [];
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
  
  // Chapter management modal
  showChaptersListModal = false;
  currentCourseForChapters: Course | null = null;
  courseChapters: Chapter[] = [];
  loadingChapters = false;

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private chapterService: ChapterService,
    private authService: AuthService
  ) {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      content: ['', Validators.required],
      duration: ['', Validators.required],
      trainerId: [null]
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

  loadCourses(): void {
    this.loading = true;
    const userInfo = this.authService.getUserInfo();
    const isAdmin = userInfo?.role === 'ADMIN';
    
    if (isAdmin) {
      // ADMIN voit tous les cours
      this.courseService.getAllCourses().subscribe({
        next: (courses) => {
          this.courses = courses;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading courses:', error);
          this.loading = false;
        }
      });
    } else if (this.currentUserId) {
      // Les autres voient uniquement leurs cours
      this.courseService.getCoursesByTrainer(this.currentUserId).subscribe({
        next: (courses) => {
          this.courses = courses;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading courses:', error);
          this.loading = false;
        }
      });
    }
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
      trainerId: course.trainerId
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
}
