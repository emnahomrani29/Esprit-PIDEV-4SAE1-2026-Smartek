import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-trainer-events',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './trainer-events.component.html',
  styleUrl: './trainer-events.component.scss'
})
export class TrainerEventsComponent implements OnInit {
  
  constructor() {}

  ngOnInit(): void {
    // TODO: Load events data
  }

  getEventsStats() {
    return [
      { label: 'Événements actifs', value: 0 },
      { label: 'Participants totaux', value: 0 }
    ];
  }
}
