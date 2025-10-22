import { Component, OnInit } from '@angular/core';
import { ToastService } from '../../../core/services/toast-service';
import { ToastType } from '../../models/toast-type.model';

@Component({
  selector: 'app-toast',
  imports: [],
  templateUrl: './toast.html',
  styleUrl: './toast.css'
})
export class Toast implements OnInit {
  toast: { message: string; type: ToastType } | null = null;

  constructor(private toastService: ToastService) { }

  ngOnInit() {
    this.toastService.toast$.subscribe(toast => {
      this.toast = toast;
    });
  }
}
