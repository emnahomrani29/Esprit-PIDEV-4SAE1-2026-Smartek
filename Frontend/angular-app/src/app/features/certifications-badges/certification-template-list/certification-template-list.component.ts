import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CertificationService } from '../../../core/services/certification.service';
import { CertificationTemplate } from '../../../core/models/certification.model';

@Component({
  selector: 'app-certification-template-list',
  templateUrl: './certification-template-list.component.html',
  styleUrls: ['./certification-template-list.component.scss']
})
export class CertificationTemplateListComponent implements OnInit {
  certifications: CertificationTemplate[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private certificationService: CertificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCertifications();
  }

  loadCertifications(): void {
    this.loading = true;
    this.error = null;
    this.certificationService.getAllTemplates().subscribe({
      next: (data) => {
        this.certifications = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load certifications';
        this.loading = false;
        console.error(err);
      }
    });
  }

  createCertification(): void {
    this.router.navigate(['/dashboard/certifications/new']);
  }

  editCertification(id: number): void {
    this.router.navigate(['/dashboard/certifications/edit', id]);
  }

  deleteCertification(id: number): void {
    if (confirm('Are you sure you want to delete this certification template?')) {
      this.certificationService.deleteTemplate(id).subscribe({
        next: () => {
          this.loadCertifications();
        },
        error: (err) => {
          this.error = 'Failed to delete certification';
          console.error(err);
        }
      });
    }
  }

  awardCertification(): void {
    this.router.navigate(['/dashboard/certifications/award']);
  }
}
