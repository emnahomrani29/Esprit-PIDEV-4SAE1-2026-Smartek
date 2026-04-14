import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { map } from 'rxjs';

@Component({
  selector: 'app-company-participation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './participation.component.html'
})
export class CompanyParticipationComponent implements OnInit {
  trainings: any[] = [];
  enrollments: any[] = [];
  loading = false;
  showEnrollModal = false;
  selectedTraining: any = null;
  enrollForm = { employeeName: '', employeeEmail: '' };
  userId = 0;

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit() {
    this.userId = this.authService.getUserInfo()?.userId || 0;
    this.loadTrainings();
    this.loadMyEnrollments();
  }

  loadTrainings() {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/trainings`).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    ).subscribe({
      next: data => { this.trainings = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  loadMyEnrollments() {
    this.http.get<any[]>(`${environment.apiUrl}/training-enrollments/user/${this.userId}`).subscribe({
      next: data => { this.enrollments = data || []; },
      error: () => {}
    });
  }

  openEnroll(training: any) {
    this.selectedTraining = training;
    this.enrollForm = { employeeName: '', employeeEmail: '' };
    this.showEnrollModal = true;
  }

  enroll() {
    const payload = { trainingId: this.selectedTraining.id, userId: this.userId };
    this.http.post<any>(`${environment.apiUrl}/training-enrollments`, payload).subscribe({
      next: () => { this.showEnrollModal = false; this.loadMyEnrollments(); alert('Inscription réussie !'); },
      error: () => alert('Erreur lors de l\'inscription')
    });
  }

  isEnrolled(trainingId: number): boolean {
    return this.enrollments.some(e => e.trainingId === trainingId);
  }

  unenroll(trainingId: number) {
    if (!confirm('Se désinscrire de cette formation ?')) return;
    this.http.delete(`${environment.apiUrl}/training-enrollments/user/${this.userId}/training/${trainingId}`).subscribe({
      next: () => this.loadMyEnrollments()
    });
  }

  statusColor(status: string): string {
    const map: any = { ACTIVE: 'bg-green-100 text-green-700', COMPLETED: 'bg-blue-100 text-blue-700', CANCELLED: 'bg-red-100 text-red-700', ENROLLED: 'bg-indigo-100 text-indigo-700' };
    return map[status] || 'bg-gray-100 text-gray-600';
  }
}
