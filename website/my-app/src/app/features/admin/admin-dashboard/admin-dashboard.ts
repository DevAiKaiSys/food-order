import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { OrderService } from '../../../core/services/order-service';
import { Order } from '../../../shared/models/order.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  imports: [FormsModule, CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit, OnDestroy {
  private orderService = inject(OrderService);

  searchId: string = '';
  searchSubject: Subject<string> = new Subject<string>();

  orders: Order[] = [];
  isLoading = false;
  hasError = false;
  errorMessage = '';
  isOnline = true;

  statusConfig: any = {
    'PENDING': { label: 'รอดำเนินการ', color: 'warning', icon: 'clock' },
    'PROCESSING': { label: 'กำลังดำเนินการ', color: 'info', icon: 'gear' },
    'COMPLETED': { label: 'เสร็จสิ้น', color: 'success', icon: 'check-circle' },
    'CANCELLED': { label: 'ยกเลิก', color: 'danger', icon: 'x-circle' }
  };

  private subscriptions: Subscription[] = [];

  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  totalItems = 0;
  pageSizeOptions: number[] = [5, 10, 20, 50];

  Math = Math;

  ngOnInit() {
    // Subscribe to searchSubject with debounceTime and switchMap
    this.subscriptions.push(
      this.searchSubject.pipe(
        debounceTime(300)  // รอ 300ms หลังจากหยุดพิมพ์
      ).subscribe((searchId: string) => {
        console.log('Searching for:', searchId);
        this.currentPage = 1;  // รีเซ็ตหน้าเป็นหน้าแรกทุกครั้งที่ทำการค้นหา
        this.searchId = searchId;
        this.loadOrders();
      })
    );

    this.subscriptions.push(
      this.orderService.orders$.subscribe(orders => this.orders = orders),
      this.orderService.loading$.subscribe(loading => this.isLoading = loading),
      this.orderService.error$.subscribe(error => {
        this.hasError = !!error;
        this.errorMessage = error || '';
      }),
      this.orderService.pagination$.subscribe(p => {
        this.totalPages = p.totalPages;
        this.totalItems = p.totalItems;
        this.currentPage = p.currentPage;
        this.itemsPerPage = p.pageSize;
      })
    );

    this.loadOrders();
  }

  ngOnDestroy() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.searchSubject.complete();
  }

  loadOrders() {
    if (!this.isOnline) {
      this.hasError = true;
      this.errorMessage = 'ไม่มีการเชื่อมต่ออินเทอร์เน็ต กรุณาตรวจสอบการเชื่อมต่อของคุณ';
      return;
    }
    this.orderService.loadOrders(this.currentPage, this.itemsPerPage, this.searchId);
  }

  retryLoadOrders() {
    this.orderService.clearError();
    this.loadOrders();
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchId);
  }

  onPageSizeChange() {
    this.itemsPerPage = parseInt(this.itemsPerPage.toString(), 10);
    this.currentPage = 1;
    this.loadOrders();
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadOrders();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;

    if (this.totalPages <= maxPagesToShow) {
      for (let i = 1; i <= this.totalPages; i++) pages.push(i);
    } else {
      const half = Math.floor(maxPagesToShow / 2);
      let start = Math.max(1, this.currentPage - half);
      let end = Math.min(this.totalPages, start + maxPagesToShow - 1);
      if (end - start < maxPagesToShow - 1) start = Math.max(1, end - maxPagesToShow + 1);
      for (let i = start; i <= end; i++) pages.push(i);
    }

    return pages;
  }
}
