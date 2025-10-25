import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { OrderService } from '../../../core/services/order-service';
import { Order, OrderDetail, OrderStatus, STATUS_CONFIG, StatusConfig } from '../../../shared/models/order.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';

interface ActionButtonConfig {
  status: OrderStatus;
  label: string;
  btnClass: string;
  nextStatus?: OrderStatus;
}

type FilterStatus = OrderStatus | 'ALL';

@Component({
  selector: 'app-admin-dashboard',
  imports: [FormsModule, CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit, OnDestroy {
  private orderService = inject(OrderService);
  private toastr = inject(ToastrService);

  filterStatus: FilterStatus = 'ALL';

  searchId: string = '';
  searchSubject: Subject<string> = new Subject<string>();

  orders: Order[] = [];
  isLoading = false;
  hasError = false;
  errorMessage = '';
  isOnline = true;

  // สำหรับ modal
  selectedOrder: Order | null = null;
  selectedOrderDetail: OrderDetail | null = null;
  isLoadingDetail = false;
  detailError = false;

  // สำหรับ cancel confirmation
  orderToCancel: Order | null = null;
  isCancelling = false;

  // ใช้ constant จาก model
  statusConfig: { [key in OrderStatus]: StatusConfig } = STATUS_CONFIG;
  OrderStatus = OrderStatus; // Expose enum to the template

  private subscriptions: Subscription[] = [];

  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  totalItems = 0;
  pageSizeOptions: number[] = [5, 10, 20, 50];

  Math = Math;
  loadingStates: { [key: string]: boolean } = {};

  actionButtons: ActionButtonConfig[] = [
    { status: OrderStatus.CONFIRMED, label: 'ยืนยัน', btnClass: 'btn-info', nextStatus: OrderStatus.COOKING },
    { status: OrderStatus.COOKING, label: 'เริ่มทำ', btnClass: 'btn-primary', nextStatus: OrderStatus.DELIVERING },
    { status: OrderStatus.DELIVERING, label: 'จัดส่ง', btnClass: 'btn-secondary', nextStatus: OrderStatus.COMPLETED },
    { status: OrderStatus.COMPLETED, label: 'เสร็จสิ้น', btnClass: 'btn-success' },
    { status: OrderStatus.CANCELLED, label: 'ยกเลิก', btnClass: 'btn-danger' },
  ];

  filterStatusOptions: { value: FilterStatus; label: string }[] = [
    { value: 'ALL', label: 'ทั้งหมด' },
    { value: OrderStatus.PENDING, label: 'รอยืนยัน' },
    { value: OrderStatus.CONFIRMED, label: 'ยืนยันแล้ว' },
    { value: OrderStatus.COOKING, label: 'กำลังทำ' },
    { value: OrderStatus.DELIVERING, label: 'กำลังจัดส่ง' },
    { value: OrderStatus.COMPLETED, label: 'เสร็จสิ้น' },
    { value: OrderStatus.CANCELLED, label: 'ยกเลิก' },
  ];

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

    const statusParam = this.filterStatus !== 'ALL' ? this.filterStatus : undefined;
    this.orderService.loadOrders(this.currentPage, this.itemsPerPage, statusParam, this.searchId);
  }

  retryLoadOrders() {
    this.orderService.clearError();
    this.loadOrders();
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchId);
  }

  onStatusFilterChange(): void {
    this.currentPage = 1;
    this.loadOrders();
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
  viewOrderDetail(order: Order) {
    this.isLoadingDetail = true;
    this.detailError = false;
    this.selectedOrder = order;

    this.orderService.getOrderDetail(order.id).subscribe({
      next: (response) => {
        this.selectedOrderDetail = response.data;
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
        this.toastr.error("ไม่สามารถโหลดรายละเอียดออเดอร์ได้", 'error', { closeButton: true });
      }
    });
  }

  isActionDisabled(order: Order, action: OrderStatus): boolean {
    const current = order.status;

    if (action === OrderStatus.CANCELLED) {
      // ห้ามยกเลิกถ้า Complete หรือ Cancelled ไปแล้ว
      return current === OrderStatus.COMPLETED || current === OrderStatus.CANCELLED;
    }

    // ตรวจสอบตามลำดับ
    switch (action) {
      case OrderStatus.CONFIRMED: return current !== OrderStatus.PENDING;
      case OrderStatus.COOKING: return current !== OrderStatus.CONFIRMED;
      case OrderStatus.DELIVERING: return current !== OrderStatus.COOKING;
      case OrderStatus.COMPLETED: return current !== OrderStatus.DELIVERING;
      default: return true; // ปุ่มอื่นๆ ที่ไม่รู้จัก
    }
  }

  updateStatus(order: Order | OrderDetail, newStatus: OrderStatus): void {
    const loadingKey = `${order.id}-${newStatus}`;
    this.loadingStates[loadingKey] = true;

    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: () => {
        this.showUpdateStatusToast(order, newStatus);
        this.loadingStates[loadingKey] = false;
        this.loadOrders();

        if (newStatus === OrderStatus.CONFIRMED || newStatus === OrderStatus.CANCELLED) {
          this.closeModal();
        }
      },
      error: (err) => {
        const errorMsg = err.error?.message || err.message || 'ไม่สามารถอัปเดตสถานะได้';
        this.toastr.error(errorMsg, 'อัปเดตล้มเหลว', { closeButton: true });
        this.loadingStates[loadingKey] = false;
      }
    });
  }

  private showUpdateStatusToast(order: Order | OrderDetail, newStatus: OrderStatus): void {
    const statusConfig = this.statusConfig[newStatus];
    const message = `ออเดอร์ #${order.slip_id} เปลี่ยนเป็น ${statusConfig.label}`;
    const title = 'อัปเดตสำเร็จ';

    if (newStatus === OrderStatus.COMPLETED) {
      this.toastr.success(message, title, { closeButton: true });
    } else if (newStatus === OrderStatus.CANCELLED) {
      this.toastr.info(message, title, { closeButton: true });
    }
  }

  closeModal() {
    const modalElement = document.getElementById('orderDetailModal');
    if (modalElement) {
      const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
      if (modal) {
        modal.hide();
      }
    }
  }

  isActionLoading(order: Order, action: OrderStatus): boolean {
    return this.loadingStates[`${order.id}-${action}`] || false;
  }

  handleStatusAction(order: Order, newStatus: OrderStatus): void {
    if (newStatus === OrderStatus.CANCELLED) {
      // แสดง modal ยืนยันการยกเลิก
      this.orderToCancel = order;
      const modalElement = document.getElementById('cancelConfirmModal');
      if (modalElement) {
        const modal = new (window as any).bootstrap.Modal(modalElement);
        modal.show();
      }
    } else if (newStatus === OrderStatus.CONFIRMED) {
      // แสดง modal รายละเอียดพร้อมปุ่มยืนยัน
      this.viewOrderDetail(order);
    } else {
      // อัปเดตสถานะปกติ
      this.updateStatus(order, newStatus);
    }
  }

  confirmCancelOrder(): void {
    if (!this.orderToCancel) return;

    this.isCancelling = true;
    const order = this.orderToCancel;

    this.orderService.updateOrderStatus(order.id, OrderStatus.CANCELLED).subscribe({
      next: () => {
        this.showUpdateStatusToast(order, OrderStatus.CANCELLED);
        this.isCancelling = false;
        this.orderToCancel = null;
        this.loadOrders();
        this.closeCancelModal();
      },
      error: (err) => {
        const errorMsg = err.error?.message || err.message || 'ไม่สามารถยกเลิกออเดอร์ได้';
        this.toastr.error(errorMsg, 'อัปเดตล้มเหลว', { closeButton: true });
        this.isCancelling = false;
      }
    });
  }

  closeCancelModal(): void {
    const modalElement = document.getElementById('cancelConfirmModal');
    if (modalElement) {
      const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
      if (modal) {
        modal.hide();
      }
    }
  }
}
