import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
export class SignUpComponent {
  currentStep = 1;
  totalSteps = 3;

  step1Form: FormGroup;
  step2Form: FormGroup;
  step3Form: FormGroup;

  showPassword = false;
  showConfirmPassword = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    // Step 1: Informations personnelles
    this.step1Form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]]
    });

    // Step 2: Mot de passe
    this.step2Form = this.fb.group({
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    // Step 3: Informations supplémentaires
    this.step3Form = this.fb.group({
      phone: ['', [Validators.pattern(/^[0-9]{10}$/)]],
      image: [null],
      experience: [0, [Validators.min(0)]],
      role: ['LEARNER', Validators.required]
    });
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  nextStep() {
    if (this.currentStep === 1 && this.step1Form.valid) {
      this.currentStep++;
    } else if (this.currentStep === 2 && this.step2Form.valid) {
      this.currentStep++;
    }
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  onSubmit() {
    if (this.step3Form.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const formData = {
        firstName: this.step1Form.value.firstName,
        email: this.step1Form.value.email,
        password: this.step2Form.value.password,
        phone: this.step3Form.value.phone || undefined,
        imageBase64: this.step3Form.value.image || undefined,
        experience: this.step3Form.value.experience || 0,
        role: this.step3Form.value.role
      };

      this.authService.register(formData).subscribe({
        next: (response: AuthResponse) => {
          console.log('Registration successful:', response);
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error: any) => {
          console.error('Registration error:', error);
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
        }
      });
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  getPasswordStrength(): string {
    const password = this.step2Form.get('password')?.value || '';
    if (password.length === 0) return '';
    if (password.length < 8) return 'weak';
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) return 'medium';
    return 'strong';
  }

  hasPasswordError(errorType: string): boolean {
    const control = this.step2Form.get('password');
    return control ? control.hasError(errorType) && control.touched : false;
  }

  hasMinLength(): boolean {
    const password = this.step2Form.get('password')?.value || '';
    return password.length >= 8;
  }

  hasNumber(): boolean {
    const password = this.step2Form.get('password')?.value || '';
    return /\d/.test(password);
  }

  hasUpperAndLower(): boolean {
    const password = this.step2Form.get('password')?.value || '';
    return /[a-z]/.test(password) && /[A-Z]/.test(password);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Vérifier le type de fichier
      if (!file.type.startsWith('image/')) {
        alert('Please select an image file');
        return;
      }

      // Vérifier la taille (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('Image size should not exceed 5MB');
        return;
      }

      this.selectedFile = file;

      // Créer un aperçu de l'image et convertir en byte array
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.imagePreview = e.target?.result as string;
        // Convertir base64 en byte array pour le backend
        const base64String = this.imagePreview.split(',')[1];
        this.step3Form.patchValue({ image: base64String });
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.step3Form.patchValue({ image: null });
  }
}
