import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contract-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contract-management.component.html',
  styleUrls: ['./contract-management.component.css']
})
export class ContractManagementComponent {
  contracts: any[] = [];

  ngOnInit() {
    // TODO: Charger les contrats
  }
}
