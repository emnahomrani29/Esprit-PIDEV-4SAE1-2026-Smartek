import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent {
  signInForm: FormGroup;
  showPassword = false;
  rememberMe = false;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.signInForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.signInForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData = {
        email: this.signInForm.value.email,
        password: this.signInForm.value.password
      };

      this.authService.login(loginData).subscribe({
        next: (response: AuthResponse) => {
          console.log('Login successful:', response);
          console.log('Token saved:', this.authService.getToken());
          console.log('User info:', this.authService.getUserInfo());
          this.isLoading = false;
          
          // Redirection basée sur le rôle
          switch (response.role) {
            case 'ADMIN':
            case 'RH_COMPANY':
              window.location.href = '/dashboard';
              break;
            case 'RH_SMARTEK':
              window.location.href = '/rh-smartek';
              break;
            case 'SPONSOR':
              window.location.href = '/sponsor';
              break;
            case 'PARTNER':
              window.location.href = '/partner';
              break;
            case 'LEARNER':
              window.location.href = '/learner';
              break;
            case 'TRAINER':
              window.location.href = '/trainer';
              break;
            default:
              // LEARNER et TRAINER vont vers le front-office
              window.location.href = '/';
          }
        },
        error: (error: any) => {
          console.error('Login error:', error);
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Login failed. Please check your credentials.';
        }
      });
    }
  }
}
