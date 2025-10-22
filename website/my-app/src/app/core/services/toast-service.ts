import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ToastType } from '../../shared/models/toast-type.model';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastSubject = new BehaviorSubject<{
    message: string;
    type: ToastType
  } | null>(null);

  public toast$ = this.toastSubject.asObservable();

  show(message: string, type: ToastType = 'success') {
    this.toastSubject.next({ message, type });
    setTimeout(() => this.toastSubject.next(null), 3000);
  }
}
