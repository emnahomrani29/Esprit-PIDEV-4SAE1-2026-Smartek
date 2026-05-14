import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-trainer-courses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class TrainerCoursesComponent {
  courses: any[] = [];

  ngOnInit() {
    // TODO: Charger les cours
  }
}
