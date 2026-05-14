import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rh-certifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './certifications.component.html',
  styleUrls: ['./certifications.component.css']
})
export class RhCertificationsComponent {
  certifications: any[] = [];

  ngOnInit() {
    // TODO: Charger les certifications
  }
}
