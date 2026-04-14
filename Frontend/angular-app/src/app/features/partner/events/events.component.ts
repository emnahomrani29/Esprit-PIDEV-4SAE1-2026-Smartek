import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-partner-events',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class PartnerEventsComponent {
  events: any[] = [];

  ngOnInit() {
    // TODO: Charger les événements
  }
}
