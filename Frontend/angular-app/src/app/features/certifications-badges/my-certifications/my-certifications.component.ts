import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CertificationService, PageResponse } from '../../../core/services/certification.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { EarnedCertification } from '../../../core/models/certification.model';
import { PaginationComponent, PageInfo } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-my-certifications',
  standalone: true,
  imports: [CommonModule, PaginationComponent],
  templateUrl: './my-certifications.component.html',
  styleUrl: './my-certifications.component.scss'
})
export class MyCertificationsComponent implements OnInit {
  currentUser: AuthResponse | null = null;
  certifications: EarnedCertification[] = [];
  loading = false;
  error: string | null = null;
  awarding = false;
  sharingId: number | null = null;

  // Expose Math to template
  Math = Math;

  // Pagination state
  pageInfo: PageInfo = {
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  };
  pageSizeOptions = [10, 25, 50];

  constructor(
    private certificationService: CertificationService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    if (!this.currentUser) {
      this.error = 'Not authenticated';
      return;
    }
    this.loadCertifications();
  }

  private loadCertifications(): void {
    if (!this.currentUser) return;
    
    this.loading = true;
    this.error = null;
    
    this.certificationService.getCertificationsByLearnerPaginated(
      this.currentUser.userId,
      this.pageInfo.page,
      this.pageInfo.size,
      'issueDate',
      'DESC'
    ).subscribe({
      next: (response: PageResponse<EarnedCertification>) => {
        this.certifications = response.content;
        this.pageInfo.totalElements = response.totalElements;
        this.pageInfo.totalPages = response.totalPages;
        this.pageInfo.page = response.number;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load certifications';
        this.loading = false;
      }
    });
  }

  onPageChange(page: number): void {
    this.pageInfo.page = page;
    this.loadCertifications();
  }

  onPageSizeChange(size: number): void {
    this.pageInfo.size = size;
    this.pageInfo.page = 0; // Reset to first page when changing page size
    this.loadCertifications();
  }

  viewCertificate(certificationId: number): void {
    this.router.navigate(['/certificate-viewer', certificationId]);
  }

  shareOnLinkedIn(cert: EarnedCertification): void {
    if (!cert.id || this.sharingId === cert.id) return;
    this.sharingId = cert.id;

    this.certificationService.shareOnLinkedIn(cert.id).subscribe({
      next: (res) => {
        window.open(res.linkedInUrl, '_blank');
        this.sharingId = null;
      },
      error: () => {
        this.error = 'Failed to generate LinkedIn share link';
        this.sharingId = null;
      }
    });
  }

  awardSample(): void {
    if (!this.currentUser || this.awarding) return;
    this.awarding = true;
    this.certificationService.getAllTemplates().subscribe({
      next: (templates) => {
        if (!templates || templates.length === 0) {
          this.error = 'No certification templates available to award';
          this.awarding = false;
          return;
        }
        const issueDate = new Date();
        const yyyy = issueDate.getFullYear();
        const mm = String(issueDate.getMonth() + 1).padStart(2, '0');
        const dd = String(issueDate.getDate()).padStart(2, '0');
        const payload = {
          certificationTemplateId: Number(templates[0].id),
          learnerId: Number(this.currentUser!.userId),
          issueDate: `${yyyy}-${mm}-${dd}`
        };
        this.certificationService.awardCertification(payload).subscribe({
          next: () => {
            this.awarding = false;
            this.loadCertifications();
          },
          error: () => {
            this.error = 'Failed to award sample certification';
            this.awarding = false;
          }
        });
      },
      error: () => {
        this.error = 'Failed to load templates';
        this.awarding = false;
      }
    });
  }
}
