import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-learner-certifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './certifications.component.html',
  styleUrls: ['./certifications.component.css']
})
export class LearnerCertificationsComponent {
  certifications: any[] = [];

  ngOnInit() {
    // TODO: Charger les certifications
  }
}
