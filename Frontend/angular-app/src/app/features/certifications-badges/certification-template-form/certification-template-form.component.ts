import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CertificationService } from '../../../core/services/certification.service';
import { CertificationTemplate } from '../../../core/models/certification.model';

@Component({
  selector: 'app-certification-template-form',
  templateUrl: './certification-template-form.component.html',
  styleUrls: ['./certification-template-form.component.scss']
})
export class CertificationTemplateFormComponent implements OnInit {
  certificationForm: FormGroup;
  isEditMode = false;
  certificationId: number | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private certificationService: CertificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.certificationForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.certificationId = +params['id'];
        this.loadCertification(this.certificationId);
      }
    });
  }

  loadCertification(id: number): void {
    this.loading = true;
    this.certificationService.getTemplateById(id).subscribe({
      next: (certification) => {
        this.certificationForm.patchValue(certification);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load certification';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.certificationForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = null;
    const certification: CertificationTemplate = this.certificationForm.value;

    const request = this.isEditMode
      ? this.certificationService.updateTemplate(this.certificationId!, certification)
      : this.certificationService.createTemplate(certification);

    request.subscribe({
      next: () => {
        this.router.navigate(['/dashboard/certifications']);
      },
      error: (err) => {
        this.error = this.isEditMode ? 'Failed to update certification' : 'Failed to create certification';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/certifications']);
  }
}
