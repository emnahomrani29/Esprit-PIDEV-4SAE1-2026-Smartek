import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CertificationService } from '../../../core/services/certification.service';
import { AuthService } from '../../../core/services/auth.service';
import { EarnedCertification } from '../../../core/models/certification.model';

@Component({
  selector: 'app-certificate-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './certificate-viewer.component.html',
  styleUrl: './certificate-viewer.component.scss'
})
export class CertificateViewerComponent implements OnInit {
  certification: EarnedCertification | null = null;
  learnerName: string = '';
  loading = false;
  error: string | null = null;
  downloading = false;
  sharingLinkedIn = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private certificationService: CertificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCertification(+id);
    }
  }

  private loadCertification(id: number): void {
    this.loading = true;
    this.error = null;

    this.certificationService.getEarnedCertificationById(id).subscribe({
      next: (data) => {
        this.certification = data;
        this.loadLearnerName();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load certification';
        this.loading = false;
      }
    });
  }

  private loadLearnerName(): void {
    const currentUser = this.authService.getUserInfo();
    if (currentUser) {
      this.learnerName = currentUser.firstName || 'Learner';
    }
  }

  getBadgeLevel(): string {
    return 'Gold';
  }

  getCertificationId(): string {
    if (!this.certification) return '';
    const date = new Date(this.certification.issueDate);
    const year = date.getFullYear();
    return `SMARTEK-${year}-${String(this.certification.id).padStart(6, '0')}`;
  }

  formatDate(date: string | Date | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  /** Download the digitally signed PDF from the backend */
  downloadPDF(): void {
    if (!this.certification?.id || this.downloading) return;
    this.downloading = true;

    this.certificationService.downloadCertificatePdf(this.certification.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Smartek_Certificate_${this.certification!.certificationTemplate.title.replaceAll(/\s+/g, '_')}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloading = false;
      },
      error: () => {
        this.error = 'Failed to download PDF';
        this.downloading = false;
      }
    });
  }

  /** Share this certification on LinkedIn */
  shareOnLinkedIn(): void {
    if (!this.certification?.id || this.sharingLinkedIn) return;
    this.sharingLinkedIn = true;

    this.certificationService.shareOnLinkedIn(this.certification.id).subscribe({
      next: (res) => {
        window.open(res.linkedInUrl, '_blank');
        this.sharingLinkedIn = false;
      },
      error: () => {
        this.error = 'Failed to generate LinkedIn share link';
        this.sharingLinkedIn = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/my-certifications']);
  }
}
