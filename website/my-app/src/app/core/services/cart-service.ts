import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CartItem, MenuItem } from '../../shared/models/order.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<CartItem[]>([]);
  public cart$ = this.cartSubject.asObservable();

  addToCart(item: MenuItem) {
    const cart = this.cartSubject.value;
    const existing = cart.find(c => c.id === item.id);

    if (existing) {
      existing.qty++;
      this.cartSubject.next([...cart]);
    } else {
      this.cartSubject.next([...cart, { ...item, qty: 1 }]);
    }
  }

  removeFromCart(itemId: number) {
    const cart = this.cartSubject.value.filter(c => c.id !== itemId);
    this.cartSubject.next(cart);
  }

  updateQuantity(itemId: number, qty: number) {
    const cart = this.cartSubject.value.map(c =>
      c.id === itemId ? { ...c, qty: Math.max(1, qty) } : c
    );
    this.cartSubject.next(cart);
  }

  clearCart() {
    this.cartSubject.next([]);
  }

  getTotal(): number {
    return this.cartSubject.value.reduce((sum, item) =>
      sum + (item.price * item.qty), 0
    );
  }

  getItemCount(): number {
    return this.cartSubject.value.reduce((sum, item) => sum + item.qty, 0);
  }
}
