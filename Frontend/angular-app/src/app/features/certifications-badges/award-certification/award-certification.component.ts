import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CertificationService } from '../../../core/services/certification.service';
import { CertificationTemplate } from '../../../core/models/certification.model';

@Component({
  selector: 'app-award-certification',
  templateUrl: './award-certification.component.html',
  styleUrls: ['./award-certification.component.scss']
})
export class AwardCertificationComponent implements OnInit {
  awardForm: FormGroup;
  certifications: CertificationTemplate[] = [];
  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private fb: FormBuilder,
    private certificationService: CertificationService,
    private router: Router
  ) {
    this.awardForm = this.fb.group({
      certificationTemplateId: ['', [Validators.required]],
      learnerId: ['', [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadCertifications();
  }

  loadCertifications(): void {
    this.certificationService.getAllTemplates().subscribe({
      next: (data) => {
        this.certifications = data;
      },
      error: (err) => {
        this.error = 'Failed to load certifications';
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.awardForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = null;
    this.success = null;

    const formValue = this.awardForm.value;
    const issueDate = new Date();
    const yyyy = issueDate.getFullYear();
    const mm = String(issueDate.getMonth() + 1).padStart(2, '0');
    const dd = String(issueDate.getDate()).padStart(2, '0');
    const payload = {
      certificationTemplateId: Number(formValue.certificationTemplateId),
      learnerId: Number(formValue.learnerId),
      issueDate: `${yyyy}-${mm}-${dd}`
    };

    this.certificationService.awardCertification(payload).subscribe({
      next: () => {
        this.success = 'Certification awarded successfully!';
        this.loading = false;
        this.awardForm.reset();
        setTimeout(() => {
          this.router.navigate(['/dashboard/certifications']);
        }, 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to award certification';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/certifications']);
  }
}
