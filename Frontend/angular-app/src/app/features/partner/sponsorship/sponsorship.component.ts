import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sponsorship',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sponsorship.component.html',
  styleUrls: ['./sponsorship.component.css']
})
export class SponsorshipComponent {
  sponsorships: any[] = [];

  ngOnInit() {
    // TODO: Charger les sponsors
  }
}
