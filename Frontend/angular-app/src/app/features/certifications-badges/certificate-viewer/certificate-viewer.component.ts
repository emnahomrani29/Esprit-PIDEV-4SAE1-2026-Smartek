import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CertificationService } from '../../../core/services/certification.service';
import { AuthService } from '../../../core/services/auth.service';
import { EarnedCertification } from '../../../core/models/certification.model';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

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
    // This would be determined by the exam score or badge template
    // For now, returning a default
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

  async downloadPDF(): Promise<void> {
    if (!this.certification || this.downloading) return;
    
    this.downloading = true;
    try {
      const element = document.getElementById('certificate-content');
      if (!element) {
        throw new Error('Certificate element not found');
      }

      // Capture the certificate as canvas
      const canvas = await html2canvas(element, {
        scale: 2,
        useCORS: true,
        logging: false,
        backgroundColor: '#ffffff'
      });

      // Create PDF in A4 landscape format
      const pdf = new jsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4'
      });

      const imgWidth = 297; // A4 landscape width in mm
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      
      const imgData = canvas.toDataURL('image/png');
      pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight);

      // Generate filename
      const fileName = `SMARTEK_Certification_${this.learnerName}_${this.certification.certificationTemplate.title.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`;
      
      pdf.save(fileName);
    } catch (error) {
      console.error('Error generating PDF:', error);
      this.error = 'Failed to generate PDF';
    } finally {
      this.downloading = false;
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard/my-certifications']);
  }
}
