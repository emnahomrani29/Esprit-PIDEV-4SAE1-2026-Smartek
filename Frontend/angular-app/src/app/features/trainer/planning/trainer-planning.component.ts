import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-trainer-planning',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './trainer-planning.component.html',
  styleUrl: './trainer-planning.component.scss'
})
export class TrainerPlanningComponent implements OnInit {
  
  constructor() {}

  ngOnInit(): void {
    // TODO: Load planning data
  }

  getPlanningStats() {
    return [
      { label: 'Sessions à venir', value: 0 },
      { label: 'Sessions ce mois', value: 0 }
    ];
  }
}
