import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss'
})
export class PaginationComponent {
  @Input() pageInfo!: PageInfo;
  @Input() pageSizeOptions: number[] = [10, 25, 50];
  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  get startItem(): number {
    return this.pageInfo.totalElements === 0 ? 0 : this.pageInfo.page * this.pageInfo.size + 1;
  }

  get endItem(): number {
    const end = (this.pageInfo.page + 1) * this.pageInfo.size;
    return Math.min(end, this.pageInfo.totalElements);
  }

  get visiblePages(): number[] {
    const current = this.pageInfo.page;
    const total = this.pageInfo.totalPages;
    const delta = 2;
    const range: number[] = [];
    const rangeWithDots: number[] = [];

    for (let i = Math.max(0, current - delta); i <= Math.min(total - 1, current + delta); i++) {
      range.push(i);
    }

    let prev: number | null = null;
    for (const i of range) {
      if (prev !== null && i - prev > 1) {
        rangeWithDots.push(-1); // -1 represents dots
      }
      rangeWithDots.push(i);
      prev = i;
    }

    return rangeWithDots;
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.pageInfo.totalPages && page !== this.pageInfo.page) {
      this.pageChange.emit(page);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSizeChange.emit(newSize);
  }

  previousPage(): void {
    if (this.pageInfo.page > 0) {
      this.goToPage(this.pageInfo.page - 1);
    }
  }

  nextPage(): void {
    if (this.pageInfo.page < this.pageInfo.totalPages - 1) {
      this.goToPage(this.pageInfo.page + 1);
    }
  }
}
