import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-learner-exams',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './exams.component.html',
  styleUrls: ['./exams.component.css']
})
export class LearnerExamsComponent {
  exams: any[] = [];

  ngOnInit() {
    // TODO: Charger les examens
  }
}
