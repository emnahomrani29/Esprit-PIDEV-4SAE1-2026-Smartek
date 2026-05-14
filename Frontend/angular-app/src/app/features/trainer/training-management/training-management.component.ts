import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-training-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './training-management.component.html',
  styleUrls: ['./training-management.component.css']
})
export class TrainingManagementComponent {
  trainings: any[] = [];

  ngOnInit() {
    // TODO: Charger les formations
  }
}
