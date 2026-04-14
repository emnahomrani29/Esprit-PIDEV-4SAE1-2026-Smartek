import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rh-events',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class RhEventsComponent {
  events: any[] = [];

  ngOnInit() {
    // TODO: Charger les événements
  }
}
