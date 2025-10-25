import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { OrderService } from '../../../core/services/order-service';
import { Order, OrderDetail, OrderStatus, StatusConfig } from '../../../shared/models/order.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast-service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [FormsModule, CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit, OnDestroy {
  private orderService = inject(OrderService);
  private toastService = inject(ToastService);

  searchId: string = '';
  searchSubject: Subject<string> = new Subject<string>();

  orders: Order[] = [];
  isLoading = false;
  hasError = false;
  errorMessage = '';
  isOnline = true;

  // สำหรับ modal
  selectedOrder: OrderDetail | null = null;
  isLoadingDetail = false;
  detailError = false;

  orderStatuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'COOKING', 'DELIVERING', 'COMPLETED', 'CANCELLED'];
  statusConfig: { [key in OrderStatus]: StatusConfig } = {
    PENDING: { label: 'รอดำเนินการ', color: 'warning', icon: 'clock', nextStatus: 'CONFIRMED' },
    CONFIRMED: { label: 'ยืนยัน', color: 'info', icon: 'check-circle', nextStatus: 'COOKING' },
    COOKING: { label: 'กำลังทำอาหาร', color: 'primary', icon: 'fire', nextStatus: 'DELIVERING' },
    DELIVERING: { label: 'กำลังจัดส่ง', color: 'dark', icon: 'truck-front', nextStatus: 'COMPLETED' },
    COMPLETED: { label: 'เสร็จสิ้น', color: 'success', icon: 'check-circle-fill' },
    CANCELLED: { label: 'ยกเลิก', color: 'danger', icon: 'x-circle' }
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
    this.subscriptions.push(
      this.searchSubject.pipe(
        debounceTime(300)
      ).subscribe((searchId: string) => {
        console.log('Searching for:', searchId);
        this.currentPage = 1;
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

  // เปิด modal และโหลดรายละเอียด
  viewOrderDetail(orderId: number) {
    this.isLoadingDetail = true;
    this.detailError = false;
    this.selectedOrder = null;

    this.orderService.getOrderDetail(orderId).subscribe({
      next: (response) => {
        this.selectedOrder = response.data;
        this.isLoadingDetail = false;

        // เปิด modal (ใช้ Bootstrap 5 Modal)
        const modalElement = document.getElementById('orderDetailModal');
        if (modalElement) {
          const modal = new (window as any).bootstrap.Modal(modalElement);
          modal.show();
        }
      },
      error: (error) => {
        console.error('Error loading order detail:', error);
        this.detailError = true;
        this.isLoadingDetail = false;
        this.toastService.show('ไม่สามารถโหลดรายละเอียดออเดอร์ได้', 'error');
      }
    });
  }

  changeStatus(orderId: number, newStatus: OrderStatus) {
    this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
      next: () => {
        this.toastService.show(
          `เปลี่ยนสถานะเป็น ${this.statusConfig[newStatus].label} สำเร็จ`,
          'success'
        );
        this.loadOrders();
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        const errorMsg = error?.error?.message || 'เกิดข้อผิดพลาดในการเปลี่ยนสถานะ';
        this.toastService.show(errorMsg, 'error');
      }
    });
  }

  cancelOrder(orderId: number) {
    const order = this.orders.find(o => o.id === orderId);
    if (order?.status === 'COMPLETED') {
      this.toastService.show('ไม่สามารถยกเลิกออเดอร์ที่เสร็จสิ้นแล้ว', 'error');
      return;
    }

    this.changeStatus(orderId, 'CANCELLED');
  }
}
