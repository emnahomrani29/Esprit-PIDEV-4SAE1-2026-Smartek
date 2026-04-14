import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, AuthResponse } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  user: AuthResponse | null = null;
  isLoading = true;
  isEditing = false;
  selectedFile: File | null = null;
  previewImage: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    this.isLoading = true;
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        this.user = userData;
        if (userData.imageBase64) {
          this.previewImage = `data:image/jpeg;base64,${userData.imageBase64}`;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading user data:', error);
        this.isLoading = false;
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  getUserInitial(): string {
    return this.user?.firstName?.charAt(0).toUpperCase() || 'U';
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
