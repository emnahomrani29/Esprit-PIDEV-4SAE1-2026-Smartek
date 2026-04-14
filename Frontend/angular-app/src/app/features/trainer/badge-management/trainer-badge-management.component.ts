import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-trainer-badge-management',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './trainer-badge-management.component.html',
  styleUrl: './trainer-badge-management.component.scss'
})
export class TrainerBadgeManagementComponent implements OnInit {
  badges: any[] = [];
  isLoading = false;

  ngOnInit() {
    // TODO: Load badges
  }
}
