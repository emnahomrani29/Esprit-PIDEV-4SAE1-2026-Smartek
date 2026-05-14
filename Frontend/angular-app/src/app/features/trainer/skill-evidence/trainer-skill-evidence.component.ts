import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-trainer-skill-evidence',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './trainer-skill-evidence.component.html',
  styleUrl: './trainer-skill-evidence.component.scss'
})
export class TrainerSkillEvidenceComponent implements OnInit {
  evidences: any[] = [];
  isLoading = false;

  ngOnInit() {
    // TODO: Load skill evidences
  }
}
