import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-learner-courses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class LearnerCoursesComponent {
  courses: any[] = [];

  ngOnInit() {
    // TODO: Charger les cours
  }
}
