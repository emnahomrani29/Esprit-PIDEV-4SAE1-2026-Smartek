import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-interviews',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interviews.component.html',
  styleUrls: ['./interviews.component.css']
})
export class InterviewsComponent {
  interviews: any[] = [];

  ngOnInit() {
    // TODO: Charger les entretiens
  }
}
