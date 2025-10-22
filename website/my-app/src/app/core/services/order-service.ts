import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';
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

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  loadOrders(): void {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    this.http.get<any>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Error loading orders:', error);
        this.loadingSubject.next(false);

        // Set appropriate error message based on error type
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
        this.ordersSubject.next(response.data.content || response);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      },
      error: () => {
        // Error already handled in catchError
      }
    });
  }

  createOrder(order: Partial<Order>): Observable<any> {
    return this.http.post<any>(this.apiUrl, order).pipe(
      tap(() => this.loadOrders()),
      catchError(error => {
        console.error('Error creating order:', error);
        return throwError(() => error);
      })
    );
  }

  updateOrderStatus(orderId: string, status: OrderStatus): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${orderId}/status`, { status }).pipe(
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

  getOrderById(orderId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${orderId}`).pipe(
      catchError(error => {
        console.error('Error getting order by ID:', error);
        return throwError(() => error);
      })
    );
  }

  clearError(): void {
    this.errorSubject.next(null);
  }
}
