import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-company-participation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './participation.component.html',
  styleUrls: ['./participation.component.css']
})
export class CompanyParticipationComponent {
  events: any[] = [];

  ngOnInit() {
    // TODO: Charger les événements
  }
}
