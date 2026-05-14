import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.scss'
})
export class PageHeaderComponent {
  @Input() title: string = '';
  @Input() subtitle: string = '';
  @Input() icon: string = '📚';
  @Input() gradient: string = 'from-green-600 via-emerald-600 to-teal-600';
  @Input() showStats: boolean = false;
  @Input() stats: { label: string; value: string | number }[] = [];
}
