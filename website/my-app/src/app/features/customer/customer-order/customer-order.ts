import { Component, inject } from '@angular/core';
import { MenuItem, CartItem, Order } from '../../../shared/models/order.model';
import { OrderService } from '../../../core/services/order-service';
import { ToastService } from '../../../core/services/toast-service';
import { CartService } from '../../../core/services/cart-service';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CartItemComponent } from "./components/cart-item-component/cart-item-component";
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CustomerFormComponent } from './components/customer-form/customer-form';

@Component({
  selector: 'app-customer-order',
  imports: [CartItemComponent, CommonModule, ReactiveFormsModule, CustomerFormComponent],
  templateUrl: './customer-order.html',
  styleUrl: './customer-order.css'
})
export class CustomerOrder {
  private cartService = inject(CartService);
  private orderService = inject(OrderService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  // Mock menu
  menuItems: MenuItem[] = [
    { id: 1, menu_name: 'ข้าวผัดกุ้ง', price: 60, image: 'https://cdn-icons-png.flaticon.com/512/3174/3174887.png', category: 'อาหารจานหลัก' },
    { id: 2, menu_name: 'ส้มตำ', price: 40, image: 'https://cdn-icons-png.flaticon.com/512/933/933310.png', category: 'อาหารจานเดียว' },
    { id: 3, menu_name: 'ต้มยำกุ้ง', price: 80, image: 'https://cdn-icons-png.flaticon.com/512/1046/1046784.png', category: 'อาหารประเภทต้ม' },
    { id: 4, menu_name: 'ผัดไทย', price: 50, image: '', category: 'อาหารจานหลัก' },
    { id: 5, menu_name: 'ข้าวมันไก่', price: 45, image: 'https://cdn-icons-png.flaticon.com/512/3174/3174891.png', category: 'อาหารจานเดียว' },
    { id: 6, menu_name: 'ข้าวขาหมู', price: 55, image: 'https://cdn-icons-png.flaticon.com/512/857/857681.png', category: 'อาหารจานเดียว' },
    { id: 7, menu_name: 'แกงเขียวหวานไก่', price: 70, image: 'https://cdn-icons-png.flaticon.com/512/1046/1046857.png', category: 'แกง' },
    { id: 8, menu_name: 'ปลาทอดน้ำปลา', price: 90, image: 'https://cdn-icons-png.flaticon.com/512/1699/1699876.png', category: 'อาหารจานหลัก' }
  ];

  cart: CartItem[] = [];
  showCart: boolean = false;
  isPlacingOrder = false;
  customerForm!: FormGroup;

  ngOnInit() {
    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
    });

    this.customerForm = this.fb.group({
      customerName: ['', [Validators.required, Validators.minLength(2)]],
      phone: ['', [Validators.required, this.phoneValidator.bind(this)]]
    });
  }

  phoneValidator(control: AbstractControl): ValidationErrors | null {
    if (control.value && !this.isValidPhone(control.value)) {
      return { invalidPhone: true };
    }
    return null;
  }

  isValidPhone(phone: string): boolean {
    // ลบช่องว่างและเครื่องหมายขีดออก
    const normalized = phone.replace(/[\s-]/g, '');

    // รูปแบบเบอร์ไทยทั่วไป:
    // - 0XXXXXXXXX (เช่น 0812345678)
    // - +66XXXXXXXXX (เช่น +66812345678)
    // - (+66)XXXXXXXXX (เช่น (+66)812345678)
    const regex = /^(0\d{9}|(\+66|66)\d{8}|\(?\+66\)?\d{8})$/;

    return regex.test(normalized);
  }

  normalizePhone(phone: string): string {
    let normalized = phone.replace(/[\s-()]/g, '');
    if (normalized.startsWith('+66')) {
      normalized = '0' + normalized.substring(3);
    } else if (normalized.startsWith('66')) {
      normalized = '0' + normalized.substring(2);
    }
    return normalized;
  }


  // Getter Validation for HTML
  get f() {
    return this.customerForm.controls;
  }

  addToCart(item: MenuItem) {
    this.cartService.addToCart(item);
    // this.toastService.show(`เพิ่ม ${item.menu_name} ลงตะกร้าแล้ว`, 'success');
  }

  removeFromCart(itemId: number) {
    this.cartService.removeFromCart(itemId);
  }

  updateQty(itemId: number, delta: number) {
    const item = this.cart.find(c => c.id === itemId);
    if (item) {
      this.cartService.updateQuantity(itemId, item.qty + delta);
    }
  }

  getTotal(): number {
    return this.cartService.getTotal();
  }

  submitOrder() {
    if (this.cart.length === 0) {
      this.toastService.show('กรุณาเลือกสินค้า', 'error');
      return;
    }

    // Form Validation
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      this.toastService.show('กรุณากรอกข้อมูลลูกค้าให้ถูกต้อง', 'error');
      return;
    }

    const orderData: Partial<Order> = {
      customer_name: this.customerForm.value.customerName,
      phone: this.normalizePhone(this.customerForm.value.phone),
      details: this.cart,
      total_amount: this.getTotal()
    };

    this.isPlacingOrder = true;

    this.orderService.createOrder(orderData).subscribe({
      next: () => {
        this.toastService.show('สั่งอาหารสำเร็จ!', 'success');
        this.cartService.clearCart();
        this.customerForm.reset();
        this.isPlacingOrder = false;

        const offcanvasElement = document.getElementById('mobileCartOffcanvas');
        if (offcanvasElement) {
          const closeButton = offcanvasElement.querySelector('[data-bs-dismiss="offcanvas"]') as HTMLButtonElement;
          if (closeButton) {
            closeButton.click();
          }
        }

        this.router.navigate(['/admin']);
      },
      error: () => {
        this.isPlacingOrder = false;
        this.toastService.show('เกิดข้อผิดพลาด', 'error');
      }
    });
  }

  toggleCart() {
    this.showCart = !this.showCart;
  }

  onImageError(event: Event) {
    const target = event.target as HTMLImageElement;
    target.src = 'https://placehold.co/80x80?text=No+Image';
  }
}
