import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, delay, Observable, of, tap, throwError } from 'rxjs';
import { Order, OrderStatus } from '../../shared/models/order.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/orders`;

  private ordersSubject = new BehaviorSubject<Order[]>([]);
  public orders$ = this.ordersSubject.asObservable();

  private paginationSubject = new BehaviorSubject<{
    totalPages: number,
    totalItems: number,
    currentPage: number,
    pageSize: number
  }>({ totalPages: 0, totalItems: 0, currentPage: 1, pageSize: 10 });
  public pagination$ = this.paginationSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  loadOrders(page: number = 1, size: number = 10, searchId: string = '') {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString());

    if (searchId && searchId.trim() !== '') {
      params = params.set('searchId', searchId.trim());
    }

    this.http.get<any>(this.apiUrl, { params }).pipe(
      catchError(error => {
        console.error('Error loading orders:', error);
        this.loadingSubject.next(false);

        let errorMessage = 'ไม่สามารถโหลดข้อมูลได้';
        if (error.status === 0) {
          errorMessage = 'ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้';
        } else if (error.status === 404) {
          errorMessage = 'ไม่พบข้อมูล';
        } else if (error.status === 500) {
          errorMessage = 'เซิร์ฟเวอร์เกิดข้อผิดพลาด';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }

        this.errorSubject.next(errorMessage);
        return throwError(() => error);
      })
    ).subscribe({
      next: (response) => {
        const data = response?.data;
        if (!data) {
          this.ordersSubject.next([]);
          this.loadingSubject.next(false);
          return;
        }

        this.ordersSubject.next(data.content || []);

        this.paginationSubject.next({
          totalPages: data.total_pages || 0,
          totalItems: data.total_elements || 0,
          currentPage: (data.number || 0) + 1,
          pageSize: data.size || size
        });

        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      },
      error: () => { }
    });
  }

  // เพิ่ม method สำหรับดึงรายละเอียด order
  getOrderDetail(orderId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${orderId}`).pipe(
      catchError(error => {
        console.error('Error loading order detail:', error);
        return throwError(() => error);
      })
    );
  }

  createOrder(order: Partial<Order>): Observable<any> {
    return this.http.post<any>(this.apiUrl, order).pipe(
      tap(() => { }),
      catchError(error => {
        console.error('Error creating order:', error);
        return throwError(() => error);
      })
    );
  }

  clearError(): void {
    this.errorSubject.next(null);
  }

  updateOrderStatus(orderId: number, status: OrderStatus): Observable<any> {
    let endpoint = '';

    // เลือก endpoint ตามสถานะ
    switch (status) {
      case OrderStatus.CONFIRMED:
        endpoint = `${this.apiUrl}/${orderId}/confirm`;
        break;
      case OrderStatus.COOKING:
        endpoint = `${this.apiUrl}/${orderId}/cook`;
        break;
      case OrderStatus.DELIVERING:
        endpoint = `${this.apiUrl}/${orderId}/deliver`;
        break;
      case OrderStatus.COMPLETED:
        endpoint = `${this.apiUrl}/${orderId}/complete`;
        break;
      case OrderStatus.CANCELLED:
        endpoint = `${this.apiUrl}/${orderId}/cancel`;
        break;
      default:
        return throwError(() => new Error('Invalid status'));
    }

    return this.http.post<any>(endpoint, {}).pipe(
      delay(400), // จำลอง API delay
      catchError(error => {
        console.error('Error updating order status:', error);
        return throwError(() => error);
      })
    );
  }

  cancelOrder(orderId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${orderId}`).pipe(
      tap(() => this.loadOrders()),
      catchError(error => {
        console.error('Error canceling order:', error);
        return throwError(() => error);
      })
    );
  }
}
