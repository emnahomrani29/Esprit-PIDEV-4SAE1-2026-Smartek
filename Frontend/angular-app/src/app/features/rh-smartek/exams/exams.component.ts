import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rh-exams',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './exams.component.html',
  styleUrls: ['./exams.component.css']
})
export class RhExamsComponent {
  exams: any[] = [];

  ngOnInit() {
    // TODO: Charger les examens
  }
}
