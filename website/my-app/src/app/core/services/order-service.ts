import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';
import { Order } from '../../shared/models/order.model';
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
      .set('page', (page - 1).toString()) // backend ใช้ page index เริ่มที่ 0
      .set('size', size.toString());

    // เพิ่ม searchId ถ้ามีค่า
    if (searchId && searchId.trim() !== '') {
      params = params.set('searchId', searchId.trim());
    }

    this.http.get<any>(this.apiUrl, { params }).pipe(
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
      tap(() => {
        // this.loadOrders()
      }),
      catchError(error => {
        console.error('Error creating order:', error);
        return throwError(() => error);
      })
    );
  }

  clearError(): void {
    this.errorSubject.next(null);
  }
}
