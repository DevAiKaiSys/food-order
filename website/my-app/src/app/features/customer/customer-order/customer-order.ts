import { Component } from '@angular/core';
import { MenuItem, CartItem } from '../../../shared/models/order.model';
import { OrderService } from '../../../core/services/order-service';
import { ToastService } from '../../../core/services/toast-service';
import { CartService } from '../../../core/services/cart-service';
import { FormsModule } from '@angular/forms';
import { CartItemComponent } from "./components/cart-item-component/cart-item-component";

@Component({
  selector: 'app-customer-order',
  imports: [FormsModule, CartItemComponent],
  templateUrl: './customer-order.html',
  styleUrl: './customer-order.css'
})
export class CustomerOrder {
  // Mock menu
  menuItems: MenuItem[] = [
    { id: 1, menuName: 'ข้าวผัดกุ้ง', price: 60, image: 'https://cdn-icons-png.flaticon.com/512/3174/3174887.png', category: 'อาหารจานหลัก' },
    { id: 2, menuName: 'ส้มตำ', price: 40, image: 'https://cdn-icons-png.flaticon.com/512/933/933310.png', category: 'อาหารจานเดียว' },
    { id: 3, menuName: 'ต้มยำกุ้ง', price: 80, image: 'https://cdn-icons-png.flaticon.com/512/1046/1046784.png', category: 'อาหารประเภทต้ม' },
    { id: 4, menuName: 'ผัดไทย', price: 50, image: '', category: 'อาหารจานหลัก' },
    { id: 5, menuName: 'ข้าวมันไก่', price: 45, image: 'https://cdn-icons-png.flaticon.com/512/3174/3174891.png', category: 'อาหารจานเดียว' },
    { id: 6, menuName: 'ข้าวขาหมู', price: 55, image: 'https://cdn-icons-png.flaticon.com/512/857/857681.png', category: 'อาหารจานเดียว' },
    { id: 7, menuName: 'แกงเขียวหวานไก่', price: 70, image: 'https://cdn-icons-png.flaticon.com/512/1046/1046857.png', category: 'แกง' },
    { id: 8, menuName: 'ปลาทอดน้ำปลา', price: 90, image: 'https://cdn-icons-png.flaticon.com/512/1699/1699876.png', category: 'อาหารจานหลัก' }
  ];

  cart: CartItem[] = [];
  customerName: string = '';
  customerPhone: string = '';
  showCart: boolean = false;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private toastService: ToastService
  ) { }

  ngOnInit() {
    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
    });
  }

  addToCart(item: MenuItem) {
    this.cartService.addToCart(item);
    this.toastService.show(`เพิ่ม ${item.menuName} ลงตะกร้าแล้ว`, 'success');
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

    if (!this.customerName || !this.customerPhone) {
      this.toastService.show('กรุณากรอกข้อมูลให้ครบถ้วน', 'error');
      return;
    }

    const orderData: any = {
      customer: this.customerName,
      phone: this.customerPhone,
      items: this.cart,
      total: this.getTotal()
    };

    this.orderService.createOrder(orderData).subscribe({
      next: () => {
        this.toastService.show('สั่งอาหารสำเร็จ!', 'success');
        this.cartService.clearCart();
        this.customerName = '';
        this.customerPhone = '';
      },
      error: () => {
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
