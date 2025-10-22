import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-cart-item-component',
  imports: [],
  templateUrl: './cart-item-component.html',
  styleUrl: './cart-item-component.css'
})
export class CartItemComponent {
  @Input() item: any;
  @Input() readonly: boolean = false;
  @Output() removeItem: EventEmitter<number> = new EventEmitter();
  @Output() updateQuantity: EventEmitter<{ id: number, change: number }> = new EventEmitter();

  onRemoveClick() {
    this.removeItem.emit(this.item.id);
  }

  onQuantityChange(change: number) {
    this.updateQuantity.emit({ id: this.item.id, change });
  }
}
