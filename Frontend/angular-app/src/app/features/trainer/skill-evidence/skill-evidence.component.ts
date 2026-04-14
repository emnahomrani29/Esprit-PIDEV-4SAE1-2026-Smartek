import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skill-evidence',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skill-evidence.component.html',
  styleUrls: ['./skill-evidence.component.css']
})
export class SkillEvidenceComponent {
  evidences: any[] = [];

  ngOnInit() {
    // TODO: Charger les preuves de compétences
  }
}
