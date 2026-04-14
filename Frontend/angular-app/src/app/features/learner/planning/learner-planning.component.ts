import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-learner-planning',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './learner-planning.component.html',
  styleUrl: './learner-planning.component.scss'
})
export class LearnerPlanningComponent implements OnInit {
  
  constructor() {}

  ngOnInit(): void {
    // TODO: Load planning data
  }
}
