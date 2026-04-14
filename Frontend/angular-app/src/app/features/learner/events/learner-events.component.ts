import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-learner-events',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './learner-events.component.html',
  styleUrl: './learner-events.component.scss'
})
export class LearnerEventsComponent implements OnInit {
  
  constructor() {}

  ngOnInit(): void {
    // TODO: Load events data
  }
}
