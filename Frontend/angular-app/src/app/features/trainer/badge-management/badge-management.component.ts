import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-badge-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './badge-management.component.html',
  styleUrls: ['./badge-management.component.css']
})
export class BadgeManagementComponent {
  badges: any[] = [];

  ngOnInit() {
    // TODO: Charger les badges
  }
}
