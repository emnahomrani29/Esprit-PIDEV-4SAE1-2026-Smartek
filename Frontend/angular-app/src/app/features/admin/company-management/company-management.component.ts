import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-company-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './company-management.component.html',
  styleUrls: ['./company-management.component.css']
})
export class CompanyManagementComponent {
  companies: any[] = [];

  ngOnInit() {
    // TODO: Charger les entreprises
  }
}
